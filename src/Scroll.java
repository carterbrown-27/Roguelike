import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONObject;

public class Scroll extends Item implements Consumable {
	private static StringHelper stringHelper;
	private static HashMap<String,String> scrollNames = new HashMap<>();
	private String fakeName;
	private HashMap<String,Integer> effects = new HashMap<>();
	private static BufferedWriter bw;

	Scroll(String id, int _amount){
		super(id);
		super.addPrompt('r', "(r)ead");
		super.setStackable(true);
		super.setUnknown(true);
		this.setAmount(_amount);

		if(stringHelper == null) stringHelper = new StringHelper(Main.getRng());
//		if(bw == null){
//			try{
//				bw = new BufferedWriter(new FileWriter("Logs/ScrollNames.txt"))
//			}catch(IOException e){
//				e.printStackTrace();
//			}
//		}
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
		if(Main.getPlayer().isItemIdentified(this)) {
			return String.format("%s - %s %s", super.getInventoryID(), quantity, super.getAmount() > 1 ? name.replace("scroll", "scrolls") : name);
		}else {
			return String.format("%s - %s scroll%s reading %s", super.getInventoryID(), quantity, super.getAmount() > 1 ? "s" : "", name);
		}

	}

	@Override
	public void use(Creature c) {
		// TODO (A) Implement
		c.getInv().removeOne(this.getInventoryID());

		if(c instanceof Player) {
			((Player) c).identify(this);
		}

		// TODO (T) TEMP
		if(effects.containsKey("IDENTIFY")) {
			if (c.getInv().containsUnidentified()) {
				Main.getView().appendText("Identify what?");
				c.getInv().printContents(false);
				Main.setGameState(Main.GameState.INVENTORY_SELECT);
				Main.setInvSelAction(Main.InventorySelectAction.IDENTIFY);
				// System.out.println("here1");
			} else {
				Main.getView().appendText("There is nothing in your pack to identify!");
			}
		}

		if(effects.containsKey("TELEPORT")) {
			c.setPos(c.map.randomEmptySpace());
		}
		// "noise", "dizzy" etc
	}

	@Override
	public String getDisplayName() {
		if(Main.getPlayer().isItemIdentified(this)){
			return super.getDisplayName();
		}else {
			return fakeName;
		}
	}

	@Override
	public String getDescription() {
		if(Main.getPlayer().isItemIdentified(this)){
			return super.getDescription();
		}else {
			return "An unknown scroll.";
		}		
	}

	public static String randomScrollName(String realName) {
		if (scrollNames.containsKey(realName)) {
			return scrollNames.get(realName);
		}
		// TODO: Although chance is very, very, small, ensure that two scroll types cannot have the same fake name
		// Odds = Ω(1/(7*Σ[i,4,10](i!))) (if name generator was completely random, which it is not, it would be 1/30 million)
		String name = (stringHelper.randomName() + " " + stringHelper.randomName()).toUpperCase();
		scrollNames.put(realName, name);
//		try{
//			bw.write(realName);
//			bw.flush();
//		}catch(IOException e){
//			e.printStackTrace();
//		}

		return name;
	}
}
