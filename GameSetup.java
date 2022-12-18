import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GameSetup extends JFrame {
	
	JPanel menu;
	JTextField size;
	String sizeText;
	
	public GameSetup() {
		this.setTitle("Minesweeper Setup");
		this.setSize(1050, 350);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
		this.setContentPane(createMenu());
	}

	private JPanel createMenu() {
		menu = new JPanel();
		menu.setBackground(new Color(206, 202, 212));
		
		ImageIcon image = new ImageIcon("MinesweeperTitle.png");    
		JLabel iconLbl = new JLabel();
		iconLbl.setIcon(image);
		menu.add(iconLbl);
		
		JLabel gameSize = new JLabel("Game Size:");
		gameSize.setFont(new Font("Verdana", Font.PLAIN, 20));
		menu.add(gameSize);
		size = new JTextField(2);
		size.setFont(new Font("Verdana", Font.PLAIN, 20));
		menu.add(size);
		
		JButton play = new JButton("Play");
		play.addActionListener(new PlayButtonListener());
		menu.add(play);
		return menu;
	}
	
	class PlayButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent event) {
		   sizeText = size.getText();
		   try {
			   Integer comp = Integer.parseInt(sizeText);
			   if (comp < 3 || comp > 16) {
				   JOptionPane.showMessageDialog(null, "Minimum game board size is 3. Maximum game board size is 16. Please try again.");
			   } else {
				   dispose();
				   Board instance = new Board(comp);
			   }
		   } catch (NumberFormatException e) {
			   JOptionPane.showMessageDialog(null, "Invalid numerical input. Please try again.");
		   }
	   }
	}
}