public class AI {
	private Creature c;
	private ActionLibrary lib;

	AI(Creature _c){
		// ("configuring AI...");
		this.c = _c;
		this.lib = new ActionLibrary(this.c);
		// TODO (A) Replace this with "influence map" from Map
		// ("done.");
	}

	public boolean takeTurn(){
		
		c.upkeep();
		// TODO (+) add AI
		boolean waiting = basic();
		c.endStep();
		
		return waiting;
	}

	// what one turn of the given creature looks like

	// 0 is wait
	// 1 is success
	// 2 is fail
	public boolean basic(){
		if(c.isAdjacentTo(Main.getPlayer().getPos())){
			if(lib.melee(Main.getPlayer(),1)){
				Main.getView().appendText("The "+c.getName()+" hits you!");
			}else{
				Main.getView().appendText("The "+c.getName()+" misses you.");
			}
			return true;
		}else{
			return lib.move(false);
		}
	}
}
