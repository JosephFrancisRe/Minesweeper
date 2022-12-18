import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.awt.Toolkit;

public class Board extends JFrame {

	// Constants
	private static final Integer CELLSIZE = 60;
	
	// Board variables
	private boolean activeGame = true;
	private boolean activeCountdown = true;
	private JLabel timerLabel, flagsRemainingLabel;
	private Integer countdown = 1000;
	private Integer gameSize = 16;
	private Integer mineCount = gameSize * 2 + (gameSize / 2);
	private Integer flagsRemaining;
	private ArrayList<Cell> cellList = new ArrayList<Cell>();
	private ArrayList<Integer> mineList = new ArrayList<Integer>();
	private ArrayList<Integer> revealList = new ArrayList<Integer>();
	private ArrayList<Integer> flagList = new ArrayList<Integer>();
	
	// Network variables
	int port = 8000;
	DataInputStream in;
	DataOutputStream out;
	ServerSocket server;
	Socket socket;
	
	public Board() {
		BoardSetup();
	}

	public Board(Integer comp) {
		gameSize = comp;
		
		// Variable mineCount based on the size of the board
		if (gameSize == 15)
			mineCount = 36;
		else if (gameSize == 14)
			mineCount = 33;
		if (gameSize < 14)
			mineCount = (int)(Math.pow(comp, 2) * .15);
		if (gameSize < 14 && gameSize >= 6)
			mineCount += (gameSize / 2);
		if (gameSize < 6)
			mineCount *= 2;
		
		flagsRemaining = mineCount;
		
		BoardSetup();
	}
	
