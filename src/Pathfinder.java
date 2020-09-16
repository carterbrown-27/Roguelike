import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// TODO: use global djikstra "want" maps instead of individual BFS
public abstract class Pathfinder {

	public static class PointBFS {
		private Point point;
		private int dist;
		private PointBFS parent;

		PointBFS(int x, int y, PointBFS _parent){
			this.point = new Point(x,y);
			this.parent = _parent;
			this.dist = parent != null ? _parent.getDist() + 1 : 0;
		}

		public PointBFS getParent(){
			return parent;
		}
		
		public Point getPoint() {
			return point;
		}

		public int getX() {
			return point.x;
		}

		public int getY() {
			return point.y;
		}
		
		public int getDist() {
			return dist;
		}
	}

	@SuppressWarnings("unused")
	private static boolean isParentOf(PointBFS p, int x, int y){
		while(p.getParent() != null){
			if(p.getX() == x && p.getY() == y) return true;
			p = p.getParent();
		}
		return false;
	}

	public static PointBFS pathfindBFS(Point start, Point end, Tile[][] tileMap, HashSet<Entity> entities, Creature c, boolean entityCol, int maxDist){
		Queue<PointBFS> q = new LinkedList<PointBFS>();
		q.add(new PointBFS(start.x,start.y,null));

		final int H = tileMap.length, W = tileMap[0].length;
		boolean[][] visited = new boolean[H][W];

		while (!q.isEmpty()) {
			PointBFS p = q.remove();

			if (p.getX() == end.x && p.getY() == end.y) {
				return p;
			}

			visited[p.getY()][p.getX()] = true;

			int dist = p.getDist();

			// up
			for(Direction dir: Direction.values()) {
				Point newPoint = Direction.translate(p.getPoint(), dir);
				if(dist < maxDist && !visited[newPoint.y][newPoint.x] && isOpen(newPoint, tileMap, entityCol, entities, c)){
					q.add(new PointBFS(newPoint.x, newPoint.y, p));
				}
			}
		}

		return null;
	}

	public static PointBFS pathfindBFS(Point start, Point end, Tile[][] tileMap, HashSet<Entity> entities, Creature c, boolean entityCol){
		return pathfindBFS(start, end, tileMap, entities, c, entityCol, (int) 1e8);
	}
	
	private static boolean isOpen(Point p, Tile[][] tileMap, boolean entityCol, Set<Entity> entities, Creature c){
		if(p.x<0 || p.y<0 || p.y>=tileMap.length || p.x>=tileMap[p.y].length) return false;
		if(entityCol) {
			for (Entity e : entities) {
				if (!e.equals(c) && !e.isPassable() && e.getX() == p.x && e.getY() == p.y){
					return false;
				}
			}
		}
		//		if(Main.player != null && !end.equals(Main.player.e.getPos()) && Main.player.e.getPos().equals(new Point(_x,p.y))){
		//			return false;
		//		}

		// TODO (F) fix collision checking, maybe flesh out Tile class
		// this currently checks the tile BASED ON THE CREATURE's MAP, which works for this application
		return tileMap[p.y][p.x].canOccupy(c.isFlying(), c.isAmphibious());
	}
}
