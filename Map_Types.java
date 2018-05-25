import java.io.File;


public class Map_Types {
	public static char[] tile_characters = {' ','#','Z',':','+'};
	public static File[] tiles = {// new File("Floor.png"), #0
																new File("floorReducedDark.png"),
																// new File("RedWall.png"), #1
																new File("darkWall2.png"),
																new File("ropefloor.png"),
																new File("hole_floor.png"),
																new File("floorStepsDark.png"),
																// new File("Floor.png") #4
																};
	
	public static char[] foreground_characters = {' ','@','R'};
	public static File[] foregrounds = {null, // 0
																			new File("@.png"), // 1...
																			new File("rat.png")
																			};
	
	Map_Types(String type){
		if(type.equals("Cave")){
			// nothing
		} // else
	}
}
