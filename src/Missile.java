import java.awt.Point;
import java.util.List;

// TODO (*) consider making this not consumable, just make all items throwable, with this overriding superclass properties.
public class Missile extends Item implements Consumable, Equippable {
	private boolean equipped;
	private int amount;
	
	Missile(String id, int _amount){
		super(id);
		super.addPrompt('o', "thr(o)w");
		this.setAmount(_amount);
	}
	
	Missile(String id){
		// TODO (+) replace 1 with stack size.
		this(id,1);
	}
	
	@Override
	public void equip(Creature c) {
		
	}
	
	@Override
	public void unequip(Creature c) {
		
	}
	
	@Override
	public boolean isEquipped() {
		return this.equipped;
	}
	
	public void throwThis(Creature c, Point target) {
		// throwing logic
		use(c);
	}
	
	@Override
	public String toString() {
		String name = getDisplayName();
		String quantity = super.getQuantityString();

		return String.format("%s - %s %s%s",super.getInventoryID(), quantity, name, super.getAmount() > 1 ? "s" : "");
	}
	
	@Override
	public void use(Creature c) {
		
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public void setAmount(int amt) {
		this.amount = amt;
	}

	@Override
	public void changeAmount(int amt) {
		this.amount += amt;
		if(this.amount < 0) {
			this.amount = 0;
		}
	}
	
	@Override
	public boolean isStackable() {
		return true;
	}
}
