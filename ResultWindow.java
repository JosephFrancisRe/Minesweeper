import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ResultWindow extends JFrame {
	String imagePath;
	JPanel resultPane;
	Board instance;
	
	public ResultWindow(int type, Board instance) {
		// Type 0 = loss, 1 = win
		if (type == 0) {
			imagePath = "Win.png";
		} else if (type == 1) {
			imagePath = "Lose.png";
		}
		
		this.instance = instance;
		this.setTitle("Result");
		this.setSize(850, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
		this.setContentPane(createWindow());
	}
		
	private JPanel createWindow() {
		resultPane = new JPanel();
		
		ImageIcon image = new ImageIcon(imagePath);    
		JLabel iconLbl = new JLabel();
		iconLbl.setIcon(image);
		resultPane.add(iconLbl);
		
		JButton newGame = new JButton("New Game");
		newGame.addActionListener(new NewButtonListener());
		newGame.setFont(new Font("Verdana", Font.PLAIN, 20));
		resultPane.add(newGame);
		
		JButton restart = new JButton("Restart");
		restart.addActionListener(new RestartButtonListener());
		restart.setFont(new Font("Verdana", Font.PLAIN, 20));
		resultPane.add(restart);
		
		JButton review = new JButton("Review");
		review.addActionListener(new ReviewButtonListener());
		review.setFont(new Font("Verdana", Font.PLAIN, 20));
		resultPane.add(review);
		
		resultPane.setBackground(new Color(206, 202, 212));
		return resultPane;
	}
	
	class NewButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent event) {
		   instance.restartGame(0);
		   dispose();
	   }
	}
	
	class RestartButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent event) {
		   instance.restartGame(3);
		   dispose();
	   }
	}
	
	class ReviewButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent event) {
		   dispose();
	   }
	}
}
