import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class GameObject {
	private static BufferedImage sourcedItems;
	private BufferedImage sprite;

	GameObject(){
		init();
	}

	protected static BufferedImage getSourcedItems() {
		return sourcedItems;
	}

	protected static BufferedImage subImage(int x, int y){
		init();
		return sourcedItems.getSubimage(x*24+x, y*24+y, 24, 24);
	}
	
	private static void init() {
		if(sourcedItems==null) loadImages();
	}
	
	public BufferedImage getSprite() {
		return sprite;
	}
	
	public void setSprite(BufferedImage _sprite) {
		this.sprite = _sprite;
	}
	
	public void setSprite(int x, int y) {
		this.setSprite(subImage(x,y));
	}

	private static void loadImages() {
		try {
			sourcedItems = ImageIO.read(new File("imgs/sourcedItems.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
