import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class Player extends KeyAdapter {
	public static Entity e;
	public static BufferedImage img;
	public static Map map;
	
	public static int ViewDistance = 4;
	public static int Luminosity = 3;
	
	Player(int x, int y, Map _map){
		try{
			img = ImageIO.read(new File(MapTypes.PATH+"sourcedItems.png")).getSubimage(2*Map.tileSize, 6*Map.tileSize+6, Map.tileSize, Map.tileSize);
		}catch(Exception e){
			System.out.println("image not found.");
		};
		this.e = new Entity("player",x,y,_map,img);
	}
	
	public void move(int dir){
		e.move(dir);
	}
}
