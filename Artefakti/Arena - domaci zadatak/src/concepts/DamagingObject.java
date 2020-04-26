package concepts;

public abstract class DamagingObject extends GameObject {

    private boolean damaging = true;

    public DamagingObject(Vector position) {
        super(position);
    }

    public boolean isDamaging() {
        return damaging;
    }

    public void setDamaging(boolean damaging) {
        this.damaging = damaging;
    }

}
