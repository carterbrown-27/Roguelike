public interface Consumable {
	
	public void use(Entity e);
	
	public int getAmount();
	
	public void setAmount(int amt);
	
	public void changeAmount(int amt);
	
	// public int getCommonStackSize();
}
