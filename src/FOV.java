import java.awt.Point;
import java.util.ArrayList;


public class FOV {

	/*** @SquidLib interpretation
	 * thanks to them, link here:
	 * <github link here> ***/

	public enum DIAGONAL {
		UP_LEFT		(-1,-1),
		UP_RIGHT	(+1,-1),
		DOWN_RIGHT(+1,+1),
		DOWN_LEFT (-1,+1);

		/**
		 * UP = -y
		 * DOWN = +y
		 * LEFT = -x
		 * RIGHT = +x
		 **/
		int deltaX;
		int deltaY;

		DIAGONAL (int x, int y){
			this.deltaX = x;
			this.deltaY = y;
		}
	}

	/** true = opaque **/
	boolean[][] opacityMap;
	boolean[][] lightMap;
	int startX;
	int startY;
	int width;
	int height;
	int radius;

	public boolean[][] calculate(boolean[][] opacityMap, int startX, int startY, int radius){
		this.opacityMap = opacityMap;
		this.startX = startX;
		this.startY = startY;
		this.height = opacityMap.length;
		this.width = opacityMap[0].length;
		this.radius = radius;
		
		this.lightMap = new boolean[height][width];

		lightMap[startY][startX] = true;
		for(DIAGONAL d: DIAGONAL.values()){
			// does both directions from the diagonal
			// every direction's start is 1, end is 0, other args are multipliers
			castLight(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0);
			castLight(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY);
		}

		return lightMap;
	}


	
	
	/* this orientation
	 * 
	 * 	.	.	.	.	3
	 * 		.	.	. 2
	 * 			.	.	1
	 * 				@
	 */
	public void castLight(int row, double start, double end, int xx, int xy, int yx, int yy){
		double newStart = 0.0f;
		if (start < end) {
			return;
		}
		boolean blocked = false;
		// while the row# is less then viewdistance, and the prev. row didn't end on a blocked tile, row#++
		for (int distance = row; distance <= radius && !blocked; distance++) {
			// deltaY is how far away from @
			int deltaY = -distance;
			// distance always = the number of tiles "to the left" from @ in a row, therefore go from -3 to 0 if on row 3
			for (int deltaX = -distance; deltaX <= 0; deltaX++) {
				
				// multipliers change the +/- of the numbers to simulate direction shown in diagram
				int currentX = startX + deltaX * xx + deltaY * xy;
				int currentY = startY + deltaX * yx + deltaY * yy;
				
				// calculates the slopes
				double leftSlope = (deltaX - 0.5) / (deltaY + 0.5);
				double rightSlope = (deltaX + 0.5) / (deltaY - 0.5);
				
				// if in bounds continue
				if (!(currentX >= 0 && currentY >= 0 && currentX < this.width && currentY < this.height) || start < rightSlope) {
					continue;
				} else if (end > leftSlope) {
					break;
				}

				// light space if reached
				lightMap[currentY][currentX] = true;

				if (blocked) { //previous cell was a blocking one
					if (!opacityMap[currentY][currentX]) {//hit a wall
						newStart = rightSlope;
						continue;
					} else {
						blocked = false;
						start = newStart;
					}
				} else {
					if (!opacityMap[currentY][currentX] && distance < radius){ //hit a wall within sight line
						blocked = true;
						// recur
						castLight(distance + 1, start, leftSlope, xx, xy, yx, yy);
						newStart = rightSlope;
					}
				}
			}
		}
	}
	
	public static ArrayList<Point> bresenhamLine(Point start, Point end){
		ArrayList<Point> linePoints = new ArrayList<Point>();
		
		// TODO: float -> int optimization
		int x1 = start.x;
		int x2 = end.x;
		int y1 = start.y;
		int y2 = end.y;
		
		int rise = y2-y1;
		int run = x2-x1;
		
		// vertical line
		if(run == 0){
			// if start is greater than end, switch the two.
			if(y2 < y1){
				int temp = y1;
				y1 = y2;
				y2 = temp;
			}
			
			for(int y = y1; y <= y2; y++){
				linePoints.add(new Point(x1,y));
			}
		}else{
			float slope = (float) rise / run;
			int adjust = 1;
			if(slope<0) adjust = -1;
			
			float offset = 0;
			float threshold = 0.5f;
			
			// less than y = x;
			/* ....
			 * ....
			 * ..xx shifts y value
			 * xx..
			 */
			if(Math.abs(slope) <= 1){
				float delta = Math.abs(slope);
				int y = y1;
				if(x2 < x1){
					int temp = x1;
					x1 = x2;
					x2 = temp;
					y = y2;
				}
				
				for(int x = x1; x <= x2; x++){
					linePoints.add(new Point(x,y));
					offset += delta;
					
					if(offset >= threshold){
						y += adjust;
						threshold++;
					}
				}
			// greater than y = x;
			/* x....
			 * x....
			 * .x... shifts x value
			 * .x...
			 */
			}else{
				float delta = Math.abs((float) run / rise);
				int x = x1;
				if(y2 < y1){
					int temp = y1;
					y1 = y2;
					y2 = temp;
					x = x2;
				}
				
				for(int y = y1; y <= y2; y++){
					linePoints.add(new Point(x,y));
					offset+=delta;
					
					if(offset >= threshold){
						x += adjust;
						threshold++;
					}
				}
			}
		}
		
		return linePoints;
		//return (Point[]) linePoints.toArray();
	}
}
