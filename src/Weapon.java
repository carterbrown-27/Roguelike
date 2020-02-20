import java.util.List;

import org.json.JSONObject;

public final class Weapon extends Item implements Equippable {
	private boolean equipped = false;
	private double damage;
	private double accuracy;
	private double weight;
	
	Weapon(String id){
		super(id);
		
		// TODO (A) introduce logic to see if it is weilded/not.
		super.addPrompt('u', "(u)nwield");
		super.addPrompt('w', "(w)ield");
		// TODO: weapon init here:
		damage = super.getItemData().getDouble("damage");
		accuracy = super.getItemData().getDouble("accuracy");
		weight = super.getItemData().getDouble("weight");
	}
	
	// TODO (+) add a dmg calculation here (maybe)
	public double getDamage() {
		return damage;
	}
	
	public double getAccuracy() {
		return accuracy;
	}
	
	public double getWeight() {
		return weight;
	}
	
	@Override
	public void equip(Creature c) {
		// TODO (A) Implement
		c.weapon = this;
	}

	@Override
	public void unequip(Creature c) {
		// TODO (A) Implement
		c.weapon = null;
	}
	
	@Override
	public void drop(Entity e) {
		// TODO (T) TEMP
		if(e instanceof Creature) unequip((Creature) e);
		super.drop(e);
		// TODO (A) Implement
		// drop to floor.
	}
	
	@Override
	public boolean isEquipped() {
		return this.equipped;
	}
}
