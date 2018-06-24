import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;

public class Player extends KeyAdapter {
	public Entity e;
	public BufferedImage img;
	public Map map;
	
	public int ViewDistance = 4;
	public int Luminosity = 3;
	
	Player(int x, int y, Map _map){
		map = _map;
		this.e = new Entity(Creature.PLAYER,x,y,_map);
		map.addEntity(e);
	}
	
	public void move(int dir){
		e.move(dir);
	}
}
