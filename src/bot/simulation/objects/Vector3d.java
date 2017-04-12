package bot.simulation.objects;

public class Vector3d {
    public double x;
    public double y;
    public double z;

    public Vector3d() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
