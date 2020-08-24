import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

// TODO (R) Replace with Tile Info & (J) JSONize other data.
public enum MapType {
	UNDERCITY ("undercity");
	
	// TODO (E) Encapsulate
	public final String PATH = "imgs/";
	public char[] tile_characters = {' ','#','Z',':','+','D','~','\''};
	
	public final String TILEMAP = "sourcedTileset_v2.1.png";
	public final String ITEMS = "sourcedItems.png";
	
	public char[] foreground_characters = {' ','H'};
	public BufferedImage[] foregroundImages = new BufferedImage[foreground_characters.length];

	public Tile[] usedTiles = new Tile[tile_characters.length];
	public BufferedImage[] defaultImages = new BufferedImage[usedTiles.length];
	
	// TODO (R) Switch from [] to ArrayList.
	private HashMap<Integer,BufferedImage[]> variations;
	private HashMap<Integer,BufferedImage[]> directionals;
	private BufferedImage[][] tilesArray;
	private HashMap<Integer,Integer> variationPercentages;
	
	// TODO (A) Create Rooms here instead of storing Room data.
	public HashMap<Integer,char[][]> precons;
	public HashMap<Integer,ArrayList<String>> precons_fg;

	MapType(String type){
		variations = new HashMap<>();
		directionals = new HashMap<>();
		variationPercentages = new HashMap<>();
		precons = new HashMap<>();
		precons_fg = new HashMap<>();
		
		if(type.equals("undercity")){
			BufferedReader br;
			try{
				br = new BufferedReader(new FileReader("DATA/precons.txt"));
				ArrayList<String> rows = new ArrayList<String>();
				
				char[][] room;
				int count = 0;
				
				String line = br.readLine();
				while(line!=null){
					if(line.equals("---") || line.equals("***")){
						room = charGridFromStrings(rows);
						precons.put(count,room);
						count++;
						rows.clear();
						if(line.equals("***")) break;
						
					}else if(line.equals("+++")){
						 room = charGridFromStrings(rows);
						 precons.put(count, room);
						 line = br.readLine();
						 ArrayList<String> fg = new ArrayList<String>();
						 while(!line.equals("---")){
							 // foreground
							 fg.add(line);
							 line = br.readLine();
						 }
						 precons_fg.put(count, fg);
						 count++;
						 rows.clear();
						 
					}else{
						rows.add(line.trim());
					}
					line = br.readLine();
				}
				
			}catch(IOException e){
				e.printStackTrace();
			}
			
			
			BufferedImage tilemap = null;
			BufferedImage itemmap = null;
			try {
				tilemap = ImageIO.read(new File(PATH+TILEMAP));
				itemmap = ImageIO.read(new File(PATH+ITEMS));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			int tileSize = 24;
			int maxW = tilemap.getWidth()/tileSize;
			int maxH = tilemap.getHeight()/tileSize;
			
			tilesArray = new BufferedImage[maxH][maxW];
			
			for(int x = 0; x < maxW; x++){
				for(int y = 0; y < maxH; y++){
					tilesArray[y][x] = tilemap.getSubimage(x*tileSize, y*tileSize, tileSize, tileSize);
				}
			}
			
			// floors
			// Tile temp = new Tile(0,tilesArray[0][0],true);
			// temp.setVariations(tilesArray[0],40);
			// usedTiles[0] = temp;
			variations.put(0, tilesArray[0]);
			// variations.put(0, Arrays.copyOfRange(tilesArray[0],0,6));
			variationPercentages.put(0, 70);
			defaultImages[0] = tilesArray[0][3];
			
			// walls
			// usedTiles[1] = new Tile(1,tilesArray[1][4],false);
			variations.put(1, new BufferedImage[] {
					tilesArray[1][2],
					tilesArray[1][3]
			});
			variationPercentages.put(1,30);
			defaultImages[1] = tilesArray[1][1];
			
			// rope stairs
			// usedTiles[2] = new Tile(2,tilesArray[1][8],true);
			defaultImages[2] = tilesArray[1][8];
			
			// hole stairs
			// usedTiles[3] = new Tile(3,tilesArray[1][8],true);[
			// usedTiles[3]= new Tile(3,tilesArray[1][7],true);
			defaultImages[3] = tilesArray[1][7];
			
			// other
			// usedTiles[4] = usedTiles[0];
			defaultImages[4] = defaultImages[0];
			// usedTiles[5] = usedTiles[0];
			defaultImages[5] = itemmap.getSubimage(0*tileSize+0, 0*tileSize, tileSize, tileSize);
			
			directionals.put(6,new BufferedImage[] {
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
					tilesArray[3][4], // 10 E
					tilesArray[3][4], // 11 E
					tilesArray[4][4], // 12
					tilesArray[3][7],
					tilesArray[3][4], // 14 E
					tilesArray[4][3]
			});
			
			// open door
			defaultImages[7] = itemmap.getSubimage(2*tileSize+2, 0*tileSize, tileSize, tileSize);
			
			// bridge
			BufferedImage vertB = itemmap.getSubimage(1*tileSize+1, 9*tileSize+9, tileSize, tileSize);
			BufferedImage horiB = itemmap.getSubimage(2*tileSize+2, 9*tileSize+9, tileSize, tileSize);
			foregroundImages[1] = vertB;
			
			
			BufferedImage[] tempDir = new BufferedImage[16];	
			tempDir[15-1] = vertB;
			tempDir[15-4] = vertB;
			tempDir[15-2] = horiB;
			tempDir[15-8] = horiB;
			tempDir[15-5] = vertB;
			tempDir[15-10] = horiB;
			directionals.put(100+1, tempDir);
 		}
	}
	
	public char[][] charGridFromStrings(ArrayList<String> rows){
		int max = 0;
		for(String s: rows){
			if(s.length() > max) max = s.length();
		}
		
		char[][] room = new char[rows.size()][max];
		
		for(int y = 0; y < rows.size(); y++){
			for(int x = 0; x < max; x++){
				room[y][x] = rows.get(y).charAt(x);
			}
		}
		
		return room;
	}
	
	public BufferedImage pickFGImage(int type, int fgAdj, int adj){
		int real = type+100;
		if(directionals.containsKey(real)){
			if(fgAdj<15){
				if(directionals.get(real)[fgAdj]!=null){
					return directionals.get(real)[fgAdj];
				}else{
					return foregroundImages[type];
				}
			}else{
				return directionals.get(real)[15-adj];
			}
		}
		return foregroundImages[type];
	}
	
	public BufferedImage pickImage(int type, int adj){
		if(variations.containsKey(type) && Main.getRng().nextInt(100)<variationPercentages.get(type)){
			return variations.get(type)[Main.getRng().nextInt(variations.get(type).length-1)];
		}else if(directionals.containsKey(type)){
			return directionals.get(type)[adj];
		}
		return defaultImages[type];
	}
}
