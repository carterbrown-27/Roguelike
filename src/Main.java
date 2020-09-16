/** @author carterbrown-27 **/

import java.io.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

	public static final int TILE_SIZE = 24;

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

	private static BufferedImage targetImg;
	private static BufferedImage targetDot;

	public static BufferedImage curMapLayer;
	public static BufferedImage curFXLayer;
	public static BufferedImage curTargetLayer;

	public static void main(String[] args){

		running = true;

		// TEMP
		try{
			targetImg = ImageIO.read(new File("imgs/crosshair.png"));
			targetDot = ImageIO.read(new File("imgs/targetdot.png"));
		}catch(IOException e){
			e.printStackTrace();
		}

		setGameState(GameState.REGULAR);
		setInvSelAction(InventorySelectAction.NONE);

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
		view.getFrame().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				logger.fine("Key Pressed " + KeyEvent.getKeyText(e.getKeyCode()));

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
					view.clearText();
				}

				// ESCAPE takes precedent.
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setRegularGameState();
					return;
				}

				if(state == GameState.REGULAR) {
					// if/elifs as order may matter, performance is not important (this is called on valid keypresses)
					if (directionValue(e) != null) {
						// Directional Key: corresponding move/action
						logger.fine("Directional Key");
						Direction dir = directionValue(e);
						player.act_adj(dir);

					}else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						// ENTER: Descend/Ascend Staircase
						handleUseStaircase();

					} else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
						// Ctrl-P: Prints map to console
						currentMap.printMap();

					} else if(e.getKeyCode() == KeyEvent.VK_T) {
						// T: toggles targeting mode
						// Crosshairs' origin = player.
						if(state == GameState.TARGETING){
							setRegularGameState();
						} else {
							setTargetingGameState();
						}

					} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
						// SPACE: Open action selections
						// TODO (A) Implement

					} else if(e.getKeyCode() == KeyEvent.VK_G) {
						// G: get item
						handleGet();

					} else if(e.getKeyCode() == KeyEvent.VK_I){
						// I: open inventory
						player.getInv().printContents(false);

						if(!player.getInv().isEmpty()) {
							setGameState(GameState.INVENTORY);
						}
					
					} else if(e.getKeyCode() == KeyEvent.VK_O){
						// O: Open Chest
						// TODO: change to general Interactable
						ArrayList<NonCreatureEntity> interactablesInRange = currentMap.getInteractablesInRange(player.getPos());
						
						if(interactablesInRange.size() == 1) {
							interactablesInRange.get(0).interact(player, Interaction.OPEN);
							takeTurn();
						}else if(interactablesInRange.size() > 1){
							setGameState(GameState.INTERACT_SELECT);
							interaction = Interaction.OPEN;
							view.appendText("Enter the direction of what you want to open.");
						}else {
							view.appendText("There is nothing to open here.");
						}

					}else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
						// Ctrl-C: Clear
						view.clearText();
					}else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
						takeTurn();
					}else if(e.getKeyCode() == KeyEvent.VK_5){
						// 5: Rest
						player.startRest();
					}else if(e.getKeyCode() == KeyEvent.VK_X){
						// X: Map Overview	
						// TODO (+) Add Map Overview
					}else{
						// other keys: abilities
						for(Player.Ability a: Player.Ability.values()){
							if(e.getKeyCode() == a.k){
								if(player.getSP() >= a.s){
									player.select(a);
									view.appendText(a.name);
								}else{
									view.appendText("Not enough stamina!");
									player.deselect();
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
		curMapLayer = renderMapLayer(ropePoint.x,ropePoint.y);
		renderAndUpdate();
	}

	public enum GameState {
		REGULAR,
		INVENTORY,
		PICKUP,
		ITEM_SCREEN,
		TARGETING,
		INVENTORY_SELECT,
		INTERACT_SELECT
	}

	public enum InventorySelectAction {
		NONE,
		IDENTIFY,
		ENCHANT,
		ETC
	}

	// TODO (R) Review: move?
	public enum Interaction {
		NONE,
		OPEN,
		CLOSE,
		TALK,
		ETC
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
	public static void setGameState(GameState s) {
		if(state != s) logger.info(String.format("Game State Changed from %s to %s", state, s));
		state = s;
	}

	public static void setRegularGameState(){
		setGameState(GameState.REGULAR);
		curTargetLayer = null;
		currentInventory = null;
		invSelAction = InventorySelectAction.NONE;
		renderAndUpdate();
	}

	public static void setTargetingGameState(){
		setGameState(GameState.TARGETING);
		view.clearText();
		view.appendText("Throw where?");

		targetPos = player.getPos();
		// solution to rel equations when targetPos = player.getPos() is = to player.viewDis
		updateTarget(player.viewDis, player.viewDis);
	}
	
	public static void setInvSelAction(InventorySelectAction a) {
		invSelAction = a;
	}
	
	public static void setInteraction(Interaction i) {
		interaction = i;
	}

	public static void handleItemPickup(char k) {
		logger.fine("Item Pickup Handler called.");
		if(k < 'a' || k > 'z'){
			setRegularGameState();
			return;
		}

		if(currentInventory == null){
			currentInventory = currentMap.getTile(player.getPos()).inventory;
		}

		if (currentInventory.contains(k)) {
			player.pickUp(k, currentInventory);
			if(currentInventory.isEmpty()){
				setRegularGameState();
			}
		}
	}
	
	public static void handleInteractSelect(KeyEvent e) {
		Direction dir = directionValue(e);
		if(dir != null) {
			// TODO (R) Review: could map to directions for slightly nicer code
			ArrayList<NonCreatureEntity> inRange = currentMap.getInteractablesInRange(player.getPos());
			for(NonCreatureEntity se: inRange) {
				if(se.getPos().equals(Direction.translate(player.getPos(), dir))) {
					se.interact(player, interaction);
					takeTurn();
					break;
				}
			}
		}
	}
	
	public static void handleItemScreen(char k) {
		logger.fine("Item Screen Handler called.");
		char c = selectedItem;
		Item i = player.getInv().getItem(c);

		if(!Character.isAlphabetic(k) || !i.actionsContains(k)) {
			view.appendText(k + " is not an option.");
			return;
		}

		// NOTE: letters for actions must be unique across all items
		switch(k) {

		case 'd' : {
			i.drop(player);
			break;
		}

		case 'a' : {
			// reassign
			break;
		}

		}

		if(i instanceof Equippable){
			Equippable eq = (Equippable) i;

			switch(k) {

				// TODO (R): Get rid of duplicate logic
				case 'w' : {
					view.appendText("You wield your "+i.getDisplayName());
					eq.equip(player);
					break;
				}

				case 'p' : {
					view.appendText("You put on your "+i.getDisplayName());
					eq.equip(player);
					break;
				}

				case 'v' : {
					view.appendText("You quiver your "+i.getDisplayName());
					eq.equip(player);
					break;
				}

				case 'u' : {
					view.appendText("You unequip your "+i.getDisplayName());
					eq.unequip(player);
					break;
				}
			}
			// TODO (A) Implement... Something?
		}

		if(i instanceof Consumable){
			Consumable cnsm = (Consumable) i;
			switch(k) {

			case 'r' : {
				// read effect
				view.appendText("You read the "+i.getDisplayName());
				cnsm.use(player);
				break;
			}

			case 'q' : {
				// quaff effect
				view.appendText("You quaff the "+i.getDisplayName());
				cnsm.use(player);
				break;
			}

			case 'e' : {
				view.appendText("You eat the "+i.getDisplayName());
				cnsm.use(player);
				break;
			}

			case 't' : {
				setTargetingGameState();
				break;
			}
			}
		}

		view.refreshStats();
		if(invSelAction == InventorySelectAction.NONE){
			Main.takeTurn();
		}
	}

	public static void handleInventory(char k) {
		logger.fine("Inventory Handler called.");
		view.clearText();
		
		boolean selected = false;
		if (player.getInv().contains(k)) {
			selected = true;
			// open item menu
			// TODO (R) Review
			Item i = player.getInv().getItem(k);

			view.appendText(i+" selected.");
			view.appendText(i.getDescription());

			for(String s: i.listPrompts()) {
				view.appendText(s);
			}

			selectedItem = k;
		}

		if(selected) {
			setGameState(GameState.ITEM_SCREEN);
		} else {
			setRegularGameState();
		}
	}

	public static void handleInventorySelect(char k) {
		logger.info("Inventory Selection Handler called.");
		
		boolean flag = false;
		if (player.getInv().contains(k)) {
			Item i = player.getInv().getItem(k);
			System.out.println("id!");
			if(invSelAction == InventorySelectAction.IDENTIFY  && !player.isItemIdentified(i)){
				flag = true;
				player.identify(i);
			}else if(invSelAction == InventorySelectAction.ENCHANT){
				flag = true;
			}
		}

		if(!flag){
			view.appendText(k+" is not a valid option.");
		}else{
			setRegularGameState();
		}
	}

	public static void handleTargeting(KeyEvent e) {
		Direction dir = directionValue(e);
		if(dir != null) {
			// TODO (+) aiming visuals & continual L.O.S. check
			Point tmp = targetPos;
			targetPos = Direction.translate(targetPos,dir);
			int relX = targetPos.x - (player.getX() - player.viewDis);
			int relY = targetPos.y - (player.getY() - player.viewDis);

			int td = getImageTileDimension();
			if(relX < 0 || relY < 0 || relX >= td || relY >= td){
				targetPos = tmp;
				logger.fine("Crosshair moved out of bounds.");
			}else{
				updateTarget(relX, relY);
			}
		}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
			handleTargetConfirm();
		}
	}

	// TODO (A) Add throwing of non-quivered objects
	public static void handleTargetConfirm() {
		if(player.getPos() == targetPos) return;
		List<Tile> line = Bresenham.findLine(currentMap.getTileMap(), player.getPos(), targetPos);

		Projectile projectile = new Projectile(player.quivered, player.getPos(), currentMap);
		int i;
		for(i = 1; i < line.size()-1; i++){
			Tile t = line.get(i);
			// TODO (T) TEMP: move to tile logic.
			if(!t.canOccupy(true, false)){
				i--;
				break;
			}
		}

		// TODO (T) Add a better stack-splitting system
		line.get(i).inventory.addItem(new Missile(player.quivered.getTypeName(), 1));
		player.getInv().removeOne(player.quivered.getInventoryID());

		curMapLayer = renderMapLayer(player.getX(), player.getY());
		renderAndUpdate();
		setRegularGameState();
	}

	public static void updateTarget(int relX, int relY){
		int d = getImageDimension();

		BufferedImage targetLayer = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);

		List<Point> line = Bresenham.findLine(currentMap.getTileMap(), player.getPos(), targetPos).stream().map(x -> x.pos).collect(Collectors.toList());;

		Graphics g = targetLayer.getGraphics();
		g.drawImage(targetImg, relX * TILE_SIZE, relY * TILE_SIZE, null);

		int plx = player.getX() - player.viewDis, ply = player.getY() - player.viewDis;
		for(int i = 1; i < line.size()-1; i++){
			Point p = line.get(i);
			// System.out.println(p);
			g.drawImage(targetDot, (p.x - plx) * TILE_SIZE, (p.y - ply) * TILE_SIZE, null);
		}

		g.dispose();

		curTargetLayer = targetLayer;

		renderAndUpdate();
	}

	public static void handleGet() {
		Inventory tileInv = currentMap.getTile(player.getPos()).inventory;
		Inventory selectedInv = null;


		// TODO (F) Fix Chests
		if(tileInv.isEmpty()){
			for(Entity n: currentMap.entities){
				if(n instanceof NonCreatureEntity) {
					NonCreatureEntity se = (NonCreatureEntity) n;
					if(se.getPos().equals(player.getPos())){
						if(!se.isLocked){
							selectedInv = se.getInv();
							view.appendText("Pick up what?");
						}else{											
							view.appendText("It's locked.");
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
			view.appendText("There is nothing here.");
			return;
		}

		if(selectedInv.isOneItem()){
			player.pickUp(selectedInv.getFirstItem(), selectedInv);
		}else if(!selectedInv.isEmpty()){
			setGameState(GameState.PICKUP);
			currentInventory = selectedInv;
			view.appendText("Pick up what?");
		}
	}

	public static void handleUseStaircase() {
		if(currentMap.valueAt(player.getPos()) == 3){
			if(floors.size()<= floorNumber +1){
				newFloor();
			}else{
				changeFloor(floorNumber + 1,true,false);
			}
		}else if(currentMap.valueAt(player.getPos()) == 2 && floorNumber > 0){
			// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
			changeFloor(floorNumber - 1,false,false);
		}
		view.appendText("Current Floor: "+ floorNumber);
	}

	public static void startGame(){
		player = new Player(0, 0,null);

		floors.clear();
		floorNumber = 0;
		floors.put(floorNumber,  new Map(MAP_H, MAP_W, MAP_FILL));
		currentMap = floors.get(floorNumber);

		player.map = currentMap;
		ropePoint = currentMap.getPosition(2);
		player.setPos(ropePoint);

		view.getFrame().setIconImage(player.getSprite().getScaledInstance(96,96,Image.SCALE_SMOOTH));

		changeFloor(floorNumber, true, true);

		// TODO (T) TEMP player default inventory
		Weapon dagger = new Weapon("dagger");
		player.getInv().addItem(dagger);
		player.equip(dagger);

		Missile darts = new Missile("throwing dart",3);
		player.getInv().addItem(darts);
		player.equip(darts);
		
		Food bread = new Food("bread");
		player.getInv().addItem(bread);

		running = true;
	}

	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		// floors.replace(currentFloor, new Map(map_h,map_w,map_fill,rng));
		logger.fine("Now " + floors.size() + " floors.");
		floors.put(floorNumber +1, new Map(MAP_H, MAP_W, MAP_FILL));
		changeFloor(floorNumber +1,true,true);
	}

	public static void changeFloor(int floor, boolean down, boolean isNew){
		long time = System.currentTimeMillis();
		floorNumber = floor;
		currentMap = floors.get(floorNumber);
		Point startPoint= currentMap.getPosition(2);

		// TODO: update for multiple stairs
		if(!down) startPoint= currentMap.getPosition(3);
		player.map = currentMap;
		player.setPos(startPoint);

		if(isNew){
			// TODO (T) TEMP populate level with mobs
			int mobs = rng.nextInt(8)+16;
			for (int i = 0; i < mobs; i++) {
				Point t;
				do{
					t = currentMap.randomOpenSpace();
					// make sure mobs aren't within 3 tiles of player.
				}while(Math.abs(t.x- player.getX()) <=3 && Math.abs(t.y- player.getY()) <=3);

				// logger.info("point picked");
				// TODO: tierCalculation.
				logger.fine("Adding Entity...");
				new Creature(rng.nextInt(2)+1, t, getCurrentMap()); // TEMP
			}

			// TODO (T) TEMP populate level with items
			int items = rng.nextInt(6)+14; // 6+8, 6+28
			for(int i = 0; i < items; i++){
				Point t = currentMap.randomOpenSpace();
				Item item = Item.randomItem(floorNumber);
				logger.fine("Adding item: "+ item.getDisplayName());
				currentMap.getTile(t).inventory.addItem(item);
			}

			// TODO (T) TEMP populate level with chests/keys
			int chests = rng.nextInt(2)+1;
			for(int i = 0; i < chests; i++){
				Point t = currentMap.randomEmptySpace();
				logger.fine("adding Chest/Key pair");
				currentMap.getTile(t).inventory.addItem(new Key("Silver Key", floorNumber));
				t = currentMap.randomEmptySpace();

				currentMap.entities.add(new NonCreatureEntity("chest", t, getCurrentMap()));
			}
		}

		try{
			File output = new File(String.format("renders/%d_%d-t%d.png", getSeed(), floorNumber, System.currentTimeMillis()));
			ImageIO.write(currentMap.renderMap(), "png", output);
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.printf("@@@ Saved Render = %dms @@@\n", System.currentTimeMillis()-time);

		if(view.getFrame()==null) view.init();
		curMapLayer = renderMapLayer(player.getX(), player.getY());
		renderAndUpdate();

		System.out.printf("@@@ Frame Initialized = %dms @@@\n",System.currentTimeMillis()-time);
	}

	public static void blackOverlay(){
		// TODO (F) load file into memory
		try{
			view.refreshFrame(ImageIO.read(new File("imgs/descendingOverlay.png")));
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
		Inventory floorInv = currentMap.getTile(player.getPos()).inventory;
		if(!floorInv.isEmpty() && (lastPos!=null && !player.getPos().equals(lastPos))){
			floorInv.printContents(true);
		}

		player.upkeep();

		ArrayList<Creature> dead = new ArrayList<>();

		// int[] order = floors.get(currentFloor).getEntityPriority();
		// for(int i: order){
		Queue<Creature> creatureQueue = new LinkedList<>();

		for(Entity e: currentMap.entities){
			if(e instanceof Creature) {
				Creature c = (Creature) e;
				c.awakeCheck();
				// TODO (T) TEMP
				if(c.getHP() <= 0) {
					dead.add(c);
				} else if(c.isAwake()) {
					creatureQueue.add(c);
				}
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
		Point pos = player.getPos();
		lastPos = pos;
		// if(player.HP != playerHP) appendText("Player HP = " + ActionLibrary.round(player.HP,2));
		if(player.getHP() <= 0){
			view.appendText("You die...");
			running = false;
		}

		curMapLayer = renderMapLayer(player.getX(), player.getY());

		setRegularGameState();
		renderAndUpdate();
	}
	
	public static void renderAndUpdate(){
		// TODO (F) Standardize render sizes
		// TEMP

		int d = getImageDimension();
		BufferedImage fullRender = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);

		Graphics g = fullRender.getGraphics();
		g.drawImage(curMapLayer, 0, 0, null);
		g.drawImage(curFXLayer, 0, 0, null);
		g.drawImage(curTargetLayer, 0, 0, null);
		g.dispose();

		fullRender = resize(fullRender, 4);
		view.refreshFrame(fullRender);
	}

	// TODO (T) TEMP
	public static int getImageTileDimension(){
		return (player.viewDis * 2 + 1);
	}

	public static int getImageDimension(){
		return getImageTileDimension() * TILE_SIZE;
	}

	@Deprecated
	public static boolean allWaiting(Queue<Creature> q){
		while(!q.isEmpty()){
			/* if(!q.remove().waiting) */ return false;
		}
		return true;
	}

	public static BufferedImage renderMapLayer(int x, int y){
		return currentMap.render_vig(x, y, player.viewDis, player.luminosity);
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