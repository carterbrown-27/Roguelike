import java.awt.image.BufferedImage;
import java.util.Random;


public class Tile {
	public BufferedImage image;
	
	public BufferedImage getImage(){
		return image;
	}
	
	public void setImage(BufferedImage _image){
		image = _image;
	}
	
	private BufferedImage[] variations;
	
	private int var_Percent = 0;
	private Random rng = new Random();
	
	public void setVariations(BufferedImage[] v, int p){
		variations = v;
		var_Percent = p;
	}
	
	public boolean open;
	
	public boolean isOpen(){
		return open;
	}
	
	public int type;
	public String name;
	public char icon;
	
	Tile(int _type, BufferedImage _image, boolean _open){
		type = _type;
		image =_image;
		open = _open;
		// icon = MapTypes.tile_characters[type];
	}
	
	
	public BufferedImage vary(){
		if(rng.nextInt(100)<var_Percent){
			return variations[rng.nextInt(variations.length-1)];
		}
		return image;
	}
}
