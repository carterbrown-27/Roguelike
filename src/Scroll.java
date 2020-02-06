import java.util.HashMap;
import java.util.List;

public class Scroll extends Item implements Consumable {
	private static StringHelper stringHelper;
	private static HashMap<String,String> scrollNames = new HashMap<>();
	private String fakeName;
	
	Scroll(String id, int _amount){
		super(id);
		super.addPrompt('r', "(r)ead");      
		this.setAmount(_amount);
		
		if(stringHelper == null) stringHelper = new StringHelper(Main.rng);
		this.fakeName = randomScrollName(this.getTypeName());
	}
	
	Scroll(String id){
		this(id,1);
	}
	
	@Override
	public String toString() {
		String name = getDisplayName();
		String quantity = super.getQuantityString() + (!Main.player.isItemIdentified(this) ? " scroll(s) reading" : "");
		
		return String.format("%s - %s %s",super.getInventoryID(), quantity, name);
	}
	
	@Override
	public void use(Creature c) {
		// TODO (A) Implement
		
		super.delete(c);
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
}
