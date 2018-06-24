import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public enum Creature {
	PLAYER (0),
	RAT		 (1),
	SLIME	 (2);
	
	// code
	public BufferedImage sourcedItems;
	
	public int STRENGTH;
	public BufferedImage SPRITE;
	public String NAME;
	
	Creature(int type){
		try {
			sourcedItems = ImageIO.read(new File("imgs/sourcedItems.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(type==0){
			createPlayer();
		}else if(type==1){
			createRat();
		}
//		switch(type){
//		case 0: createPlayer(); break;
//		case 1: createRat(); break;
//		}
	}
	
	private BufferedImage subImage(int x, int y){
		return sourcedItems.getSubimage(x*24, y*24+y, 24, 24);
	}
	
	private void createPlayer(){
		NAME = "player"; 
		this.STRENGTH = 3;
		this.SPRITE = subImage(2,6);
	}
	
	private void createRat(){
		NAME = "rat";
		this.STRENGTH = 2;
		this.SPRITE = subImage(3,8);
		// this.move_ability = abilities.SCURRY;
	}
	
//	private BufferedImage getSprite(String path){
//		
//	}
}