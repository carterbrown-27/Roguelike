import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;


public class MapTypes {
	public static final String PATH = "imgs/";
	public static char[] tile_characters = {' ','#','Z',':','+','D'};
	public static File[] tileFiles = {// new File("Floor.png"), #0
																new File(PATH+"floorReducedDark.png"),
																// new File("RedWall.png"), #1
																new File(PATH+"darkWall2.png"),
																new File(PATH+"ropefloor.png"),
																new File(PATH+"hole_floor.png"),
																new File(PATH+"floorStepsDark.png"),
																new File(PATH+"RedWall.png")
																// new File("Floor.png") #4
																};
	
	public static char[] foreground_characters = {' ','@','R'};
	public static File[] foregrounds = {null, // 0
																			new File(PATH+"@.png"), // 1...
																			new File(PATH+"rat.png")
																			};
	
	public static BufferedImage[] tiles = new BufferedImage[tileFiles.length];
	public static Tile[] usedTiles = new Tile[tile_characters.length];
	public static BufferedImage[] usedImages = new BufferedImage[usedTiles.length];
	public static HashMap<Integer,BufferedImage[]> variations = new HashMap<Integer,BufferedImage[]>();
	public static BufferedImage[][] tilesArray = null;
	public static HashMap<Integer,Integer> variationPercentages = new HashMap<Integer,Integer>();
	
	
	private static Random rng = new Random();
	
	// TEMPORARY
	
//	public static final int defaultFloorPos = 18;
//	public static final int defaultWallPos = 25;
	
	MapTypes(String type){
		
		if(type.equals("sourcedCave")){
			BufferedImage tilemap = null;
			BufferedImage itemmap = null;
			try {
				tilemap = ImageIO.read(new File(PATH+"sourcedTileset.png"));
				itemmap = ImageIO.read(new File(PATH+"sourcedItems.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int tileSize = Map.tileSize;
			int mw = tilemap.getWidth()/tileSize;
			int mh = tilemap.getHeight()/tileSize;
			
			tilesArray = new BufferedImage[mh][mw];
			
			for(int x = 0; x < mw; x++){
				for(int y = 0; y < mh; y++){
					tilesArray[y][x] = tilemap.getSubimage(x*tileSize, y*tileSize, tileSize, tileSize);
				}
			}
			
			// floors
			// Tile temp = new Tile(0,tilesArray[0][0],true);
			// temp.setVariations(tilesArray[0],40);
			// usedTiles[0] = temp;
			variations.put(0, tilesArray[0]);
			variationPercentages.put(0, 60);
			usedImages[0] = tilesArray[0][1];
			
			// walls
			// usedTiles[1] = new Tile(1,tilesArray[1][4],false);
			usedImages[1] = tilesArray[1][4];
			
			// down stairs
			usedTiles[2] = new Tile(2,tilesArray[1][7],true);
			usedImages[2] = tilesArray[1][7];
			
			// up stairs
			// usedTiles[3] = new Tile(3,tilesArray[1][8],true);
			// usedTiles[3]= new Tile(3,tilesArray[1][7],true);
			usedImages[3] = tilesArray[1][7];
			
			// other
			// usedTiles[4] = usedTiles[0];
			usedImages[4] = usedImages[0];
			// usedTiles[5] = usedTiles[0];
			usedImages[5] = itemmap.getSubimage(1*tileSize+1, 0*tileSize, tileSize, tileSize);
		}
	}
	
	public static BufferedImage pickImage(int type){
		if(variations.containsKey(type) && rng.nextInt(100)<variationPercentages.get(type)){
			return variations.get(type)[rng.nextInt(variations.get(type).length-1)];
		}
		return usedImages[type];
	}
	
	public static Tile getTile(int type){
		return new Tile(type,usedTiles[type].image,usedTiles[type].isOpen());
	}
}
