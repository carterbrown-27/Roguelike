import java.awt.*;
import java.awt.image.BufferedImage;

// TODO (X) Overhaul: Rebuild from scratch
public class Tile extends GameObject{
	public enum TerrainType {
		FLOOR,
		WALL,
		WATER,
		FLYABLE_ONLY,
		SWIM_ONLY,
		DIFFICULT,
		STAIRS
	}

	// TEMP
	public enum TileType{
		FLOOR,
		WALL,
		STAIRS_UP,
		STAIRS_DOWN,
		TEST, // temp
		DOOR, // temp
		WATER,
		DOOR_OPEN; // temp
	}

	public enum Fg_Type{
		NONE,
		BRIDGE;
	}

	// TODO: Private these
	public TileType type;
	public Fg_Type fgtype = Fg_Type.NONE;
	public TerrainType terrain;
	public Point pos;
	public BufferedImage image;
	public int value;
	public char icon;

	public boolean visited = false;
	public BufferedImage asLastSeen;
	public Entity lastEntityHere;
	public Entity entityHere;
	
	public MapType maptype;
	public int adj;
	public Inventory inventory = new Inventory();

	public String name;
	public int fgAdj;

	Tile(int t, MapType mt, int adj){
		this.type = TileType.values()[t];
		this.value = t;
		this.maptype = mt;
		this.adj = adj;
		this.image = maptype.pickImage(value, adj);
		this.icon = maptype.tile_characters[t];
	}

	public void setValue(int t){
		this.type = TileType.values()[t];
		this.value = t;
		this.image = maptype.pickImage(value, adj);
		this.icon = maptype.tile_characters[t];
	}

	public void setForeground(Fg_Type f, int fgAdj){
		this.fgtype = f;
		this.fgAdj = fgAdj;
	}

	public void setForeground(Fg_Type f){
		fgtype = f;
	}
	
	public BufferedImage getImage(){
		return image;
	}

	public BufferedImage getFgImage(){
		if(fgtype == Fg_Type.NONE) return null;
		return maptype.pickFGImage(fgtype.ordinal(), fgAdj, adj);
	}

	public void view(BufferedImage img, Entity e){
		visited = true;
		asLastSeen = img;
		lastEntityHere = e;
	}

	public boolean canOccupy(boolean flying, boolean amphib){
		// TODO (T) TEMP, move to TerrainType
		return !(type == TileType.WALL || type == TileType.WATER) || (type == TileType.WATER && ((flying || amphib || fgtype == Fg_Type.BRIDGE)));
	}
}