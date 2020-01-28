import java.awt.Point;

public enum Direction {
	UP 				(0,-1),
	RIGHT 			(+1,0),
	DOWN 			(0,+1),
	LEFT			(-1,0),
	UP_RIGHT		(+1,-1),
	DOWN_RIGHT		(+1,+1),
	DOWN_LEFT		(-1,+1),
	UP_LEFT			(-1,-1);

	Point p;

	Direction(int x, int y){
		p = new Point(x,y);
	}
	
	public int getX() {
		return p.x;
	}
	
	public int getY() {
		return p.y;
	}
	
	public static Point translate(Point p, Direction dir) {
		Point newPoint = new Point(p);
		newPoint.translate(dir.getX(), dir.getY());
		return newPoint;
	}
}