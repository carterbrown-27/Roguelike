import java.io.*;
import java.util.*;
public class MapGenerator {
	public static int height = 48; // 52
	public static int width = 67; // 90
	
	public static int randFillPercent = 46; // 46 [+4 / -3]
	public static int seed = 12345678;
	public static Random rng = new Random(seed);
	public static boolean randSeed = true;
	
	public static int[][] map = new int[height][width];
	public static int smooths = 5;
	public static char[] characters = {' ','#','Z','@',' '};
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(randSeed){
			rng = new Random();
		}
		generateMap();
		placeExits();
		printMap();
	}
	
	public static void generateMap(){
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				// if border, wall
				if(x==0||x==height-1||y==0||y==width-1){
					map[x][y] = 1;
					
				// random placement
				} else if(rng.nextInt(100)<=randFillPercent){
					map[x][y] = 1;
				}else{
					map[x][y] = 0;
				}
			}
		}
		while(smooths-->0){
			map = smoothMap();
		}
		smooths=5;
	}
	
	public static int[][] smoothMap(){
		int[][] temp = new int[height][width];
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				int adj = surroundingWallCount(x,y);
				if(adj>4) temp[x][y] = 1;
				if(adj<4) temp[x][y] = 0;
				if(adj==4) temp[x][y] = map[x][y];
			}
		}
		return temp;
	}
	
	public static int surroundingWallCount(int centerX, int centerY){
		int walls = 0;
		for(int x = centerX-1; x <= centerX+1; x++){
			for(int y = centerY-1; y <= centerY+1; y++){
				if(x>=0&&y>=0 && x<height&&y<width){
					if(x!=centerX||y!=centerY){
						walls+= map[x][y];
					}
				}else{
					walls++;
				}
			}
		}
		return walls;
	}
	
	public static void printMap(){
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				System.out.print(characters[map[x][y]]+" ");
			}
			System.out.println();
		}
	}
	
	public static boolean connectedPoints(int[][] temp, int x1, int y1, int x2, int y2){
		if(x1==x2&&y1==y2){
			return true;
		}
		if(temp[x1][y1] == 1 || temp[x1][y1] == 4) return false;
		
		temp[x1][y1] = 4;
		// right
		if(connectedPoints(temp,x1+1,y1,x2,y2)) return true;
		// up
		if(connectedPoints(temp,x1,y1+1,x2,y2)) return true;
		// left
		if(connectedPoints(temp,x1-1,y1+1,x2,y2)) return true;
		// down
		if(connectedPoints(temp,x1,y1-1,x2,y2)) return true;
		
		return false;
	}
	
	public static void placeExits(){
		int x1=0;
		int y1=0;
		int x2=0;
		int y2=0;
		
		int[][] temp = new int[height][width];
		for(int i = 0; i < height; i++){
			temp[i] = map[i];
		}
		
		int tries = 0;
		do{
			do {
				do {
					x1 = rng.nextInt(height);
					y1 = rng.nextInt(width);
				} while (map[x1][y1] == 1);
				do {
					x2 = rng.nextInt(height);
					y2 = rng.nextInt(width);
				} while (map[x2][y2] == 1);
			} while (!(Math.abs(x2-x1)+Math.abs(y2-y1)>=Math.min(Math.max(width, height)/3,Math.min(width,height))));
			tries++;
			if(tries>=100){
				generateMap();
				placeExits();
				return;
			}
		} while(!connectedPoints(temp, x1,y1,x2,y2));
		
		map[x1][y1] = 2;
		map[x2][y2] = 3;
	}

}
