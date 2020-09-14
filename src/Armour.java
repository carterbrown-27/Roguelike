import org.json.JSONObject;

public final class Armour extends Item implements Equippable {
	private boolean equipped = false;
	private double defence;
	private String slot;
	
	Armour(String id) {
		super(id);
		super.setAmount(1);
		super.addPrompt('u', "(u)nequip");
		super.addPrompt('p', "(p)ut on");
		
		JSONObject itemData = super.getItemData();
		this.slot = itemData.getString("slot");
		this.defence = itemData.getDouble("defence");
	}
	
	public double getDefence() {
		return defence;
	}
	
	@Override
	public String toString() {
		return super.toString() + (isEquipped() ? " (worn)" : "");
	}

	@Override
	public void equip(Creature c) {
		Armour prev = c.getArmourSet().getSlot(slot);
		if(prev != null) prev.unequip(c);
		c.getArmourSet().putInSlot(slot, this);
		equipped = true;
	}

	@Override
	public void unequip(Creature c) {
		c.getArmourSet().takeOff(slot, this);
		equipped = false;
	}

	@Override
	public boolean isEquipped() {
		return equipped;
	}
	
	@Override
	public void drop(Creature from) {
		unequip(from);
		super.drop(from);
	}
}
