import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Pathfinder {

	private Creature c;
	private HashSet<Entity> entities = new HashSet<>();

	private Queue<PointBFS> q;

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

	public void setEntity(Creature _c){
		this.c = _c;
	}

	public PointBFS pathfindBFS(Point start, Point end, int[][] tempMap, HashSet<Entity> entities, boolean diagonals, boolean entityCol){
		this.entities = entities;
		q = new LinkedList<PointBFS>();
		q.add(new PointBFS(start.x,start.y,null));

		int checkedTiles = 0;
		while (!q.isEmpty() && checkedTiles < tempMap.length*tempMap[0].length) {
			checkedTiles++;
			PointBFS p = q.remove();
			if (p.getX() == end.x && p.getY() == end.y) {
				return p;
			}
			if(!isOpen(p,p.point,tempMap,entityCol)){
				break;
			}

			// up
			for(Direction dir: Direction.values()) {
				Point newPoint = Direction.translate(p.point, dir);
				if(isOpen(p,newPoint,tempMap,entityCol)){
					tempMap[p.getY()][p.getX()] = -1;
					q.add(new PointBFS(newPoint.x,newPoint.y,p));
				}
			}
		}

		return null;
	}

	private boolean isOpen(PointBFS self, Point check, int[][] map, boolean entityCol){
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
				if (!e.equals((Entity) this.c) && !e.isPassable && e.getX() == check.x && e.getY() == check.y){
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
	private boolean isOpen(PointBFS start, Direction dir, int[][] map, boolean entityCol) {
		Point check = Direction.translate(start.point, dir);
		return isOpen(start,check,map,entityCol);
	}
}
