public interface Equippable {
	public final boolean equippable = true;
	
	public abstract void equip(Entity e);
	
	public abstract void unequip(Entity e);
	
	public abstract boolean isEquipped();
	
	public abstract boolean isEquippable();
}
