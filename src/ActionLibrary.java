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
		pf.setEntity(e);
		// System.out.println("updating path...");
		// updatePath();
		// System.out.println("done.");
	}

	//goes reverse, therefore last point will be first
	public void updatePath(){
		boolean flag = false;
		Pathfinder.PointBFS temp;
		do {
			flag = false;
			if (player == null) System.out.println("NO PLAYERRRRRRR AHHHH");
			pBFS = pf.pathfindBFS(player.getPos(), e.getPos(), map.copyMap(), true);
			// System.out.println("done pathing");
			distance = 0;
			temp = pBFS;
			if (temp == null || temp.getParent() == null) {
				System.out.println("entity unreachable, re-placing");
				Point p = map.randomOpenSpace();
				e.x = p.x;
				e.y = p.y;
				flag = true;
			}
		} while (flag);
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

	public boolean melee(double damage_modifier){
		int dir = e.getDir(player.getPos());
		if(dir!=-1){
			// TODO: attack script
			// System.out.println("ATTACK!");
			// hits for random between 1/2 strength and 1 & 1/2 strength
			player.HP -= round(e.STRENGTH/2 + (rng.nextDouble()*e.STRENGTH) * damage_modifier - (Main.player.getDefense()/2 + (rng.nextDouble()*Main.player.getDefense())), 1);
		}
		return true;
	}

	public boolean lunge(){
		e.SP -= 3;
		// System.out.println("Lunge:");
		return (move() && melee(0.5));
	}
	
	
	// 102.456
	// 102.456 * 10 = 1024.56
	// 1024.56 -> 1025
	// 1025 / 10
	// 102.5
	
	public static double round(double n, int decimals){
		double m = Math.pow(10, decimals);
		return Math.round(n*m)/m;
	}
}
