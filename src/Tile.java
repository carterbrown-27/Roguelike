import java.awt.image.BufferedImage;
import java.util.Random;

public class Tile {
	
	// TODO: fully implement new systems into main
	public enum Tile_Type{
		FLOOR					(0),
		WALL					(1),
		STAIRS_UP			(2),
		STAIRS_DOWN		(3),
		TEST					(4), // temp
		DOOR					(5), // t
		WATER					(6),		
		DOOR_OPEN			(7); // t
		
		boolean isOpen = true;
		boolean isTransparent = true;
		
		Tile_Type(int t){
			if(t==1){
				isOpen = false;
				isTransparent = false;
			}else if(t==2 || t==5){
				isTransparent = false;
			}else if(t==6){
				isOpen = false;
			}
		}
		
		public boolean isOpen(){
			return isOpen;
		}
		
		public boolean isTransparent(){
			return isTransparent();
		}
	}
	
	public Tile_Type type;
	public BufferedImage image;
	public int value;
	public char icon;
	public boolean visited = false;
	public BufferedImage asLastSeen;
	
	public MapTypes maptype;
	public int adj;
	public Inventory inventory = new Inventory();
	
	
	Tile(int t, MapTypes mt, int adj){
		type = Tile_Type.values()[t];
		value = t;
		maptype = mt;
		this.adj = adj;
		image = maptype.pickImage(value, adj);
		icon = maptype.tile_characters[t];
	}
	
	public void setValue(int t){
		type = Tile_Type.values()[t];
		value = t;
		image = maptype.pickImage(value, adj);
		icon = maptype.tile_characters[t];
	}
	
	public void addItem(Item i){
		inventory.addItem(i);
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	public void setImage(BufferedImage _image){
		image = _image;
	}
	
	public void view(BufferedImage img){
		visited = true;
		asLastSeen = img;
	}
	
	public boolean isOpen(){
		return type.isOpen();
	}
	
	public boolean isTransparent(){
		return type.isTransparent();
	}

	public String name;
}