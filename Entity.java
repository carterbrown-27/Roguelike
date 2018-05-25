import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Entity {
	
	public static int x;
	public static int y;
	
	public static String name;
	public static int hp;
	
	public static Map map;
	public static BufferedImage img;
	
	public static int speed = 1;
	
	// TODO: add class Type for specific
	public static String type = "def";
	
	Entity(String _type, int _x, int _y, Map _map, BufferedImage _img){
		x = _x;
		y =_y;
		map = _map;
		img = _img;
		name = map.addEntity(this);
	}
	
	public int getX(){ return x; }
	public int getY(){ return y; }
	public static Point getPos(){ return (new Point(x,y)); }
	public String getType(){ return type; } 
	public BufferedImage getImg(){ return img; } 
	
	// u:0,r:1,d:2,l:3
	public boolean move(int dir){
		Point pos = getPos();
		boolean sxs = true;
		if(dir == 0 && map.isOpen(x-1,y)){
			x--;
		} else if(dir == 1 && map.isOpen(x,y+1)){
			y++;
		} else if(dir == 2 && map.isOpen(x+1, y)){
			x++;
		} else if(dir == 3 && map.isOpen(x, y-1)){
			y--;
		}else{
			sxs = false;
		}
		if(sxs){
			Main.advanceTicks(8/speed);
		}
		return sxs;
	}
}