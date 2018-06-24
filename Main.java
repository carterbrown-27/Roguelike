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

	private static JFrame frame;
	private static JPanel panel = new JPanel();

	public static Player player;

	public static int ticks = 0;
	
	public static double lastPress = System.currentTimeMillis();
	public static double interval = 140;
	
	public static ArrayList<Map> floors = new ArrayList<Map>();
	public static Map gen;
	
	public static int currentFloor = 0;
	
	public static void main(String[] args){
		
		// Map.Room r = new Map.Room(8,8);
		// 52,90,46
		floors.add(new Map(52,90,49));
		Point ropePoint = floors.get(currentFloor).getPosition(2);
		// if(ropePoint==null) ropePoint = new Point(floors.get(currentFloor).randomOpenSpace());
		player = new Player(ropePoint.x,ropePoint.y,floors.get(currentFloor));
		
		Point t = floors.get(currentFloor).randomOpenSpace();
		floors.get(currentFloor).addEntity(new Entity(Creature.RAT,t.x,t.y,floors.get(currentFloor)));
		try{
			File output = new File("render.png");
			ImageIO.write(floors.get(currentFloor).renderMap(), "png", output);
		}catch(Exception e){};

		frame = buildFrame(render(ropePoint.x,ropePoint.y));
		
		
		// controls
		
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if (System.currentTimeMillis()-lastPress>=interval) {
					lastPress = System.currentTimeMillis();
					if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
						player.move(0);
					} else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
						if(e.isControlDown()){
							player.move(6);
						}else if(e.isShiftDown()){
							player.move(7);
						}else{
							player.move(3);
						}
					} else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
						player.move(2);
					} else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(e.isControlDown()){
							player.move(5);
						}else if(e.isShiftDown()){
							player.move(4);
						}else{
							player.move(1);
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
					}
				}
			}
		});
		
		
		
		// System.out.println(ropePoint.toString());
		refreshFrame(render(ropePoint.x,ropePoint.y));
	}
	
	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		floors.add(new Map(52,90,49));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
//		while(_ticks-->0){
//			ticks++;
//			if(ticks>=8){
//				for(int t = ticks; t >= 8; t-=8){
//					// invoke take turn method
//					refreshFrame(floors.get(currentFloor).renderMap());
//				}
//			}
//		}
		ticks++;
		Point pos = player.e.getPos();
		// System.out.println(pos.toString());
		refreshFrame(render(pos.x,pos.y));
		
	}
	
	public static BufferedImage render(int x, int y){
		BufferedImage img = floors.get(currentFloor).render_vig(x, y, player.ViewDistance, player.Luminosity);
		img = resize(img,img.getWidth()*3,img.getHeight()*3);
		return img;
	}
}