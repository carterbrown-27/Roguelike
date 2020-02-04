public interface Consumable {
	
	public void use(Creature c);
	
	public int getAmount();
	
	public void setAmount(int amt);
	
	public void changeAmount(int amt);
	
	// public int getCommonStackSize();
}
