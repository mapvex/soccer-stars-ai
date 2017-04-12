package bot.simulation.objects;

public class Vector2d {
    public double x;
    public double y;

    public Vector2d() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public static Vector2d multiply(Vector2d vec, double multiplier) {
        return new Vector2d((vec.x * multiplier), (vec.y * multiplier));
    }

    public static Vector2d sum(Vector2d vec1, Vector2d vec2) {
        return new Vector2d(vec1.x + vec2.x, vec1.y + vec2.y);
    }

    public Vector2d scale(double _arg_1) {
        this.x = (this.x * _arg_1);
        this.y = (this.y * _arg_1);
        return (this);
    }
}
