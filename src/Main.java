/** @author carterbrown-27 **/

import java.io.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Logger;

// TODO (*) Decouple GUI from Main, Switch to JavaFX for GUI, create JApplet from JavaFX.
public class Main {
	// TODO (+) saving
	//	public static int seed = 12345678;
	private static Random rng;
	//	public static boolean randSeed = true;

	private static boolean running;

	private static View view;

	private static Player player;
	private static String playerName = "Hillstyle";

	public static final int MAP_H = 45;
	public static final int MAP_W = 45;
	public static final int MAP_FILL = 49;

	private static int ticks = 0;

	private static double lastPress;
	private static double interval = 70; // 70ms input interval.

	private static GameState state;
	private static InventorySelectAction invSelAction;
	private static Interaction interaction;

	private static char selectedItem;
	private static Point targetPos;

	private static HashMap<Integer,Map> floors;
	private static int floorNumber;
	private static Map currentMap;
	private static Inventory currentInventory;
	private static Point ropePoint; // TEMP
	private static Point lastPos;

	private static int seed;

	private static final Logger logger = Logger.getLogger(Main.class.getName());
	private static StringHelper stringHelper;

	public static void main(String[] args){

		running = true;
		
		state = GameState.REGULAR;
		invSelAction = InventorySelectAction.NONE;

		rng = new Random();

		lastPress = System.currentTimeMillis();

		floors = new HashMap<>();

		seed = rng.nextInt(Integer.MAX_VALUE);
		rng = new Random(getSeed());

		stringHelper = new StringHelper(rng);

		view = new View();
		startGame();
		logger.info("Seed = " + getSeed());

		// controls
		getView().getFrame().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				logger.fine("Key Pressed "+KeyEvent.getKeyText(e.getKeyCode()));

				if(!isRunning()){
					logger.warning("Game not running.");
					if(e.getKeyCode() == KeyEvent.VK_SPACE){
						// restart
						startGame();
					}
					return;
				}

				if (System.currentTimeMillis()-lastPress>=interval) {
					logger.fine("Valid Press");
					lastPress = System.currentTimeMillis();
				}else {
					return;
				}

				if(!state.equals(GameState.PICKUP) && !state.equals(GameState.INVENTORY)){
					// System.out.println("flag 1");
					getView().clearText();
				}

				// ESCAPE takes precedent.
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					state = GameState.REGULAR;
					return;
				}

