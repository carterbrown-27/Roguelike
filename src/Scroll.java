import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public class Scroll extends Item implements Consumable {
	private static StringHelper stringHelper;
	private static HashMap<String,String> scrollNames = new HashMap<>();
	private String fakeName;
	private HashMap<String,Integer> effects = new HashMap<>();

	Scroll(String id, int _amount){
		super(id);
		super.addPrompt('r', "(r)ead");      
		this.setAmount(_amount);

		if(stringHelper == null) stringHelper = new StringHelper(Main.rng);
		this.fakeName = randomScrollName(this.getTypeName());

		JSONObject effectData = super.getItemData().getJSONObject("effects");

		for(String k: effectData.keySet()) {
			Integer v = effectData.getInt(k);
			effects.put(k, v);
		}
	}

	Scroll(String id){
		this(id,1);
	}

	@Override
	public String toString() {
		String name = getDisplayName();
		String quantity = super.getQuantityString();

		// TODO (R) Refactor
		if(Main.player.isItemIdentified(this)) {
			return String.format("%s - %s %s", super.getInventoryID(), quantity, super.getAmount() > 1 ? name.replace("scroll", "scrolls") : name);
		}else {
			return String.format("%s - %s scroll%s reading %s", super.getInventoryID(), quantity, super.getAmount() > 1 ? "s" : "", name);
		}

	}

	@Override
	public void use(Creature c) {
		// TODO (A) Implement
		c.getInv().removeOne(this.getInventoryID());

		// TODO (T) TEMP
		if(effects.containsKey("IDENTIFY")) {
			if(c.getInv().containsUnidentified()){
				Main.view.appendText("Identify what?");
				c.getInv().printContents(false);
				Main.setGameState(Main.GameState.INVENTORY_SELECT);
				Main.setInvSelAction(Main.InventorySelectAction.IDENTIFY);
			}else{
				Main.view.appendText("There is nothing in your pack to identify!");
			}
		}else if(effects.containsKey("TELEPORT")) {
			c.setPos(c.map.randomEmptySpace());
		}

		if(c instanceof Player) {
			((Player) c).identify(this);
		}
		// "noise", "dizzy" etc
		Main.takeTurn();
	}

	@Override
	public String getDisplayName() {
		if(Main.player.isItemIdentified(this)){
			return super.getDisplayName();
		}else {
			return fakeName;
		}
	}

	@Override
	public String getDescription() {
		if(Main.player.isItemIdentified(this)){
			return super.getDescription();
		}else {
			return "An unknown scroll.";
		}		
	}

	public static String randomScrollName(String realName) {
		if(scrollNames.containsKey(realName)) {
			return scrollNames.get(realName);
		}
		String name = (stringHelper.randomName()+" "+stringHelper.randomName()).toUpperCase();
		scrollNames.put(realName, name);
		return name;
	}

	// TODO (T) TEMP
	@Override
	public boolean isStackable() {
		return true;
	}

	@Override
	public boolean isUnknown() {
		return true;
	}
}
