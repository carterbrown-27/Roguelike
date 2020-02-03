import java.util.List;

public final class Weapon extends Item implements Equippable {
	private boolean equipped = false;
	double damage;
	double accuracy;
	double weight;
	
	Weapon(String id){
		super(id);
		
		// TODO (A) introduce logic to see if it is weilded/not.
		super.addPrompt('U', "(u)nwield");
		super.addPrompt('W', "(w)ield");
		// TODO: weapon init here:
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
