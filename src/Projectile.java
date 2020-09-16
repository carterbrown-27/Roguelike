import java.awt.*;

public class Projectile extends Entity {
    private Missile i;

    Projectile(Missile i, Point p, Map m){
        super("<projectile>", p, m);
        super.setPassable(true);
        this.i = i;
    }

    public Missile getMissile(){
        return i;
    }
}
