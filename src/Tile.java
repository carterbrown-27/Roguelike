import java.awt.image.BufferedImage;
import java.util.Random;


public class Tile {
	public static BufferedImage image;
	
	public static BufferedImage getImage(){
		return image;
	}
	
	public static void setImage(BufferedImage _image){
		image = _image;
	}
	
	private static BufferedImage[] variations;
	
	private static int var_Percent = 0;
	private static Random rng = new Random();
	
	public static void setVariations(BufferedImage[] v, int p){
		variations = v;
		var_Percent = p;
	}
	
	public static boolean open;
	
	public static boolean isOpen(){
		return open;
	}
	
	public static int type;
	public static String name;
	public static char icon;
	
	Tile(int _type, BufferedImage _image, boolean _open){
		type = _type;
		image =_image;
		open = _open;
		icon = MapTypes.tile_characters[type];
	}
	
	
	public static BufferedImage vary(){
		if(rng.nextInt(100)<var_Percent){
			return variations[rng.nextInt(variations.length-1)];
		}
		return image;
	}
}
