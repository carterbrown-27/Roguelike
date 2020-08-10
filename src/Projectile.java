import java.awt.*;

public class Projectile extends Entity {
    Projectile(Item i, Point p, Map m){
        super("<projectile>", p, m);
        super.setPassable(true);
    }
}
