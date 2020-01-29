public final class Armour extends Item implements Equippable {
	private boolean equipped = false;
	private double defence;
	
	Armour(String id) {
		super(id);
	}
	
	public double getDefence() {
		return defence;
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
	public boolean isEquipped() {
		// TODO (A) Implement
		return equipped;
	}
}
