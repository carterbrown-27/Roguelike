import java.awt.Point;
import java.math.MathContext;
import java.util.Random;

public class ActionLibrary {
	private Entity e;
	private Pathfinder pf = new Pathfinder();
	public Entity player;
	private Map map;
	private Pathfinder.PointBFS pBFS;
	public int distance;
	private Random rng = new Random();
	
	ActionLibrary(Entity _e){
		e = _e;
		map = e.map;
		player = map.player;
		updatePath();
	}
	
	//goes reverse, therefore last point will be first
	public void updatePath(){
		if(player == null) System.out.println("NO PLAYERRRRRRR AHHHH");
		pBFS = pf.pathfindBFS(player.getPos(),e.getPos(),map.copyMap(), true);
		
		distance = 0;
		Pathfinder.PointBFS temp = pBFS;
		while(temp!=null && temp.getParent()!=null){
			distance++;
			temp = temp.getParent();
		}
	}
	
	public boolean move(){
		updatePath();
		pBFS = pBFS.getParent();
		if(pBFS!=null){
			Point p = pBFS.getPos();
			return e.move(e.getDir(p));
		}
		return false;
	}
	
	public boolean melee(){
		int dir = e.getDir(player.getPos());
		if(dir!=-1){
			// TODO: attack script
			// System.out.println("ATTACK!");
			// hits for random between 1/2 strength and 1 & 1/2 strength
			player.HP -= Math.round(e.STRENGTH/2 + (rng.nextDouble()*e.STRENGTH));
		}
		return true;
	}
	
	public boolean lunge(){
		e.SP -= 3;
		// System.out.println("Lunge:");
		return (move() && melee());
	}
}
