import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;


public class MapTypes {
	public final String PATH = "imgs/";
	public char[] tile_characters = {' ','#','Z',':','+','D','~'};
	public File[] tileFiles = {// new File("Floor.png"), #0
																new File(PATH+"floorReducedDark.png"),
																// new File("RedWall.png"), #1
																new File(PATH+"darkWall2.png"),
																new File(PATH+"ropefloor.png"),
																new File(PATH+"hole_floor.png"),
																new File(PATH+"floorStepsDark.png"),
																new File(PATH+"RedWall.png")
																// new File("Floor.png") #4
																};
	
	public char[] foreground_characters = {' ','@','R'};
	public File[] foregrounds = {null, // 0
																			new File(PATH+"@.png"), // 1...
																			new File(PATH+"rat.png")
																			};
	
	public BufferedImage[] tiles = new BufferedImage[tileFiles.length];
	public Tile[] usedTiles = new Tile[tile_characters.length];
	public BufferedImage[] usedImages = new BufferedImage[usedTiles.length];
	
	public HashMap<Integer,BufferedImage[]> variations = new HashMap<Integer,BufferedImage[]>();
	public HashMap<Integer,BufferedImage[]> directionals = new HashMap<Integer,BufferedImage[]>();
	public BufferedImage[][] tilesArray = null;
	public HashMap<Integer,Integer> variationPercentages = new HashMap<Integer,Integer>();
	
	
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
			
			int tileSize = 24;
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
			variationPercentages.put(0, 70);
			usedImages[0] = tilesArray[0][1];
			
			// walls
			// usedTiles[1] = new Tile(1,tilesArray[1][4],false);
			BufferedImage[] temp = {tilesArray[1][2],tilesArray[1][3]};
			variations.put(1,temp);
			variationPercentages.put(1,30);
			usedImages[1] = tilesArray[1][1];
			
			// rope stairs
			usedTiles[2] = new Tile(2,tilesArray[1][8],true);
			usedImages[2] = tilesArray[1][8];
			
			// hole stairs
			// usedTiles[3] = new Tile(3,tilesArray[1][8],true);[
			// usedTiles[3]= new Tile(3,tilesArray[1][7],true);
			usedImages[3] = tilesArray[1][7];
			
			// other
			// usedTiles[4] = usedTiles[0];
			usedImages[4] = usedImages[0];
			// usedTiles[5] = usedTiles[0];
			usedImages[5] = itemmap.getSubimage(1*tileSize+1, 0*tileSize, tileSize, tileSize);
			
			BufferedImage[] temp6 = {
					tilesArray[3][5], // 0
					tilesArray[4][1], // 1
					tilesArray[3][6], // 2
					tilesArray[4][2], 
					tilesArray[4][5], // 4
					tilesArray[3][8],
					tilesArray[4][6],
					tilesArray[3][9],
					tilesArray[3][4], // 8
					tilesArray[4][0],
					null, // 10
					null, // 11
					tilesArray[4][4], // 12
					tilesArray[3][7],
					null, // 14
					tilesArray[4][3]
			};
			
			directionals.put(6,temp6);
		}
	}
	
	public BufferedImage pickImage(int type, int adj){
		if(variations.containsKey(type) && rng.nextInt(100)<variationPercentages.get(type)){
			return variations.get(type)[rng.nextInt(variations.get(type).length-1)];
		}else if(directionals.containsKey(type)){
			return directionals.get(type)[adj];
		}
		return usedImages[type];
	}
	
	public Tile getTile(int type){
		return new Tile(type,usedTiles[type].image,usedTiles[type].isOpen());
	}
}
