package concepts;

public class Vector {

    private double x;
    private double y;
    private double z;

    public Vector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector duplicate() {
        return new Vector(x, y, z);
    }

    public Vector scalarMultiply(double value) {
        x *= value;
        y *= value;
        z *= value;
        return this;
    }

    public Vector add(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Vector add(Vector v) {
        return add(v.getX(), v.getY(), v.getZ());
    }

    public Vector set(double _x, double _y, double _z) {
        x = _x;
        y = _y;
        z = _z;
        return this;
    }

    public Vector set(Vector v) {
        return set(v.getX(), v.getY(), v.getZ());
    }

    public double getX() {
        return x;
    }

    public Vector setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public Vector setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return z;
    }

    public Vector setZ(double z) {
        this.z = z;
        return this;
    }
}
