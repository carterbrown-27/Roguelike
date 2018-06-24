public class AI {
	public Pathfinder p = new Pathfinder();
	public Entity e;
	public int t = 0;
	public String type = "def";
	public ActionLibrary lib;
	
	AI(Entity e){
		this.e = e;
		this.type = e.creature.NAME;
		this.lib = new ActionLibrary(this.e);
	}
	
	public void takeTurn(){
		if(type.equals("rat")){
			rat();
		}
	}
	
	// what one turn of the given creature looks like
	
	@SuppressWarnings("unused")
	public void rat(){
		if(false /* is Adjacent to target */){
			lib.melee();
		}else if(false /* 3+ spaces away*/){
			
		}else{
			lib.move();
		}
	}
	
}
