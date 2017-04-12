package bot.simulation.objects;

public class Rectangle {
    public double x, y;
    public double width, height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double left() {
        return x;
    }

    public double right() {
        return x + width;
    }

    public double top() {
        return y;
    }

    public double bottom() {
        return y + height;
    }
}
