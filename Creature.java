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
	
	// fields
	public BufferedImage sourcedItems;
	
	public double STRENGTH = 1;
	public int HP_MAX = 1;
	public int SP_MAX = 1;
	public int SP_REGEN = 1;
	public double SPEED = 1.0;
	public int EVASIVENESS = 1; // out of 10 how many are dodges.
	
	
	public BufferedImage SPRITE;
	public String NAME;
	
	// attributes
	public boolean hasAI = true;
	
	public boolean isFlying = false;
	public boolean isAmphibious = false;
	// methods
	
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
	
	public BufferedImage subImage(int x, int y){
		return sourcedItems.getSubimage(x*24, y*24+y, 24, 24);
	}
	
	private void createPlayer(){
		NAME = "player";
		SPRITE = subImage(2,6);
		
		STRENGTH = 3;
		HP_MAX = 30;
		SP_MAX = 5;
		
		hasAI = false;
	}
	
	private void createRat(){
		NAME = "rat";
		try {
			SPRITE = ImageIO.read(new File("imgs/rat.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		STRENGTH = 2;
		HP_MAX = 5;
		SP_MAX = 3;
		SPEED = 1.0;
		EVASIVENESS = 2;
	}
	
	private void createBat(){
		NAME = "bat";
		SPRITE = subImage(3,8);
		
		STRENGTH = 2;
		HP_MAX = 5;
		SP_MAX = 4;
		SPEED = 0.8;
		EVASIVENESS = 6;
		
		isFlying = true;
	}
	
//	private BufferedImage getSprite(String path){
//		
//	}
}