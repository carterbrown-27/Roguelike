import org.json.JSONObject;

public class Food extends Item implements Consumable {
	double foodValue;
	
	Food(String id) {
		super(id);
		super.addPrompt('e', "(e)at");
		super.setStackable(true);
		JSONObject itemData = super.getItemData();
		foodValue = itemData.getDouble("foodValue");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void use(Creature c) {
		// TODO Auto-generated method stub
		c.changeSAT(+foodValue);
		c.getInv().removeOne(this.getInventoryID());
	}
}
