/** @author hillstylelife **/

import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Main {
	//	public static int seed = 12345678;
	public static Random rng = new Random();
	//	public static boolean randSeed = true;
	//	public static boolean render = true;

	public static boolean running = true;

	private static JFrame frame;
	private static JPanel panel = new JPanel();
	private static JTextArea textArea = new JTextArea();

	public static Player player;

	public static int ticks = 0;

	public static double lastPress = System.currentTimeMillis();
	public static double interval = 100;
	public static boolean itemSelection = false;
	public static boolean inventoryScreen = false;
	public static boolean itemScreen = false;

	public static Item selectedItem;

	public static ArrayList<Map> floors = new ArrayList<Map>();
	public static Map gen;

	public static int currentFloor;
	public static Point ropePoint;

	public static final int map_h = 60;
	public static final int map_w = 90;
	public static final int map_fill = 49;

	public static HashMap<Item.Items,String> randomNames = new HashMap<Item.Items,String>();

	public static void main(String[] args){

		// Map.Room r = new Map.Room(8,8);
		// 52,90,46
		int seed = rng.nextInt(Integer.MAX_VALUE);
		// seed = 2119737813;
		rng = new Random(seed);
		startGame();
		System.out.println(seed);

		// controls

		/* y	(k/ua)	u
		 *(h/la) . (l/ra)
		 * b	(d/da) n
		 * or shift/ctrl+l/r 
		 * 
		 * 7 0 4
		 * 3 . 1
		 * 6 2 5
		 */

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if(running){
					txt.clear(); /** TEMP **/
					refreshText();
					if (System.currentTimeMillis()-lastPress>=interval) {
						lastPress = System.currentTimeMillis();
						if(itemSelection){
							for (char c = 'a'; c <= 'z'; c++) {
								if (e.getKeyChar() == c && floors.get(currentFloor).tileMap[player.e.y][player.e.x].inventory.inv.containsKey(c)) {
									player.pickUp(c);
								}
							}
							itemSelection = false;
						}else if(inventoryScreen){
							boolean selected = false;
							for (char c = 'a'; c <= 'z'; c++) {
								if (e.getKeyChar() == c && player.inv.inv.containsKey(c)) {
									selected = true;
									// TODO: open item menu
									/** temporary **/
									Item i = player.inv.inv.get(c);

									if(randomNames.containsKey(i.type)){
										appendText(randomNames.get(i.type)+" selected.");
									}else{
										appendText(i.name+" selected.");										
									}
									appendText("Do what with this item?");

									if(i.type.supertype.equals(Item.Items.Item_Supertype.WEAPON)){
										if(!i.weilded){
											appendText("(w)ield");
										}else{
											appendText("(u)nweild");
										}
									}else if(i.type.supertype.equals(Item.Items.Item_Supertype.ARMOUR)){
										if(!i.worn){
											appendText("(p)ut on");
										}else{
											appendText("(t)ake off");
										}
									}else if(i.type.supertype.equals(Item.Items.Item_Supertype.SCROLL)){
										appendText("(r)ead");
									}else if(i.type.supertype.equals(Item.Items.Item_Supertype.FOOD)){
										appendText("(e)at");
									}else if(i.type.supertype.equals(Item.Items.Item_Supertype.POTION)){
										appendText("(q)uaff");
									}else if(i.type.supertype.equals(Item.Items.Item_Supertype.MISSILE)){
										appendText("(q)uiver");
									}
									appendText("(d)rop\n(r)eassign\n(ESC) exit");
									selectedItem = i;
								}
							}
							if(selected) itemScreen = true;
							inventoryScreen = false;
						}else if(itemScreen){
							Item i = selectedItem;
							if(i.type.supertype.equals(Item.Items.Item_Supertype.WEAPON)){
								if(!i.weilded && e.getKeyChar() == 'w'){
									player.weild(i);
								}else if(i.weilded && e.getKeyChar() == 'u'){
									player.unweild(i);
								}
							}else if(i.type.supertype.equals(Item.Items.Item_Supertype.ARMOUR)){
								if(!i.worn && e.getKeyChar() == 'p'){
									player.putOn(i);
								}else if(i.worn && e.getKeyChar() == 't'){
									player.takeOff(i);
								}
							}else if(i.type.supertype.equals(Item.Items.Item_Supertype.SCROLL)){
								if(e.getKeyChar() == 'r'){

								}
							}else if(i.type.supertype.equals(Item.Items.Item_Supertype.MISSILE)){
								if(e.getKeyChar() == 'q'){

								}								
							}
							itemScreen = false;

						}else if (e.getKeyCode() == KeyEvent.VK_K || e.getKeyCode() == KeyEvent.VK_UP) {
							player.act_adj(0);
						} else if (e.getKeyCode() == KeyEvent.VK_H || e.getKeyCode() == KeyEvent.VK_LEFT) {
							if(e.isControlDown()){
								player.act_adj(6);
							}else if(e.isShiftDown()){
								player.act_adj(7);
							}else{
								player.act_adj(3);
							}
						} else if (e.getKeyCode() == KeyEvent.VK_J || e.getKeyCode() == KeyEvent.VK_DOWN) {
							player.act_adj(2);
						} else if (e.getKeyCode() == KeyEvent.VK_L || e.getKeyCode() == KeyEvent.VK_RIGHT) {
							if(e.isControlDown()){
								player.act_adj(5);
							}else if(e.isShiftDown()){
								player.act_adj(4);
							}else{
								player.act_adj(1);
							}
						}else if(e.getKeyCode() == KeyEvent.VK_Y){
							player.act_adj(7);
						}else if(e.getKeyCode() == KeyEvent.VK_U){
							player.act_adj(4);
						}else if(e.getKeyCode() == KeyEvent.VK_B){
							player.act_adj(6);
						}else if(e.getKeyCode() == KeyEvent.VK_N){
							player.act_adj(5);
						}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
							appendText("Current Floor"+currentFloor);
							if(floors.get(currentFloor).valueAt(player.e.getPos()) == 3){
								if(floors.size()<=currentFloor+1){
									newFloor();
								}else{
									changeFloor(currentFloor+1,true,false);
								}
							}else if(floors.get(currentFloor).valueAt(player.e.getPos()) == 2 && currentFloor > 0){
								// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
								changeFloor(currentFloor-1,false,false);
							}

						} else if(e.getKeyCode() == KeyEvent.VK_P){
							floors.get(currentFloor).printMap();

						} else if(e.getKeyCode() == KeyEvent.VK_SPACE){
							// open attack selections
							// area.append("space.\n");
							// if already open attack
						} else if(e.getKeyCode() == KeyEvent.VK_G){
							// get
							Inventory tileInv = floors.get(currentFloor).tileMap[player.e.y][player.e.x].inventory;
							if(tileInv.isOneItem()){
								player.pickUp('a');
							}else if(!tileInv.isEmpty()){
								itemSelection = true;
								System.out.println("Pick up what?");
							}

						}else if(e.getKeyCode() == KeyEvent.VK_I){
							appendText("(Your Inventory):");
							player.inv.printContents();

							if(!player.inv.isEmpty()) inventoryScreen = true;
							// TODO: inventory screen
						}else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
							txt.clear();
							refreshText();
						}else{
							for(Player.Ability a: Player.Ability.values()){
								if(e.getKeyCode() == a.k){
									if(player.e.SP >= a.s){
										player.select(a);
									}else{
										appendText("Not enough stamina (stamina!).");
										player.deselect();
									}
									break;
								}
							}
						}

					}
				}else if(e.getKeyCode() == KeyEvent.VK_SPACE){
					// restart
					startGame();
				}
			}
		});
		// System.out.println(ropePoint.toString());
		refreshFrame(render(ropePoint.x,ropePoint.y));
	}

	public static void startGame(){
		floors.clear();
		currentFloor = 0;

		floors.add(new Map(map_h,map_w,map_fill,rng));
		ropePoint = floors.get(currentFloor).getPosition(2);

		player = new Player(ropePoint.x,ropePoint.y,floors.get(currentFloor));

		for(Item.Items i: Item.Items.scrolls){
			randomNames.put(i, "scroll(s) labeled "+((randomName()+" "+randomName()).toUpperCase()));
		}

		/** temporary **/
		int mobs = rng.nextInt(6)+8;
		for (int i = 0; i < mobs; i++) {
			Point t = floors.get(currentFloor).randomOpenSpace();
			System.out.println("point picked");
			new Entity(Creature.randomType(), t.x, t.y, floors.get(currentFloor));
			System.out.println("Entity Added.");
		}

		int items = rng.nextInt(6)+28; // 6+8
		for(int i = 0; i < items; i++){
			Point t = floors.get(currentFloor).randomOpenSpace();
			Item.Items ty = Item.Items.randomItemType(currentFloor);
			System.out.println("adding "+ty);
			floors.get(currentFloor).tileMap[t.y][t.x].inventory.addItem(new Item(ty,1));
		}


		try{
			File output = new File("render.png");
			ImageIO.write(floors.get(currentFloor).renderMap(), "png", output);
		}catch(Exception e){};

		if(frame==null){
			frame = buildFrame(render(ropePoint.x,ropePoint.y));
		}else{
			refreshFrame(render(ropePoint.x,ropePoint.y));
		}

		running = true;
	}

	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		floors.add(new Map(map_h,map_w,map_fill,rng));
		System.out.println(floors.size()+" total");
		changeFloor(currentFloor+1,true,true);
	}

	public static void changeFloor(int floor, boolean down, boolean isNew){
		player.e.HP = player.e.creature.HP_MAX;
		currentFloor = floor;

		Point startPoint= floors.get(currentFloor).getPosition(2);
		if(!down) startPoint= floors.get(currentFloor).getPosition(3);
		player.map = floors.get(currentFloor);
		floors.get(currentFloor).player = player.e;
		player.e.x = startPoint.x;
		player.e.y = startPoint.y;
		player.e.map = floors.get(currentFloor);
		// floors.get(currentFloor).player = player.e;

		if(isNew){

			/** TEMPORARY **/
			int mobs = rng.nextInt(6)+8;
			for (int i = 0; i < mobs; i++) {
				Point t = floors.get(currentFloor).randomOpenSpace();
				new Entity(Creature.RAT, t.x, t.y, floors.get(currentFloor));
			}

			int items = rng.nextInt(6)+8;
			for(int i = 0; i < items; i++){
				Point t = floors.get(currentFloor).randomOpenSpace();
				System.out.println("adding item #"+i);
				floors.get(currentFloor).tileMap[t.y][t.x].inventory.addItem(new Item(Item.Items.randomItemType(currentFloor),1));
			}
		}

		refreshFrame(render(startPoint.x,startPoint.y));
	}

	public static int JFrame_WIDTH = 1500;
	public static int JFrame_HEIGHT = 1000;

	private static JFrame buildFrame(BufferedImage img) {
		//		GUI gui = new GUI();
		//		gui.run();

		JFrame frame = new JFrame();
		JFrame_HEIGHT = img.getHeight()+42;
		JFrame_WIDTH = Math.min(img.getWidth()*7/3,1600);
		frame.setIconImage(Creature.PLAYER.SPRITE);
		
		panel.setLayout(new BorderLayout());
		area.setBackground(new Color(0,0,0));
		area.setFont(f);
		area.setForeground(Color.white);
		appendText("@");
		refreshText();
		frame.setTitle("Roguelike");
		panel.setBackground(new Color(0,0,0));
		panel.add(area,BorderLayout.EAST);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(JFrame_WIDTH, JFrame_HEIGHT);
		frame.setVisible(true);
		return frame;
	}

	public static Font f = new Font("Serif",Font.BOLD,20);
	public static JTextArea area = new JTextArea();
	public static ArrayList<String> txt = new ArrayList<String>();
 
	public static void refreshFrame(BufferedImage render) {
		panel.removeAll();
		JLabel picLabel = new JLabel(new ImageIcon(render));
		panel.add(picLabel,BorderLayout.WEST);
		panel.setSize(picLabel.getWidth(), picLabel.getHeight());
		// panel.setLocation(new Point(panel.getX(),panel.getY()+25));

		panel.add(area);
		frame.add(panel);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		frame.revalidate();
		frame.repaint();
	}

	public static void appendText(String text){
		String[] strArray = text.split("\\r?\\n");
		for(String str: strArray){
			txt.add(str);
		}
		refreshText();
	}
	
	public static final int rows = 15;
	public static void refreshText(){
		area.setText("");
		while (txt.size()>rows){
			txt.remove(0);
		}
		for(int i = 0; i < txt.size()-1; i++){
			area.append(txt.get(i)+"\n"+"\n");
		}
		
		if(txt.size()>=1) area.append(txt.get(txt.size()-1));
	}

	public static void blackOverlay(){
		try{
			refreshFrame(ImageIO.read(new File("imgs/descendingOverlay.png")));
		}catch(Exception e){
			System.out.println("overlay not found.");
		}
	}

	public static BufferedImage resize(BufferedImage img, int w, int h){
		Image tmp = img.getScaledInstance(w, h, Image.SCALE_REPLICATE);
		BufferedImage dimg = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g = dimg.createGraphics();
		g.drawImage(tmp,0,0,null);
		g.dispose();

		return dimg;
	}

	public static void advanceTicks(int _ticks){
		ticks++;
		Point pos = player.e.getPos();
		// System.out.println(pos.toString());
		refreshFrame(render(pos.x,pos.y));
	}

	public static void takeTurn(){
		Inventory floorInv = floors.get(currentFloor).tileMap[player.e.y][player.e.x].inventory;
		if(!floorInv.isEmpty()){
			floorInv.printContents();
		}

		double playerHP = player.e.HP;
		player.e.SP += Creature.PLAYER.SP_REGEN;
		ArrayList<Entity> dead = new ArrayList<Entity>();
		for(Entity e: floors.get(currentFloor).entities.values()){
			// TODO: add mob sleep/detection stuff
			if(!e.takeTurn()){
				e.x = -1;
				e.y = -1;
				dead.add(e);
			}
		}

		for(Entity e: dead){
			e.die();
		}

		Point pos = player.e.getPos();
		if(player.e.HP != playerHP) appendText("Player HP = " + ActionLibrary.round(player.e.HP,2));
		if(player.e.HP < 0.1){
			appendText("tough luck kiddo. you dead");
			running = false;
		}
		refreshFrame(render(pos.x,pos.y));
	}

	public static BufferedImage render(int x, int y){
		BufferedImage img = floors.get(currentFloor).render_vig(x, y, player.ViewDistance, player.Luminosity);
		img = resize(img,img.getWidth()*3,img.getHeight()*3);
		return img;
	}

	public static String randomName(){

		// start with vowel or vtc
		// consonant or ctv

		// start with consonant or ctv
		// vowel
		// vtc or consonant

		int length = rng.nextInt(4)+5;
		// char[] vtc = {'s','t'}; // 1l2
		char[] vowels = {'a','e','i','o','u','y'}; // 0l6

		char[] ctv = {'r','h','w','l'}; // 3l4
		char[] consonants = {'b','c','d','f','g','j','k',
				'm','p','v','z','s','t','n'}; // 2l12

		// String[] pairs = {"ld","st","pr","qu","sh"};

		String name = "";
		int prev = -1;

		while(name.length() < length){
			if(prev == 3 || (prev == 2 && rng.nextBoolean()) || (prev == 0 && rng.nextBoolean() && rng.nextBoolean()) || (prev==-1 && rng.nextBoolean())){
				name+=vowels[rng.nextInt(vowels.length)];
				prev = 0;
			}else{
				if(prev==2 || (prev==-1 && rng.nextBoolean())){
					name+=ctv[rng.nextInt(ctv.length)];

					prev = 3;
				}else{
					name+=consonants[rng.nextInt(consonants.length)];

					prev = 2;
				}
			}
		}
		return name;
	}
}