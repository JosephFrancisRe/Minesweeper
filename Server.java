import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.google.gson.Gson;

public class Server implements Runnable {
    
	private int clientNo = 0;
	private ArrayList<Integer> highScoreList;
	private ArrayList<Integer> mineList;
	private ArrayList<Integer> revealList;
	private ArrayList<Integer> flagList;
	
	private Connection conn;
	private PreparedStatement queryStmtScores;
	private PreparedStatement querySavedGames;
	private PreparedStatement scoreInsertStatement;
	private PreparedStatement gameInsertStatement;
	
	public Server() {
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		try {
			// Create a server socket
	        ServerSocket serverSocket = new ServerSocket(8000);
	        System.out.println("MultiThreadServer started at " + new Date() + '\n');
	        
	        highScoreList = new ArrayList<Integer>();

	        while (true) {
	        	// Listen for a new connection request
	        	Socket socket = serverSocket.accept();

	        	// Increment clientNo
	        	clientNo++;

	        	System.out.println("Starting thread for client " + clientNo + " at " + new Date() + '\n');

	        	// Find the client's host name, and IP address
	        	InetAddress inetAddress = socket.getInetAddress();
	        	System.out.println("Client " + clientNo + "'s host name is " + inetAddress.getHostName() + "\n");
	        	System.out.println("Client " + clientNo + "'s IP Address is " + inetAddress.getHostAddress() + "\n");

	        	// Create and start a new thread for the connection
	        	new Thread(new HandleAClient(socket, clientNo)).start();
	        }
		}
		catch(IOException ex) {
			System.err.println(ex);
		}
	}
	  
	// Define the thread class for handling new connection
	class HandleAClient implements Runnable {
		private Socket socket; // A connected socket
		private int clientNum;

		/** Construct a thread */
		public HandleAClient(Socket socket, int clientNum) {
			try {
				this.socket = socket;
				this.clientNum = clientNum;
			
				conn = DriverManager.getConnection("jdbc:sqlite:game.db");
				queryStmtScores = conn.prepareStatement("Select score from tbl_scores");
			} catch (SQLException e) {
				System.err.println("Connection error: " + e);
				System.exit(1);
			}
			
			/* sets up prepared statements for SQL inserts */
			String scoreInsertSQL = "INSERT INTO tbl_scores(gameSize, score) VALUES(?,?)";
			String gameInsertSQL = "INSERT INTO tbl_savedgames(name, timer, gameSize, flagsRemaining, mineList, revealList, flagList) VALUES(?,?,?,?,?,?,?)";
			try {
				scoreInsertStatement = conn.prepareStatement(scoreInsertSQL);
				gameInsertStatement = conn.prepareStatement(gameInsertSQL);
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		/** Run a thread */
		public void run() {
			try {
				// Create data input and output streams
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

				// Continuously serve the client
				while (true) {
					int request = inputFromClient.readInt();
					
					buildArr(highScoreList);
					sortArr(highScoreList);
					
					// Request = 0 means the client is fetching the leaderboard
					if (request == 0) {
						for (int i = 0; i < 5; i++) {
							outputToClient.writeInt(highScoreList.get(i));
						}
					}
					
					// Request = 1 means client wants to attempt to commit a score to the leaderboard
					else if (request == 1) {
						Integer gameSize = inputFromClient.readInt();
						Integer score = inputFromClient.readInt();
						try {
							scoreInsertStatement.setString(1, gameSize.toString());
							scoreInsertStatement.setString(2, score.toString());
							scoreInsertStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					// Request = 2 means client wants to save a game to the database
					else if (request == 2) {					
						Integer nameLength = inputFromClient.readInt();
						String name = "";
						
						for (int i = 0; i < nameLength; i++) {
							name += inputFromClient.readChar();
						}
						
						Integer timer = inputFromClient.readInt();
						Integer gameSize = inputFromClient.readInt();
						Integer flagsRemaining = inputFromClient.readInt();
						
						Integer mineJsonLength = inputFromClient.readInt();
						String mineJson = "";
						
						for (int i = 0; i < mineJsonLength; i++) {
							mineJson += inputFromClient.readChar();
						}
						
						Integer revealJsonLength = inputFromClient.readInt();
						String revealJson = "";
						
						for (int i = 0; i < revealJsonLength; i++) {
							revealJson += inputFromClient.readChar();
						}
						
						Integer flagJsonLength = inputFromClient.readInt();
						String flagJson = "";
						
						for (int i = 0; i < flagJsonLength; i++) {
							flagJson += inputFromClient.readChar();
						}
						
						try {
							gameInsertStatement.setString(1, name);
							gameInsertStatement.setString(2, timer.toString());
							gameInsertStatement.setString(3, gameSize.toString());
							gameInsertStatement.setString(4, flagsRemaining.toString());
							gameInsertStatement.setString(5, mineJson);
							gameInsertStatement.setString(6, revealJson);
							gameInsertStatement.setString(7, flagJson);
							gameInsertStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					// Request = 3 means client wants to load a game from the database
					else if (request == 3) {
						Integer nameLength = inputFromClient.readInt();
						String name = "";
						
						for (int i = 0; i < nameLength; i++) {
							name += inputFromClient.readChar();
						}
						
						try {
							querySavedGames = conn.prepareStatement("Select * from tbl_savedgames where name = \"" + name + "\"");
							PreparedStatement stmt = querySavedGames;
							ResultSet rset = stmt.executeQuery();
							
							int timerInt = rset.getInt(2);
							int gameSizeInt = rset.getInt(3);
							int flagsRemainingInt = rset.getInt(4);
							Object o1 = rset.getObject(5);
							String mineListString = o1.toString();
							Object o2 = rset.getObject(6);
							String revealListString = o2.toString();
							Object o3 = rset.getObject(7);
							String flagListString = o3.toString();
							
							outputToClient.writeInt(timerInt);
							outputToClient.writeInt(gameSizeInt);
							outputToClient.writeInt(flagsRemainingInt);
							outputToClient.writeInt(mineListString.length());
							outputToClient.writeChars(mineListString);
							outputToClient.writeInt(revealListString.length());
							outputToClient.writeChars(revealListString);
							outputToClient.writeInt(flagListString.length());
							outputToClient.writeChars(flagListString);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}

		private void buildArr(ArrayList<Integer> arr) {
			   try {
				   arr.clear();
				   
				   PreparedStatement stmt = queryStmtScores;
				   ResultSet rset = stmt.executeQuery();
					
					while (rset.next()) {
						arr.add(rset.getInt("score"));
					}
			   } catch (SQLException e) {
				   e.printStackTrace();
			   }
		}
		
		private void sortArr(ArrayList<Integer> arr) {
			Collections.sort(arr, Collections.reverseOrder());
		}
	}

	public static void main(String[] args) {
		Server serv =  new Server();
	}
}
