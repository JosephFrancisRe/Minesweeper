import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Cell extends JButton implements MouseListener {
	private int kind;
	private int location;
	private Integer adjacentBombs;
	private boolean uncovered;
	private boolean flagged;
	private JLabel adjBombText;
	private Board instance;
	
	public Cell(int kind, int location, int number, boolean uncovered, boolean flagged, Board instance) {
		addMouseListener(this);
		adjBombText = new JLabel("");
		add(adjBombText);
		this.kind = kind;
		this.location = location;
		this.adjacentBombs = number;
		this.uncovered = uncovered;
		this.flagged = flagged;
		this.instance = instance;
		this.setBackground(new Color(204, 204, 204));
	}
	
	// Getter functions
	public int getKind() { /*0 = empty, 1 = mine, 2 = number*/ return kind; }
	public int getCellLocation() { return location;	}
	public int getAdjacentBombs() { return adjacentBombs; }
	public boolean isUncovered() { return uncovered; }
	public boolean isFlagged() { return flagged; }
	
	// Setter functions
	public void setUncovered(boolean parameter) { this.uncovered = parameter; }	
	public void setFlagged(boolean parameter) {	this.flagged = parameter; }

	public void revealCells(int position) {
		if (position < 0 || position >= instance.getCellList().size())
	        return;

	    if (instance.getCellList().get(position).getKind() == 1)
	        return;
	    else if (instance.getCellList().get(position).getKind() != 1 && (!instance.getCellList().get(position).isUncovered())) {
	    	instance.getCellList().get(position).setUncovered(true);
	    	instance.getCellList().get(position).setBackground(new Color(51, 204, 255));
	    	
	    	if (instance.getCellList().get(position).getKind() == 0)
	    		instance.getCellList().get(position).adjBombText.setText("");
	    	
	    	if (instance.getCellList().get(position).getKind() == 2) {
	    		String temp = "   " + instance.getCellList().get(position).adjacentBombs.toString();
	    		instance.getCellList().get(position).adjBombText.setText(temp);
				return;
	    	}
		
	    	Integer gameSize = instance.getGameSize();
	    	
		    if ((position - gameSize - 1) % gameSize != (gameSize - 1))
		    	revealCells(position - gameSize - 1);
		    revealCells(position - gameSize);
		    if ((position - gameSize + 1) % gameSize != 0)
		    	revealCells(position - gameSize + 1);
		    if ((position - 1) % gameSize != (gameSize - 1))
		    	revealCells(position - 1);
		    if ((position + 1) % gameSize != 0)
		    	revealCells(position + 1);
		    if ((position + gameSize - 1) % gameSize != (gameSize - 1))
		    	revealCells(position + gameSize - 1);
		    revealCells(position + gameSize);
		    if ((position + gameSize + 1) % gameSize != 0)
		    	revealCells(position + gameSize + 1);
	    }
	}
	
	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		if (instance.isActiveGame()) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (!isFlagged()) {
					if (!isUncovered()) {					
						revealCell();
					}
					
					// Check for a win
					int count = 0;
					
					for (int i = 0; i < instance.getCellList().size(); i++ ) {
						if (!instance.getCellList().get(i).isUncovered()) {
							count++;
						}
					}
					
					if (count == instance.getMineCount()) {
						instance.restartGame(2);
					}
				}
			}
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (!isUncovered()) {
					if (isFlagged()) {
						adjBombText.setText("");
						this.setBackground(new Color(204, 204, 204));
						instance.updateFlags(0);
						setFlagged(false);
					} else {
						if (instance.canPlaceFlag()) {
							instance.updateFlags(1);
							revealFlag();
						}
					}
					
					// Check for a win
					int count = 0;
					
					for (int i = 0; i < instance.getCellList().size(); i++ ) {
						if (instance.getCellList().get(i).isFlagged() && instance.getCellList().get(i).getKind() == 1) {
							count++;
						}
					}
					
					if (count == instance.getMineCount()) {
						instance.restartGame(2);
					}
				}
			}
		}
	}

	public void revealCell() {
		if (getKind() == 0) {
			revealCells(location);
			setUncovered(true);
			this.setBackground(new Color(51, 204, 255));
			adjBombText.setText("");
		} else if (getKind() == 1) {
			setUncovered(true);
			revealBombs();
			adjBombText.setText("  B");
			instance.restartGame(1);
		} else if (getKind() == 2) {
			setUncovered(true);
			this.setBackground(new Color(51, 204, 255));
			String temp = "   " + adjacentBombs.toString();
			adjBombText.setText(temp);
		}
	}
	
	public void revealFlag() {
		adjBombText.setText("   F");
		this.setBackground(new Color(102, 255, 102));
		setFlagged(true);
	}

	private void revealBombs() {
		for (int i = 0; i < instance.getCellList().size(); i++ ) {
			if (instance.getCellList().get(i).getKind() == 1) {
				instance.getCellList().get(i).adjBombText.setText("  B");
				instance.getCellList().get(i).setBackground(Color.pink);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) {	}
}