	// Constructor for loading a game
	public Board(Integer timerInt, Integer gameSizeInt, Integer flagsRemainingInt, String mineJson, String revealJson,
			String flagJson) {
		this.setTitle("Minesweeper");
		this.setSize(CELLSIZE * gameSize, CELLSIZE * gameSize);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
		
		countdown = timerInt;
		gameSize = gameSizeInt;
		flagsRemaining = flagsRemainingInt;
		
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		
		Timer timer = new Timer();
        timer.schedule(new GameClock(), 1000, 1000);
        
		timerLabel = new JLabel("Time Remaining: Loading", SwingConstants.CENTER);
		this.add(timerLabel, BorderLayout.NORTH);
		
		BoardPanel gameBoard = new BoardPanel(new GridLayout(gameSize, gameSize), this, mineJson, revealJson, flagJson);
		this.add(gameBoard, BorderLayout.CENTER);
		
		flagsRemainingLabel = new JLabel(flagsRemaining.toString());
		this.add(flagsRemainingLabel, BorderLayout.SOUTH);
		
		try {
			socket = new Socket("localhost", port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			System.err.println("Error! Unable to connect.");
			e.printStackTrace();
		}
	}

	private void BoardSetup() {
		this.setTitle("Minesweeper");
		this.setSize(CELLSIZE * gameSize, CELLSIZE * gameSize);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
		
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		
		Timer timer = new Timer();
        timer.schedule(new GameClock(), 1000, 1000);
        
		timerLabel = new JLabel("Time Remaining: 1000", SwingConstants.CENTER);
		this.add(timerLabel, BorderLayout.NORTH);
		
		BoardPanel gameBoard = new BoardPanel(new GridLayout(gameSize, gameSize), this);
		this.add(gameBoard, BorderLayout.CENTER);
		
		flagsRemainingLabel = new JLabel(flagsRemaining.toString());
		this.add(flagsRemainingLabel, BorderLayout.SOUTH);
		
		try {
			socket = new Socket("localhost", port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			System.err.println("Error! Unable to connect.");
			e.printStackTrace();
		}
	}
	    
	class GameClock extends TimerTask {
	    public void run() {
	    	if (activeCountdown) {
		        countdown--;
		        timerLabel.setText("Time Remaining: " + countdown);
		        
		        if (countdown <= 0) {
		        	cancel();
		        	dispose();
					GameSetup game = new GameSetup();
					game.setVisible(true);
		        }
	    	}
	    }
	}
	
	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		menu.add(createFileNewItem());
		menu.add(createFileRestartItem());
		menu.add(createSaveGameItem());
		menu.add(createLoadGameItem());
		menu.add(createLeaderboardItem());
		menu.add(createFileExitItem());
		return menu;
	}
	
	private JMenuItem createFileNewItem() {
		JMenuItem item = new JMenuItem("New");
		item.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				restartGame(0);
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}
	
	private JMenuItem createFileRestartItem() {
		JMenuItem item = new JMenuItem("Restart");
		item.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				restartGame(3);
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}
	
	private JMenuItem createLeaderboardItem() {
		JMenuItem item = new JMenuItem("Leaderboard");
		item.setAccelerator(KeyStroke.getKeyStroke('H', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				try {
					String message = "High Scores\n";
					out.writeInt(0);
					for (int i = 0; i < 5; i++)
						message = message + in.readInt() + "\n";
					JOptionPane.showMessageDialog(null, message, "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					System.err.println("Error! Unable to connect.");
					e.printStackTrace();
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}
	
	private JMenuItem createSaveGameItem() {
		JMenuItem item = new JMenuItem("Save Game");
		item.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				try {					
					String name = JOptionPane.showInputDialog("What name would you like to give your saved game?");
					out.writeInt(2);
					out.writeInt(name.length());
					out.writeChars(name);
					out.writeInt(countdown);
					out.writeInt(gameSize);
					out.writeInt(flagsRemaining);
					
					String mineJson = new Gson().toJson(mineList);
					out.writeInt(mineJson.length());
					out.writeChars(mineJson);
					
					createRevealList();
					
					String revealJson = new Gson().toJson(revealList);
					out.writeInt(revealJson.length());
					out.writeChars(revealJson);
					
					createFlagList();
					
					String flagJson = new Gson().toJson(flagList);
					out.writeInt(flagJson.length());
					out.writeChars(flagJson);
				} catch (Exception e) {
					System.err.println("Error! Unable to connect.");
					e.printStackTrace();
				}
			}

			private void createRevealList() {
				revealList.clear();
				for (int i = 0; i < cellList.size(); i++) {
					// If a cell is not revealed, a 0 is stored in the revealList. Otherwise, a 1 is stored.
					if (!cellList.get(i).isUncovered()) {
						revealList.add(0);
					} else {
						revealList.add(1);
					}
				}
			}
			
			private void createFlagList() {
				flagList.clear();
				for (int i = 0; i < cellList.size(); i++) {
					// If a cell is not flagged, a 0 is stored in the flagList. Otherwise, a 1 is stored.
					if (!cellList.get(i).isFlagged()) {
						flagList.add(0);
					} else {
						flagList.add(1);
					}
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}
	
	private JMenuItem createLoadGameItem() {
		JMenuItem item = new JMenuItem("Load Game");
		item.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				try {					
					String name = JOptionPane.showInputDialog("Which game would you like to open?");
					out.writeInt(3);
					out.writeInt(name.length());
					out.writeChars(name);

					Integer timerInt = in.readInt();
					Integer gameSizeInt = in.readInt();
					Integer flagsRemainingInt = in.readInt();
					
					Integer mineJsonLength = in.readInt();
					String mineJson = "";
					
					for (int i = 0; i < mineJsonLength; i++) {
						mineJson += in.readChar();
					}
					
					Integer revealJsonLength = in.readInt();
					String revealJson = "";
					
					for (int i = 0; i < revealJsonLength; i++) {
						revealJson += in.readChar();
					}
					
					Integer flagJsonLength = in.readInt();
					String flagJson = "";
					
					for (int i = 0; i < flagJsonLength; i++) {
						flagJson += in.readChar();
					}
					
					dispose();
					Board instance = new Board(timerInt, gameSizeInt, flagsRemainingInt, mineJson, revealJson, flagJson);
					instance.setVisible(true);
				} catch (Exception e) {
					System.err.println("Error! Unable to connect.");
					e.printStackTrace();
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}
	
	private JMenuItem createFileExitItem() {
		JMenuItem item = new JMenuItem("Exit");
		item.setAccelerator(KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				try {
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}

	public class BoardPanel extends JPanel {
		private int maximum = (int)Math.pow(gameSize, 2);
		private boolean unique;
		private Board instance;
		
		public BoardPanel(GridLayout grid, Board instance) {
			super(grid);
			this.instance = instance;
			instantiateMineList();
			instantiateCells();
			addCells();
		}
		
		// Constructor for creating a BoardPanel from a loaded game
		public BoardPanel(GridLayout grid, Board instance, String mineJson, String revealJson, String flagJson) {
			super(grid);
			this.instance = instance;
			
			Gson gson = new Gson();
	        mineList = gson.fromJson(mineJson, new TypeToken<ArrayList<Integer>>() {}.getType());
	        
			instantiateCells();
			addCells();
			revealCells(revealJson);
			flagCells(flagJson);
		}

		private void revealCells(String revealJson) {
			Gson gson = new Gson();
	        revealList = gson.fromJson(revealJson, new TypeToken<ArrayList<Integer>>() {}.getType());
	        
	        for (int i = 0; i < cellList.size(); i++) {
				if (revealList.get(i) == 1) {
					cellList.get(i).revealCell();
				}
			}
		}
		
		private void flagCells(String flagJson) {
			Gson gson = new Gson();
	        flagList = gson.fromJson(flagJson, new TypeToken<ArrayList<Integer>>() {}.getType());
	        
	        for (int i = 0; i < cellList.size(); i++) {
				if (flagList.get(i) == 1) {
					cellList.get(i).revealFlag();
				}
			}
		}

		private void addCells() {
			for (int i = 0; i < cellList.size(); i++) {
				this.add(cellList.get(i));
			}
		}

		public void instantiateMineList() {
			Random random = new Random();
			for (int i = 0; i < mineCount; i++) {
				unique = false;
				while(unique == false) {	
					int iMinePosition = random.nextInt(maximum);
					if (mineList.contains(iMinePosition) == false) {
						mineList.add(iMinePosition);
						//System.out.println(iMinePosition);
						unique = true;
					}
				}
			}
		}
		
		public void instantiateCells() {
			for (int i = 0; i < maximum; i++) {
				if (mineList.contains(i)) {
					cellList.add(new Cell(1, i, 0, false, false, instance));
				} else if (i % gameSize == 0) { 
					// Left justified cells
					if (mineList.contains(i - gameSize) /*Mine above*/ || mineList.contains(i - gameSize + 1) /*Mine above and to the right*/
							|| mineList.contains(i + 1) /*Mine to the right*/ || mineList.contains(i + gameSize) /*Mine below*/
							|| mineList.contains(i + gameSize + 1) /*Mine below and to the right*/) {
						int temp = countAdjacentBombs(i, 0);
						cellList.add(new Cell(2, i, temp, false, false, instance));
					} else {
						cellList.add(new Cell(0, i, 0, false, false, instance));
					}
				} else if (i % gameSize == gameSize - 1) { 
					// Right justified cells
					if (mineList.contains(i - gameSize - 1) /*Mine above and to the left*/ || mineList.contains(i - gameSize) /*Mine above*/
							|| mineList.contains(i - 1) /*Mine to the left*/ || mineList.contains(i + gameSize - 1) /*Mine below and to the left*/
							|| mineList.contains(i + gameSize) /*Mine below*/) {
						int temp = countAdjacentBombs(i, 1);
						cellList.add(new Cell(2, i, temp, false, false, instance));
					} else {
						cellList.add(new Cell(0, i, 0, false, false, instance));
					}
				} else {
					// Middle cells
					if (mineList.contains(i - gameSize - 1) /*Mine above and to the left*/ || mineList.contains(i - gameSize) /*Mine above*/
							|| mineList.contains(i - gameSize + 1) /*Mine above and to the right*/ || mineList.contains(i - 1) /*Mine to the left*/
							|| mineList.contains(i + 1) /*Mine to the right*/ || mineList.contains(i + gameSize - 1) /*Mine below and to the left*/
							|| mineList.contains(i + gameSize) /*Mine below*/ || mineList.contains(i + gameSize + 1) /*Mine below and to the right*/) {
						int temp = countAdjacentBombs(i, 2);
						cellList.add(new Cell(2, i, temp, false, false, instance));
					} else {
						cellList.add(new Cell(0, i, 0, false, false, instance));
					}
				}
			}
		}
		
		public int countAdjacentBombs(int i, int scenario) {
			// Scenario 0 = Left justified, 1 = right justified, 2 = middle
			int count = 0;
			
			if (scenario == 0) {
				if (mineList.contains(i - gameSize)) /*Mine above*/ 
					count++;
				if (mineList.contains(i - gameSize + 1)) /*Mine above and to the right*/
					count++;
				if (mineList.contains(i + 1)) /*Mine to the right*/
					count++;
				if (mineList.contains(i + gameSize)) /*Mine below*/
					count++;
				if (mineList.contains(i + gameSize + 1)) /*Mine below and to the right*/
					count++;
			} else if (scenario == 1) {
				if (mineList.contains(i - gameSize - 1)) /*Mine above and to the left*/
					count++;
				if (mineList.contains(i - gameSize)) /*Mine above*/
					count++;
				if (mineList.contains(i - 1)) /*Mine to the left*/
					count++;
				if (mineList.contains(i + gameSize - 1)) /*Mine below and to the left*/
					count++;
				if (mineList.contains(i + gameSize)) /*Mine below*/
					count++;
			} else {
				if (mineList.contains(i - gameSize - 1)) /*Mine above and to the left*/
					count++;
				if (mineList.contains(i - gameSize)) /*Mine above*/
					count++;
				if (mineList.contains(i - gameSize + 1)) /*Mine above and to the right*/
					count++;
				if (mineList.contains(i - 1)) /*Mine to the left*/
					count++;
				if (mineList.contains(i + 1)) /*Mine to the right*/
					count++;
				if (mineList.contains(i + gameSize - 1)) /*Mine below and to the left*/
					count++;
				if (mineList.contains(i + gameSize)) /*Mine below*/
					count++;
				if (mineList.contains(i + gameSize + 1)) /*Mine below and to the right*/
					count++;
			}
			
			return count;
		}
	}

	public void updateFlags(int i) {
		// Passing a 0 increments, passing a 1 decrements
		if (i == 0) {
			flagsRemaining++;
			flagsRemainingLabel.setText(flagsRemaining.toString());
		} else if (i == 1) {
			flagsRemaining--;
			flagsRemainingLabel.setText(flagsRemaining.toString());
		}
	}
	
	public boolean canPlaceFlag() {
	    if ((flagsRemaining - 1) >= 0)
	    	return true;
	    return false;
	}
	
	public int getGameSize() {
	    return gameSize;
	}
	
	public int getMineCount() {
	    return mineCount;
	}
	
	public boolean isActiveGame() {
	    return activeGame;
	}
	
	public ArrayList<Cell> getCellList() {
	    return cellList;
	}
	
	public void restartGame(int type) {
		// Type: 0 = new game, 1 = Lost, 2 = win, 3 = restart same sized board
		activeGame = false;
		stopClock();
		if (type == 0) {
			dispose();
			GameSetup game = new GameSetup();
			game.setVisible(true);
		} else {
			if (type == 1) {
				ResultWindow window = new ResultWindow(1, this);
			} else if (type == 2) {
				try {
					out.writeInt(1);
					out.writeInt(gameSize);
					out.writeInt(countdown);
				} catch (Exception e) {
					System.err.println("Error! Unable to connect.");
					e.printStackTrace();
				}
				ResultWindow window = new ResultWindow(0, this);
			} else  if (type == 3) {
				dispose();
				Board instance = new Board(gameSize);
				instance.setVisible(true);
			}
		}
		
	}

	private void stopClock() {
		activeCountdown = false;
	}
	
	private void activateClock() {
		activeCountdown = true;
	}
}