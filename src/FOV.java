
public class FOV {

	/*** @SquidLib interpretation ***/

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
}
