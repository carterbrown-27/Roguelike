public class AI {
	public Pathfinder p = new Pathfinder();
	public Creature c;
	public int t = 0;
	public ActionLibrary lib;
	public Player player;

	AI(Creature _c){
		System.out.println("configuring AI...");
		this.c = _c;
		this.lib = new ActionLibrary(this.c);
		player = Main.player;
		System.out.println("done.");
	}

	public boolean takeTurn(){
		c.upkeep();
		// TODO: add AI
		boolean waiting = basic();
		c.endStep();
		return waiting;
	}

	// what one turn of the given creature looks like

	// 0 is wait
	// 1 is sucess
	// 2 is fail
	public boolean basic(){
		if(c.isAdjacentTo(player.getPos())){
//			if (e.map.isSandwich(e.getX(), e.getY())) {
//				for (Entity e2 : e.map.entities.values()) {
//					if (e.isAdjacentTo(e2.getPos())) {
//						// move out of way
//						int dir = e.getDir(player.getPos());
//						Point[] p = player.adjacentPositions();
//						if(dir==7||dir==0||dir==4){
//							lib.newPath(p[2],true);
//						}else if(dir==4||dir==1||dir==5){
//							lib.newPath(p[3],true);
//						}else if(dir==6||dir==2||dir==5){
//							lib.newPath(p[0],true);
//						}else if(dir==7||dir==3||dir==6){
//							lib.newPath(p[1],true);
//						}
//
//						if(lib.pBFS==null){
//							lib.updatePath();
//							break;
//						}else{
//							return lib.move(true);
//						}
//					}
//				}
//			}
			
			if(lib.melee(player,1)){
				Main.appendText("The "+c.getName()+" hits you!");
			}else{
				Main.appendText("The "+c.getName()+" misses you.");				
			}
			return true;
		}else if(lib.distance == 2 && c.getSP() >= 3){
			lib.lunge();
			return true;
		}else{
			return lib.move(false);
		}
	}
}
