import java.awt.Point;

// TODO (*) consider making this not consumable, just make all items throwable, with this overriding superclass properties.
public class Missile extends Item implements Consumable, Equippable {
	private boolean equipped = false;

	Missile(String id, int _amount){
		super(id);
		super.addPrompt('v', "qui(v)er"); // TODO: (R) Review
		super.addPrompt('t', "(t)hrow");
		super.setAmount(_amount);
		super.setStackable(true);
	}

	Missile(String id){
		// TODO (+) replace 1 with stack size.
		this(id,1);
	}

	@Override
	public void equip(Creature c) {
		Missile prev = c.quivered;
		if(prev != null) prev.unequip(c);
		c.quivered = this;
		this.equipped = true;
	}

	@Override
	public void unequip(Creature c) {
		c.quivered = null;
		this.equipped = false;
	}

	@Override
	public boolean isEquipped() {
		return this.equipped;
	}
	
	@Override
	public void drop(Creature from) {
		unequip(from);
		super.drop(from);
	}

	public void throwThis(Creature c, Point target) {
		// throwing logic
		use(c);
	}

	@Override
	public String toString() {
		return String.format("%s - %s %s%s%s",
			super.getInventoryID(),
			super.getQuantityString(),
			super.getDisplayName(),
			super.getAmount() > 1 ? "s" : "",
			isEquipped() ? " (quivered)" : ""
		);
	}

	@Override
	public void use(Creature c) {
		c.getInv().removeOne(this.getInventoryID());
	}
}
