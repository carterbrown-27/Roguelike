public final class Weapon extends Item implements Equippable {
	private boolean equipped = false;
	double damage;
	double accuracy;
	double weight;
	
	Weapon(String id){
		super(id);
		// TODO: weapon init here:
	}
	
	public double getDamage() {
		return damage;
	}
	// TODO: add a dmg calculation here (maybe)
	
	public double getAccuracy() {
		return accuracy;
	}
	
	public double getWeight() {
		return weight;
	}
	
	@Override
	public String[] listPrompts() {
		// TODO (A) Implement
		return new String[] {};
	}
	
	@Override
	public void equip(Entity e) {
		// TODO (A) Implement
		
	}

	@Override
	public void unequip(Entity e) {
		// TODO (A) Implement
		
	}
	
	@Override
	public void drop(Entity e) {
		unequip(e);
		super.drop(e);
		// TODO (A) Implement
		// drop to floor.
	}
	
	@Override
	public boolean isEquipped() {
		return this.equipped;
	}
}
