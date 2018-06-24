import java.awt.Point;
import java.awt.image.BufferedImage;
public class Entity {
	
	public int x;
	public int y;
	
	public String name;
	// public static int hp;
	
	public Map map;
	public BufferedImage img;
	
	public int speed = 1;
	
	// TODO: add class Type for specific
	
	public Creature creature;
	
	
	Entity(Creature _creature, int _x, int _y, Map _map){
		x = _x;
		y =_y;
		creature = _creature;
		img = _creature.SPRITE;
		map = _map;
		name = map.addEntity(this);
	}
	
	public int getX(){ return x; }
	public int getY(){ return y; }
	public Point getPos(){ return (new Point(x,y)); }
	public String getName(){ return creature.NAME; } 
	public BufferedImage getImg(){ return img; } 
	
	
	// u:0,r:1,d:2,l:3
	// ur: 4,rd: 5, dl: 6, lu: 7
	public boolean move(int dir){
		boolean sxs = true;
		if(dir == 0 && map.isOpen(x-1,y)){
			x--;
		} else if(dir == 1 && map.isOpen(x,y+1)){
			y++;
		} else if(dir == 2 && map.isOpen(x+1, y)){
			x++;
		} else if(dir == 3 && map.isOpen(x, y-1)){
			y--;
		} else if(dir == 4 && diagonalCheck(4)){
			x--;
			y++;
		} else if(dir == 5 && diagonalCheck(5)){
			x++;
			y++;
		} else if(dir == 6 && diagonalCheck(6)){
			x++;
			y--;
		} else if(dir == 7 && diagonalCheck(7)){
			x--;
			y--;
		}else{
			sxs = false;
		}
		if(sxs){
			Main.advanceTicks(8/speed);
		}
		return sxs;
	}
	
	public boolean diagonalCheck(int dir){
		int blocks = 0;
		if((dir == 4 || dir == 5)&& !map.isOpen(x, y+1)) blocks++;
		if((dir == 5 || dir == 6)&& !map.isOpen(x+1, y)) blocks++;
		if((dir == 6 || dir == 7)&& !map.isOpen(x, y-1)) blocks++;
		if((dir == 7 || dir == 4)&& !map.isOpen(x-1, y)) blocks++;
		
		if(blocks>=2) return false;
		
		if(dir == 4 && !map.isOpen(x-1,y+1)) return false;
		if(dir == 5 && !map.isOpen(x+1,y+1)) return false;
		if(dir == 6 && !map.isOpen(x+1,y-1)) return false;
		if(dir == 7 && !map.isOpen(x-1,y-1)) return false;
		
		return true;
	}
	
	public boolean isAdjacentTo(Point p){
		// TODO: do
		return false;
	}
}