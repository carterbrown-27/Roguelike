import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class GameObject {
	private static BufferedImage sourcedItems;
	
	protected static BufferedImage getSourcedItems() {
		return sourcedItems;
	}
	
	protected static BufferedImage subImage(int x, int y){
		if(sourcedItems==null){
			try {
				sourcedItems = ImageIO.read(new File("imgs/sourcedItems.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sourcedItems.getSubimage(x*24+x, y*24+y, 24, 24);
	}
}
