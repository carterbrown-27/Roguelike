import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public enum Creature {
	PLAYER (0),
	RAT		 (1),
	SLIME	 (2), 
	BAT		 (3),
	SNAKE  (4),
	FROG	 (5),
	SPIDER (6);
	
	// code
	public BufferedImage sourcedItems;
	
	public double STRENGTH;

	public int HP_MAX;
	
	public int SP_MAX;
	public int SP_REGEN = 1;
	
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
	}
	
	private BufferedImage subImage(int x, int y){
		return sourcedItems.getSubimage(x*24, y*24+y, 24, 24);
	}
	
	private void createPlayer(){
		NAME = "player";
		this.SPRITE = subImage(2,6);
		
		this.STRENGTH = 3;
		this.HP_MAX = 20;
		this.SP_MAX = 5;
		
	}
	
	private void createRat(){
		NAME = "rat";
		this.SPRITE = subImage(3,8);
		
		this.STRENGTH = 2;
		this.HP_MAX = 7;
		this.SP_MAX = 3;
	}
	
//	private BufferedImage getSprite(String path){
//		
//	}
}