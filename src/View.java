import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class View {
	
	private int JFrame_WIDTH = 1600;
	private int JFrame_HEIGHT = 900;
	private Font font;
	private JFrame frame;
	private JLabel gameImage;
	private JPanel panel;
	private JPanel consolePanel;
	private JTextArea area;
	private JTextArea stats;
	
	private static ArrayList<String> txt;
	private static final int rows = 15;
	
	View() {
		init();
	}
	
	// TODO (R) Set new Layout.
	public void init() {

		frame = new JFrame();

		// TODO (X) Overhaul GUI.
		JFrame_HEIGHT = 900;
		JFrame_WIDTH = 1600;
		
		font = new Font("Serif",Font.BOLD,20);

		try{
			InputStream is = new FileInputStream("DATA/Adventurer.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			font = font.deriveFont(Font.PLAIN, 20);
		}catch(Exception e){
			e.printStackTrace();
		}

		// TODO: use different icon for game.
		// frame.setIconImage(Main.getPlayer().getSprite());

		txt = new ArrayList<String>();
		panel = new JPanel();
		area = new JTextArea();
		stats = new JTextArea();
		consolePanel = new JPanel();
		gameImage = new JLabel();
		
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.add(gameImage);
		panel.add(consolePanel);
		panel.setBackground(Color.BLACK);
		panel.setBorder(new EmptyBorder(20,15,20,20));

		area.setBackground(Color.BLACK);
		area.setFont(font.deriveFont(Font.PLAIN,24));
		area.setEditable(false);
		area.setFocusable(false);
		area.setForeground(Color.WHITE);

		initStats();
		stats.setFocusable(false);
		stats.setEditable(false);

		consolePanel.setLayout(new BorderLayout());
		consolePanel.add(stats,BorderLayout.NORTH);
		consolePanel.add(area,BorderLayout.CENTER);
		consolePanel.setBackground(Color.BLACK);
		consolePanel.setBorder(new EmptyBorder(20,20,20,20));

		panel.setSize(frame.getWidth(), frame.getHeight());
		frame.add(panel);

		frame.setTitle("Roguelike");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(JFrame_WIDTH, JFrame_HEIGHT);
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
	}

	// TODO (R) Refactor this completely, don't re-place components each time
	public void refreshFrame(BufferedImage render) {
		gameImage.setIcon(new ImageIcon(render));
		refreshStats();

		panel.revalidate();
		panel.repaint();

		frame.revalidate();
		frame.repaint();
	}

	public void initStats(){
		stats.setBackground(Color.BLACK);
		stats.setForeground(Color.LIGHT_GRAY);
		stats.setFont(font);
	}

	public void refreshStats(){
		stats.setText("");

		// TODO (R) Refactor
		stats.append(Main.getPlayerName()+"\n");
		stats.append("~~~~~\n");
		stats.append(String.format("HP: %d/%d\n", (int) Main.getPlayer().getHP(), (int) Main.getPlayer().getHP_max()));
		stats.append(String.format("SP: %d/%d\n", (int) Main.getPlayer().getSP(), (int) Main.getPlayer().getSP_max()));
		stats.append(String.format("STR: %s\n",	Main.getPlayer().getStrength()));
		stats.append(String.format("DEF: %s\n",	Main.getPlayer().getDefence()));
		stats.append(String.format("SAT: %d\n",	Math.round(Main.getPlayer().getSAT())));
		stats.append("~~~~~\n");
		

		stats.append("Weapon: " + (Main.getPlayer().weapon == null ? "none" : Main.getPlayer().weapon.getDisplayName()));
		stats.append("\n");

		stats.append("Quivered: " + (Main.getPlayer().quivered == null ? "none" : Main.getPlayer().quivered.getDisplayName()));

		stats.append("\n");


		String line = "";
		for(Status s: Main.getPlayer().getStatuses().keySet()){
			line += s.name() + ", ";
		}
		stats.append(line+"\n");
	}

	public void appendText(String text){
		String[] strArray = text.split("\\r?\\n");
		for(String str: strArray){
			txt.add(str);
		}
		refreshText();
	}

	public void refreshText(){
		area.setText("");
		while (txt.size()>rows){
			txt.remove(0);
		}
		for(int i = 0; i < txt.size(); i++){
			area.append(txt.get(i)+"\n"+"\n");
		}
	}
	
	public void clearText() {
		txt.clear();
		refreshText();
	}
	
	public JFrame getFrame() {
		return frame;
	}
}
