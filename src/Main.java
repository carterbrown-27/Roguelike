import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Main {
	//	public static int seed = 12345678;
	public static Random rng = new Random();
	//	public static boolean randSeed = true;
	//	public static boolean render = true;

	public static boolean running = true;

	private static JFrame frame;
	private static JPanel panel = new JPanel();
	private static JTextArea textArea = new JTextArea();

	public static Player player;

	public static int ticks = 0;

	public static double lastPress = System.currentTimeMillis();
	public static double interval = 140;

	public static ArrayList<Map> floors = new ArrayList<Map>();
	public static Map gen;

	public static int currentFloor;
	public static Point ropePoint;
	
	public static boolean attackPrimed = false;

	public static void main(String[] args){

		// Map.Room r = new Map.Room(8,8);
		// 52,90,46
		
		startGame();

		// controls

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if(running){
					if (System.currentTimeMillis()-lastPress>=interval) {
						lastPress = System.currentTimeMillis();
						if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
							player.basic(0);
						} else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
							if(e.isControlDown()){
								player.basic(6);
							}else if(e.isShiftDown()){
								player.basic(7);
							}else{
								player.basic(3);
							}
						} else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
							player.basic(2);
						} else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
							if(e.isControlDown()){
								player.basic(5);
							}else if(e.isShiftDown()){
								player.basic(4);
							}else{
								player.basic(1);
							}
						} else if (e.getKeyCode() == KeyEvent.VK_ENTER){
							System.out.println(currentFloor);
							if(floors.get(currentFloor).valueAt(player.e.getPos()) == 3){
								newFloor();
							}else if(floors.get(currentFloor).valueAt(player.e.getPos()) == 2 && currentFloor > 0){
								// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
								changeFloor(currentFloor-1,false);
							}

						} else if(e.getKeyCode() == KeyEvent.VK_P){
							floors.get(currentFloor).printMap();
							
						} else if(e.getKeyCode() == KeyEvent.VK_SPACE){
							// open attack selections
							if(!attackPrimed){
								attackPrimed = true;
							}else{
								attackPrimed = false;
							}
							// if already open attack
						}
						
					}
				}else if(e.getKeyCode() == KeyEvent.VK_SPACE){
					// restart
					startGame();
				}
			}
		});
		// System.out.println(ropePoint.toString());
		refreshFrame(render(ropePoint.x,ropePoint.y));
	}
	
	public static void startGame(){
		floors.clear();
		currentFloor = 0;
		
		floors.add(new Map(52,90,49));
		ropePoint = floors.get(currentFloor).getPosition(2);
		// if(ropePoint==null) ropePoint = new Point(floors.get(currentFloor).randomOpenSpace());
		player = new Player(ropePoint.x,ropePoint.y,floors.get(currentFloor));
		// System.out.println(player.e.HP);
		
		// temporary
		int mobs = rng.nextInt(6)+8;
		for (int i = 0; i < mobs; i++) {
			Point t = floors.get(currentFloor).randomOpenSpace();
			new Entity(Creature.RAT, t.x, t.y, floors.get(currentFloor));
		}
		
		
		try{
			File output = new File("render.png");
			ImageIO.write(floors.get(currentFloor).renderMap(), "png", output);
		}catch(Exception e){};

		if(frame==null){
			frame = buildFrame(render(ropePoint.x,ropePoint.y));
		}else{
			refreshFrame(render(ropePoint.x,ropePoint.y));
		}
		
		running = true;
	}

	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		floors.add(new Map(52,90,49));
		changeFloor(currentFloor+1,true);
	}

	public static void changeFloor(int floor, boolean down){
		floors.get(currentFloor).entities.remove(player.e.name);
		currentFloor = floor;
		Point startPoint= floors.get(currentFloor).getPosition(2);
		if(!down) startPoint= floors.get(currentFloor).getPosition(3);
		player.e.x = startPoint.x;
		player.e.y = startPoint.y;
		player.e.map = floors.get(currentFloor);
		floors.get(currentFloor).addEntity(player.e);
		refreshFrame(render(startPoint.x,startPoint.y));
	}

	public static int JFrame_WIDTH = 1500;
	public static int JFrame_HEIGHT = 1000;

	private static JFrame buildFrame(BufferedImage img) {
		JFrame frame = new JFrame();
		JFrame_HEIGHT = img.getHeight()+42;
		JFrame_WIDTH = img.getWidth()*7/3;
		panel.setBackground(new Color(0,0,0));
		// panel.add(textArea);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(JFrame_WIDTH, JFrame_HEIGHT);
		frame.setVisible(true);
		return frame;
	}

	public static void refreshFrame(BufferedImage render) {
		panel.removeAll();
		JLabel picLabel = new JLabel(new ImageIcon(render));
		panel.add(picLabel);
		panel.setSize(picLabel.getWidth(), picLabel.getHeight());
		// panel.setLocation(new Point(panel.getX(),panel.getY()+25));
		frame.add(panel);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		frame.revalidate();
		frame.repaint();
	}

	public static void appendText(String text){
		textArea.append(text+"\n");
	}
	
	public static void blackOverlay(){
		try{
			refreshFrame(ImageIO.read(new File("imgs/descendingOverlay.png")));
		}catch(Exception e){
			System.out.println("overlay not found.");
		}
	}

	public static BufferedImage resize(BufferedImage img, int w, int h){
		Image tmp = img.getScaledInstance(w, h, Image.SCALE_REPLICATE);
		BufferedImage dimg = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g = dimg.createGraphics();
		g.drawImage(tmp,0,0,null);
		g.dispose();

		return dimg;
	}

	public static void advanceTicks(int _ticks){
		ticks++;
		Point pos = player.e.getPos();
		// System.out.println(pos.toString());
		refreshFrame(render(pos.x,pos.y));
	}

	public static void takeTurn(){
		int playerHP = player.e.HP;
		ArrayList<Entity> dead = new ArrayList<Entity>();
		for(Entity e: floors.get(currentFloor).entities.values()){
			// System.out.println(e.getPos());
			
			// TODO: add mob sleep/detection stuff
			if(!e.takeTurn()) dead.add(e);
		}
		
		for(Entity e: dead){
			e.die();
		}
		
		Point pos = player.e.getPos();
		if(player.e.HP != playerHP) System.out.println("Player HP = " + player.e.HP);
		if(player.e.HP <= 0){
			System.out.println("tough luck kiddo. you dead");
			running = false;
		}
		refreshFrame(render(pos.x,pos.y));
	}

	public static BufferedImage render(int x, int y){
		BufferedImage img = floors.get(currentFloor).render_vig(x, y, player.ViewDistance, player.Luminosity);
		img = resize(img,img.getWidth()*3,img.getHeight()*3);
		return img;
	}
}