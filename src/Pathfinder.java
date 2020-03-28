import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public abstract class Pathfinder {

	public static class PointBFS{
		public Point point;
		private PointBFS parent;

		PointBFS(int x, int y, PointBFS _parent){
			this.point = new Point(x,y);
			this.parent = _parent;
		}

		public PointBFS getParent(){
			return parent;
		}

		public int getX() {
			return point.x;
		}

		public int getY() {
			return point.y;
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

	public static PointBFS pathfindBFS(Point start, Point end, int[][] tempMap, HashSet<Entity> entities, Creature c, boolean diagonals, boolean entityCol){
		Queue<PointBFS> q = new LinkedList<PointBFS>();
		q.add(new PointBFS(start.x,start.y,null));

		int checkedTiles = 0;
		while (!q.isEmpty() && checkedTiles < tempMap.length*tempMap[0].length) {
			checkedTiles++;
			PointBFS p = q.remove();
			if (p.getX() == end.x && p.getY() == end.y) {
				return p;
			}
			if(!isOpen(p, p.point, tempMap,entityCol, q, entities, c)){
				break;
			}

			// up
			for(Direction dir: Direction.values()) {
				Point newPoint = Direction.translate(p.point, dir);
				if(isOpen(p, newPoint, tempMap, entityCol, q, entities, c)){
					tempMap[p.getY()][p.getX()] = -1;
					q.add(new PointBFS(newPoint.x,newPoint.y,p));
				}
			}
		}

		return null;
	}

	private static boolean isOpen(PointBFS self, Point check, int[][] map, boolean entityCol, Queue<PointBFS> q,  Set<Entity> entities, Creature c){
		if (q!=null) {
			// if(isParentOf(p,x,y)) return false;
			for (PointBFS i : q) {
				if (i.getX() == check.x && i.getY() == check.y)
					return false;
			}
		}
		if(check.x<0 || check.y<0 || check.y>=map.length || check.x>=map[check.y].length) return false;
		if (entityCol) {
			for (Entity e : entities) {
				if (!e.equals((Entity) c) && !e.isPassable() && e.getX() == check.x && e.getY() == check.y){
					return false;
				}
			}
		}
		//		if(Main.player != null && !end.equals(Main.player.e.getPos()) && Main.player.e.getPos().equals(new Point(_x,check.y))){
		//			return false;
		//		}

		// TODO (F) fix collision checking, maybe flesh out Tile class
		if(map[check.y][check.x] != 1 && (map[check.y][check.x] != 2 || (c!=null && (c.isFlying() || c.isAmphibious() ))) &&
				(map[check.y][check.x] != 3 || (c!=null && c.isFlying() ))) return true;
		return false;
	}


	@SuppressWarnings("unused")
	private static boolean isOpen(PointBFS start, Direction dir, int[][] map, boolean entityCol, Queue<PointBFS> q,  Set<Entity> entities, Creature c) {
		Point check = Direction.translate(start.point, dir);
		return isOpen(start, check, map, entityCol, q, entities, c);
	}
}
