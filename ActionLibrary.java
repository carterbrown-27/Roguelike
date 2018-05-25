import java.awt.Point;

public class ActionLibrary {
	private static Entity e;
	private static Pathfinder pf = new Pathfinder();
	public static Player player = Main.player;
	private static Map map;
	private static Pathfinder.PointBFS pBFS;
	
	ActionLibrary(Entity _e){
		e = _e;
		map = e.map;
		
		// goes reverse, therefore last point will be first
		pBFS = pf.pathfindBFS(player.e.getPos(),e.getPos(),map.copyMap());
	}
	
	public static boolean move(){
		Point p = pBFS.getPos();
		return e.move(getDir(p));
	}
	
	public static boolean melee(){
		int dir = getDir(player.e.getPos());
		if(dir!=-1){
			// TODO: attack script
		}
		return true;
	}
	
	
	public static int getDir(Point d){
		if(d.x==e.getX()-1) return 0;
		if(d.y==e.getY()+1) return 1;
		if(d.x==e.getX()+1) return 2;
		if(d.y==e.getY()-1) return 3;
		return -1;
	}
	
}
