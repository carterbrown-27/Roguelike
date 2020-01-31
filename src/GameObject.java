import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public abstract class GameObject {
	private static HashMap<SpriteSource,BufferedImage> imageSources = new HashMap<>();
	private BufferedImage sprite;

	GameObject(){
		init();
	}

	protected static BufferedImage getSource(SpriteSource source) {
		return imageSources.get(source);
	}

	// TODO (+) optimize, frontload all loading and subimaging.
	protected static BufferedImage subImage(SpriteSource source, int x, int y){
		return getSource(source).getSubimage(x*24+x, y*24+y, 24, 24);
	}
	
	private static void init() {
		loadImages();
	}
	
	public BufferedImage getSprite() {
		return sprite;
	}
	
	public void setSprite(BufferedImage _sprite) {
		this.sprite = _sprite;
	}
	
	public void setSprite(SpriteSource source, int x, int y) {
		this.setSprite(subImage(source,x,y));
	}

	private static void loadImages() {
		for(SpriteSource src: SpriteSource.values()) {
			if(!imageSources.containsKey(src)) {
				try {
					imageSources.put(src,ImageIO.read(new File(src.fileLocation)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static enum SpriteSource {
		DEFAULT ("sourcedItems.png");
		
		private static final String path = "imgs/";
		public String fileLocation;
		
		SpriteSource(String fileName){
			this.fileLocation = path + fileName;
		}
	}
}
