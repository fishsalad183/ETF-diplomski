package objects;

import concepts.GameObject;
import concepts.Vector;

public abstract class Floor extends GameObject {
    
    public static final double FLOOR_HEIGHT = 1;
    
    public Floor(Vector position) {
        super(position);
    }
    
}
