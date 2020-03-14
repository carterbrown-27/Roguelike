import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class View {
	
	private int JFrame_WIDTH = 1500;
	private int JFrame_HEIGHT = 1000;
	private Font font;
	private JFrame frame;
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
		// GUI gui = new GUI();
		// gui.run();

		frame = new JFrame();
		// TODO (X) Overhaul GUI.
		JFrame_HEIGHT = 900;
		JFrame_WIDTH = 1600;
		
		font = new Font("Serif",Font.BOLD,20);

		try{
			InputStream is = new FileInputStream("DATA/Adventurer.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			font = font.deriveFont(Font.PLAIN, 20);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		// TODO: use different icon for game.
		// frame.setIconImage(Main.player.getSprite());

		txt = new ArrayList<String>();
		panel = new JPanel();
		area = new JTextArea();
		stats = new JTextArea();
		consolePanel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.add(consolePanel);
		panel.setBackground(Color.BLACK);

		area.setBackground(Color.BLACK);
		area.setFont(font.deriveFont(Font.PLAIN,24));
		area.setEditable(false);
		area.setFocusable(false);
		area.setForeground(Color.WHITE);

		stats.setFocusable(false);
		stats.setEditable(false);

		consolePanel.setLayout(new BorderLayout());
		consolePanel.add(stats,BorderLayout.NORTH);
		consolePanel.add(area,BorderLayout.CENTER);
		consolePanel.setBackground(Color.BLACK);
		consolePanel.setBorder(new EmptyBorder(20,20,20,20));

		frame.setTitle("Roguelike");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(JFrame_WIDTH, JFrame_HEIGHT);
		frame.setVisible(true);
	}

	// TODO (R) Refactor this completely, don't re-place components each time
	public void refreshFrame(BufferedImage render) {
		panel.removeAll();
		
		JLabel picLabel = new JLabel(new ImageIcon(render));
		panel.add(picLabel);
		panel.setSize(frame.getWidth(), frame.getHeight());
		refreshStats();

		panel.add(consolePanel);
		frame.add(panel);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		frame.revalidate();
		frame.repaint();
	}

	public void refreshStats(){
		stats.setBackground(Color.BLACK);
		stats.setForeground(Color.LIGHT_GRAY);
		stats.setFont(font);
		stats.setText("");

		// TODO (R) Refactor
		stats.append(Main.playerName+"\n~~~~~\n");
		stats.append(String.format("HP: %.1f/%.1f\n", Main.player.getHP(), Main.player.getHP_max()));
		stats.append(String.format("SP: %.1f/%.1f\n", Main.player.getSP(), Main.player.getSP_max()));
		stats.append(String.format("STR: %s\n",Main.player.getStrength()));
		stats.append(String.format("DEF: %s\n",Main.player.getDefence()));
		stats.append(String.format("SAT: %d\n",Math.round(Main.player.getSAT())));

		stats.append("~~~~~\n");
		
		if(Main.player.weapon != null){			
			stats.append("Weapon: "+Main.player.weapon.getDisplayName());
		}else{
			stats.append("Weapon: none");
		}
		stats.append("\n");

		stats.append("Quivered: "+ (Main.player.quivered == null ? "none" : Main.player.quivered.getDisplayName()));

		stats.append("\n");


		String line = "  ";
		for(Status s: Main.player.getStatuses().keySet()){
			line += s.name() + " ";
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
