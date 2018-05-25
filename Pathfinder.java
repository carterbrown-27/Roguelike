import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class Pathfinder {
	
	public static class PointBFS {
		int x;
		int y;
		PointBFS parent;
		
		PointBFS(int _x, int _y, PointBFS _parent){
			x = _x;
			y = _y;
			parent = _parent;
		}
		
		public PointBFS getParent(){
			return parent;
		}
		
		public Point getPos(){
			return (new Point(x,y));
		}
	}
	
	private static boolean isParentOf(PointBFS p, int x, int y){
		while(p.getParent() != null){
			if(p.x == x && p.y == y) return true;
			p = p.getParent();
		}
		return false;
	}
	
	private static Queue<PointBFS> q = new LinkedList<PointBFS>();
	public static PointBFS pathfindBFS(Point start, Point end, int[][] temp){
		q.add(new PointBFS(start.x,start.y,null));
		
		while (!q.isEmpty()) {
			PointBFS p = q.remove();
			if (p.x == end.x && p.y == end.y) {
				return p;
			}
			
			// up
			if(isOpen(p,q, p.x-1,p.y,temp)){
				temp[p.x][p.y] = -1;
				PointBFS next = new PointBFS(p.x-1,p.y,p);
				q.add(next);
			}
			// right
			if(isOpen(p,q, p.x,p.y+1,temp)){
				temp[p.x][p.y] = -1;
				PointBFS next = new PointBFS(p.x,p.y+1,p);
				q.add(next);
			}
			// down
			if(isOpen(p,q, p.x+1,p.y,temp)){
				temp[p.x][p.y] = -1;
				PointBFS next = new PointBFS(p.x+1,p.y,p);
				q.add(next);
			}
			// left
			if(isOpen(p,q, p.x,p.y-1,temp)){
				temp[p.x][p.y] = -1;
				PointBFS next = new PointBFS(p.x,p.y-1,p);
				q.add(next);
			}
		}
		
		return null;
	}
	
	private static boolean isOpen(PointBFS p, Queue<PointBFS> q,int x, int y, int[][] _map){
		// if(isParentOf(p,x,y)) return false;
		for(PointBFS i: q){
			if(i.x == x && i.y == y) return false;
		}
		if(x>=_map.length || y>=_map[x].length || x<0 || y<0) return false;
		if(_map[x][y] == 0) return true;
		return false;
	}
	
}