				if(state == GameState.REGULAR) {
					// if/elifs as order may matter, performance is not important (this is called on valid keypresses)
					if (directionValue(e) != null) {
						// Directional Key: corresponding move/action
						logger.fine("Directional Key");
						Direction dir = directionValue(e);
						getPlayer().act_adj(dir);

					}else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						// ENTER: Descend/Ascend Staircase
						handleUseStaircase();

					} else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
						// Ctrl-P: Prints map to console
						getCurrentMap().printMap();

					} else if(e.getKeyCode() == KeyEvent.VK_T) {
						// T: toggles targeting mode
						state = !(state == GameState.TARGETING) ? GameState.TARGETING : GameState.REGULAR;
						// Crosshairs' origin = player.
						targetPos = getPlayer().getPos();

					} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
						// SPACE: Open action selections
						// TODO (A) Implement

					} else if(e.getKeyCode() == KeyEvent.VK_G) {
						// G: get item
						handleGet();

					} else if(e.getKeyCode() == KeyEvent.VK_I){
						// I: open inventory
						getPlayer().getInv().printContents(false);

						if(!getPlayer().getInv().isEmpty()) {
							state = GameState.INVENTORY;
						}
					
					} else if(e.getKeyCode() == KeyEvent.VK_O){
						// O: Open Chest
						// TODO: change to general Interactable
						ArrayList<StaticEntity> interactablesInRange = getCurrentMap().getInteractablesInRange(getPlayer().getPos());
						
						if(interactablesInRange.size() == 1) {
							interactablesInRange.get(0).interact(getPlayer(), Interaction.OPEN);
							takeTurn();
						}else if(interactablesInRange.size() > 1){
							state = GameState.INTERACT_SELECT;
							interaction = Interaction.OPEN;
							getView().appendText("Enter the direction of what you want to open.");
						}else {
							getView().appendText("There is nothing to open here.");
						}

					}else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
						// Ctrl-C: Clear
						getView().clearText();
					}else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
						takeTurn();
					}else if(e.getKeyCode() == KeyEvent.VK_5){
						// 5: Rest
						getPlayer().startRest();
					}else if(e.getKeyCode() == KeyEvent.VK_X){
						// X: Map Overview	
						// TODO (+) Add Map Overview
					}else{
						// other keys: abilities
						for(Player.Ability a: Player.Ability.values()){
							if(e.getKeyCode() == a.k){
								if(getPlayer().getSP() >= a.s){
									getPlayer().select(a);
									getView().appendText(a.name);
								}else{
									getView().appendText("Not enough stamina!");
									getPlayer().deselect();
								}
								break;
							}
						}
					}
				} else {
					char k = e.getKeyChar();
					switch(state) {
					case PICKUP : {
						handleItemPickup(k);
						break;
					}

					case INVENTORY : {
						handleInventory(k);
						break;
					}

					case ITEM_SCREEN : {
						handleItemScreen(k);
						break;
					}

					case INVENTORY_SELECT : {
						handleInventorySelect(k);
						break;
					}
					
					case TARGETING : {
						handleTargeting(e);
						break;
					}
					
					case INTERACT_SELECT : {
						handleInteractSelect(e);
					}
					
					// Doesn't apply to REGULAR here, as in this branch REGULAR = False.
					default : {
						logger.severe("State not recognized or fallthrough occured.");
					}
					
					}
				}
			}
		});

		// System.out.println(ropePoint.toString());
		getView().refreshFrame(render(ropePoint.x,ropePoint.y));
	}

	public static Random getRng() {
		return rng;
	}

	public static boolean isRunning() {
		return running;
	}

	public static View getView() {
		return view;
	}

	public static Player getPlayer() {
		return player;
	}

	public static String getPlayerName() {
		return playerName;
	}

	public static int getTicks() {
		return ticks;
	}

	public static HashMap<Integer, Map> getFloors() {
		return floors;
	}

	public static int getFloorNumber() {
		return floorNumber;
	}

	public static Map getCurrentMap() {
		return currentMap;
	}

	public static Inventory getCurrentInventory() {
		return currentInventory;
	}

	public static int getSeed() {
		return seed;
	}

	public static StringHelper getStringHelper() {
		return stringHelper;
	}

	public static void setCurrentInventory(Inventory inv){
		currentInventory = inv;
	}

	public static enum GameState {
		REGULAR,
		INVENTORY,
		PICKUP,
		ITEM_SCREEN,
		TARGETING,
		INVENTORY_SELECT,
		INTERACT_SELECT
	}

	public static void setGameState(GameState s) {
		state = s;
	}

	public static enum InventorySelectAction {
		NONE,
		IDENTIFY,
		ENCHANT,
		ETC
	}
	
	public static void setInvSelAction(InventorySelectAction a) {
		invSelAction = a;
	}
	
	// TODO (R) Review: move?
	public static enum Interaction {
		NONE,
		OPEN,
		CLOSE,
		TALK,
		ETC
	}
	
	public static void setInteraction(Interaction i) {
		interaction = i;
	}

	public static void handleItemPickup(char k) {
		logger.fine("Item Pickup Handler called.");
		if(k < 'a' || k > 'z'){
			currentInventory = null;
			state = GameState.REGULAR;
			return;
		}

		if(getCurrentInventory() ==null){
			currentInventory = getCurrentMap().tileMap[getPlayer().getY()][getPlayer().getX()].inventory;
		}

		if (getCurrentInventory().contains(k)) {
			getPlayer().pickUp(k, getCurrentInventory());
			if(getCurrentInventory().isEmpty()){
				currentInventory = null;
				state = GameState.REGULAR;
			}
		}
	}
	
	public static void handleTargeting(KeyEvent e) {
		Direction dir = directionValue(e);
		if(dir != null) {
			// TODO (+) aiming visuals & continual L.O.S. check
			targetPos = Direction.translate(targetPos,dir);
		}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
			handleTargetConfirm();
		}
	}
	
	public static void handleInteractSelect(KeyEvent e) {
		Direction dir = directionValue(e);
		if(dir != null) {
			// TODO (R) Review: could map to directions for slightly nicer code
			ArrayList<StaticEntity> inRange = getCurrentMap().getInteractablesInRange(getPlayer().getPos());
			for(StaticEntity se: inRange) {
				if(se.getPos().equals(Direction.translate(getPlayer().getPos(), dir))) {
					se.interact(getPlayer(), interaction);
					takeTurn();
					break;
				}
			}
		}
	}
	
	public static void handleItemScreen(char k) {
		logger.fine("Item Screen Handler called.");
		char c = selectedItem;
		Item i = getPlayer().getInv().getItem(c);

		if(!Character.isAlphabetic(k) || !i.actionsContains(k)) {
			getView().appendText(k + " is not an option.");
			return;
		}

		// NOTE: letters for actions must be unique across all items
		switch(k) {

		case 'd' : {
			i.drop(getPlayer());
			break;
		}

		case 'a' : {
			// reassign
			break;
		}

		}

		if(i instanceof Equippable){
			Equippable eq = (Equippable) i;

			// TODO: differentiate
			switch(k) {

			case 'w' : {
				getView().appendText("You wield your "+i.getDisplayName());
				eq.equip(getPlayer());
				break;
			}

			case 'p' : {
				getView().appendText("You put on your "+i.getDisplayName());
				eq.equip(getPlayer());
				break;
			}

			case 'u' : {
				getView().appendText("You unwield your "+i.getDisplayName());
				eq.unequip(getPlayer());
				break;
			}

			case 't' : {
				getView().appendText("You take off your "+i.getDisplayName());
				eq.unequip(getPlayer());
				break;
			}

			}
			// TODO (A) Implement
		}

		if(i instanceof Consumable){
			Consumable cnsm = (Consumable) i;
			// TODO (A) Implement
			switch(k) {

			case 'r' : {
				// read effect
				getView().appendText("You read the "+i.getDisplayName());
				cnsm.use(getPlayer());
				break;
			}

			case 'q' : {
				// quaff effect
				getView().appendText("You quaff the "+i.getDisplayName());
				cnsm.use(getPlayer());
				break;
			}

			case 'e' : {
				getView().appendText("You eat the "+i.getDisplayName());
				cnsm.use(getPlayer());
				break;
			}
			}
		}

		state = GameState.REGULAR;
		getView().refreshStats();
	}

	public static void handleInventory(char k) {
		logger.fine("Inventory Handler called.");
		getView().clearText();
		
		boolean selected = false;
		if (getPlayer().getInv().contains(k)) {
			selected = true;
			// open item menu
			// TODO (R) Review
			Item i = getPlayer().getInv().getItem(k);

			getView().appendText(i+" selected.");
			getView().appendText(i.getDescription());

			for(String s: i.listPrompts()) {
				getView().appendText(s);
			}

			selectedItem = k;
		}

		if(selected) {
			state = GameState.ITEM_SCREEN;
		} else {
			state = GameState.REGULAR;
		}
	}

	public static void handleInventorySelect(char k) {
		logger.fine("Inventory Selection Handler called.");
		
		boolean flag = false;
		if (getPlayer().getInv().contains(k)) {
			Item i = getPlayer().getInv().getItem(k);
			if(invSelAction == InventorySelectAction.IDENTIFY  && !getPlayer().isItemIdentified(i)){
				flag = true;
				getPlayer().identify(i);
			}else if(invSelAction == InventorySelectAction.ENCHANT){
				flag = true;
			}
		}

		if(!flag){
			getView().appendText(k+" is not a valid option.");
		}else{
			state = GameState.REGULAR;
			invSelAction = InventorySelectAction.NONE;
		}
	}

	public static void handleTargetConfirm() {
		ArrayList<Point> line = FOV.bresenhamLine(getPlayer().getPos(), targetPos);

		Projectile projectile = new Projectile(player.quivered, player.getPos(), currentMap);
		boolean f = true;
		for(Point p: line){
			if(p.equals(getPlayer().getPos()) || p.equals(line.get(line.size()-1))){
				continue;
			}

			if(!projectile.canOccupySpace(p.x, p.y)){
				f = false;
				break;
			}
		}
		
		// TODO: (A) Implement
		if(f){
			// valid target
			getView().appendText("valid target");
		}else{
			// invalid
			getView().appendText("invalid target");
		}
		state = GameState.REGULAR;
	}

	public static void handleGet() {
		Inventory tileInv = getCurrentMap().tileMap[getPlayer().getY()][getPlayer().getX()].inventory;
		Inventory selectedInv = null;


		// TODO (F) Fix Chests
		if(tileInv.isEmpty()){
			for(Entity n: getCurrentMap().entities){
				if(n instanceof StaticEntity) {
					StaticEntity se = (StaticEntity) n;
					if(se.getPos().equals(getPlayer().getPos())){
						if(!se.isLocked){
							selectedInv = se.getInv();
							getView().appendText("Pick up what?");
						}else{											
							getView().appendText("It's locked.");
							return;
						}
						// n.SE.interact(player,'g');
						break;
					}
				}
			}
		}else{
			selectedInv = tileInv;
		}

		if(selectedInv == null || selectedInv.isEmpty()){
			getView().appendText("There is nothing here.");
			return;
		}

		if(selectedInv.isOneItem()){
			getPlayer().pickUp(selectedInv.getFirstItem(),selectedInv);
		}else if(!selectedInv.isEmpty()){
			state = GameState.PICKUP;
			currentInventory = selectedInv;
			getView().appendText("Pick up what?");
		}
	}

	public static void handleUseStaircase() {
		if(getCurrentMap().valueAt(getPlayer().getPos()) == 3){
			if(getFloors().size()<= getFloorNumber() +1){
				newFloor();
			}else{
				changeFloor(getFloorNumber() +1,true,false);
			}
		}else if(getCurrentMap().valueAt(getPlayer().getPos()) == 2 && getFloorNumber() > 0){
			// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
			changeFloor(getFloorNumber() -1,false,false);
		}
		getView().appendText("Current Floor: "+ getFloorNumber());
	}

	public static void startGame(){
		getFloors().clear();
		floorNumber = 0;

		getFloors().put(getFloorNumber(),  new Map(MAP_H, MAP_W, MAP_FILL));
		currentMap = getFloors().get(getFloorNumber());
		ropePoint = getCurrentMap().getPosition(2);
		player = new Player(ropePoint.x,ropePoint.y, getCurrentMap());
		getView().getFrame().setIconImage(getPlayer().getSprite().getScaledInstance(96,96,Image.SCALE_SMOOTH));

		changeFloor(getFloorNumber(), true, true);

		// TODO (T) TEMP player default inventory
		Weapon dagger = new Weapon("dagger");
		getPlayer().getInv().addItem(dagger);
		getPlayer().equip(dagger);

		Missile darts = new Missile("throwing dart",3);
		getPlayer().getInv().addItem(darts);
		getPlayer().equip(darts);
		
		Food bread = new Food("bread");
		getPlayer().getInv().addItem(bread);

		running = true;
	}

	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		// floors.replace(currentFloor, new Map(map_h,map_w,map_fill,rng));
		logger.fine("Now " + getFloors().size() + " floors.");
		getFloors().put(getFloorNumber() +1, new Map(MAP_H, MAP_W, MAP_FILL));
		changeFloor(getFloorNumber() +1,true,true);
	}

	public static void changeFloor(int floor, boolean down, boolean isNew){
		long time = System.currentTimeMillis();
		floorNumber = floor;
		currentMap = getFloors().get(getFloorNumber());
		Point startPoint= getCurrentMap().getPosition(2);

		// TODO: update for multiple stairs
		if(!down) startPoint= getCurrentMap().getPosition(3);
		getPlayer().map = getCurrentMap();
		getPlayer().setPos(startPoint);
		getCurrentMap().player = getPlayer();

		if(isNew){
			// TODO (T) TEMP populate level with mobs
			int mobs = rng.nextInt(8)+16;
			for (int i = 0; i < mobs; i++) {
				Point t;
				do{
					t = getCurrentMap().randomOpenSpace();
					// make sure mobs aren't within 3 tiles of player.
				}while(Math.abs(t.x- getPlayer().getX()) <=3 && Math.abs(t.y- getPlayer().getY()) <=3);

				// logger.info("point picked");
				// TODO: tierCalculation.
				logger.fine("Adding Entity...");
				new Creature(getFloorNumber(),t, getCurrentMap());
			}

			// TODO (T) TEMP populate level with items
			int items = rng.nextInt(6)+14; // 6+8, 6+28
			for(int i = 0; i < items; i++){
				Point t = getCurrentMap().randomOpenSpace();
				Item item = Item.randomItem(getFloorNumber());
				logger.fine("Adding item: "+ item.getDisplayName());
				getCurrentMap().tileMap[t.y][t.x].inventory.addItem(item);
			}

			// TODO (T) TEMP populate level with chests/keys
			int chests = rng.nextInt(2)+1;
			for(int i = 0; i < chests; i++){
				Point t = getCurrentMap().randomEmptySpace();
				logger.fine("adding Chest/Key pair");
				getCurrentMap().tileMap[t.y][t.x].inventory.addItem(new Key("Silver Key", getFloorNumber()));
				t = getCurrentMap().randomEmptySpace();

				getCurrentMap().entities.add(new StaticEntity("chest", t, getCurrentMap()));
			}
		}

		try{
			File output = new File(String.format("renders/%d_%d-t%d.png", getSeed(), getFloorNumber(), System.currentTimeMillis()));
			ImageIO.write(getCurrentMap().renderMap(), "png", output);
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.printf("@@@ Saved Render = %dms @@@\n", System.currentTimeMillis()-time);

		if(getView().getFrame()==null) getView().init();
		getView().refreshFrame(render(startPoint.x,startPoint.y));

		System.out.printf("@@@ Frame Initialized = %dms @@@\n",System.currentTimeMillis()-time);
	}

	public static void blackOverlay(){
		// TODO (F) load file into memory
		try{
			getView().refreshFrame(ImageIO.read(new File("imgs/descendingOverlay.png")));
		}catch(Exception e){
			logger.warning("Black overlay not found.");
		}
	}

	public static BufferedImage resize(BufferedImage img, float scale){
		int newWidth = (int) (img.getWidth()*scale);
		int newHeight = (int) (img.getHeight()*scale);
		BufferedImage dimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = dimg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.drawImage(img, 0, 0, newWidth, newHeight, null);
		g2.dispose();

		return dimg;
	}

	// TODO (X) Overhaul
	public static void takeTurn(){
		Inventory floorInv = getCurrentMap().tileMap[getPlayer().getY()][getPlayer().getX()].inventory;
		if(!floorInv.isEmpty() && (lastPos!=null && !getPlayer().getPos().equals(lastPos))){
			floorInv.printContents(true);
		}

		getPlayer().upkeep();

		ArrayList<Creature> dead = new ArrayList<>();

		// int[] order = floors.get(currentFloor).getEntityPriority();
		// for(int i: order){
		Queue<Creature> creatureQueue = new LinkedList<>();

		for(Entity e: getCurrentMap().entities){
			if(e instanceof Creature) {
				Creature c = (Creature) e;
				c.awakeCheck();
				// TODO (T) TEMP
				if(c.getHP() <= 0) {
					dead.add(c);
				}
				else if(c.isAwake()) creatureQueue.add(c);
			}
		}
		
		// TODO (T) TEMP
		for(Creature c: creatureQueue) {
			c.takeTurn();
		}

		for(Creature c: dead){
			c.die();
		}

		// TODO (M) death logic to Player.
		Point pos = getPlayer().getPos();
		lastPos = pos;
		// if(player.HP != playerHP) appendText("Player HP = " + ActionLibrary.round(player.HP,2));
		if(getPlayer().getHP() < 0.05){
			getView().appendText("You die...");
			running = false;
		}
		getView().refreshFrame(render(pos.x,pos.y));
	}

	@Deprecated
	public static boolean allWaiting(Queue<Creature> q){
		while(!q.isEmpty()){
			/* if(!q.remove().waiting) */ return false;
		}
		return true;
	}

	public static BufferedImage render(int x, int y){
		BufferedImage img = getCurrentMap().render_vig(x, y, getPlayer().viewDis, getPlayer().luminosity);
		img = resize(img, 4);
		return img;
	}
	
	/**
	 * @param keyCode: the KeyCode from the KeyEvent
	 * @param controlDown: is control pressed
	 * @param shiftDown: is shift pressed
	 * @return the direction corresponding to the keys pressed
	 * 
	 * These are how keys correspond to Directions:
	 * 
	 * YKU
	 * H@L
	 * BJN
	 * 
	 * or Arrow Keys + CTRL (adds down to L/R) + SHIFT (adds up to L/R)
	 * shift-left	up		shift-right
	 * left			@		right
	 * ctrl-left	down	ctrl-right
	 */
	public static Direction directionValue(int keyCode, boolean controlDown, boolean shiftDown){
		logger.finer("dir check");
		if (keyCode == KeyEvent.VK_K || keyCode == KeyEvent.VK_UP) {
			return Direction.UP;
		} else if (keyCode == KeyEvent.VK_H || keyCode == KeyEvent.VK_LEFT) {
			if(controlDown){
				return Direction.DOWN_LEFT;
			}else if(shiftDown){
				return Direction.UP_LEFT;
			}else{
				return Direction.LEFT;
			}
		} else if (keyCode == KeyEvent.VK_J || keyCode == KeyEvent.VK_DOWN) {
			return Direction.DOWN;
		} else if (keyCode == KeyEvent.VK_L || keyCode == KeyEvent.VK_RIGHT) {
			if(controlDown){
				return Direction.DOWN_RIGHT;
			}else if(shiftDown){
				return Direction.UP_RIGHT;
			}else{
				return Direction.RIGHT;
			}
		}else if(keyCode == KeyEvent.VK_Y){
			return Direction.UP_LEFT;
		}else if(keyCode == KeyEvent.VK_U){
			return Direction.UP_RIGHT;
		}else if(keyCode == KeyEvent.VK_B){
			return Direction.DOWN_LEFT;
		}else if(keyCode == KeyEvent.VK_N){
			return Direction.DOWN_RIGHT;
		}
		return null;
	}
	
	public static Direction directionValue(KeyEvent e) {
		return directionValue(e.getKeyCode(), e.isControlDown(), e.isShiftDown());
	}
}