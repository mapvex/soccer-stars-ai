package bot.simulation;

public class SimulationResult {
    public int puckId;
    public double power;
    public double angle;
    public boolean scored;
    public boolean badShot;

    public int defendingPucks;
    public int collisions;
    public boolean ballNearEnemyWall;
    public double ballX, ballY;

    public SimulationResult() {
        scored = false;
        badShot = false;
        puckId = 0;
        angle = 0.0;
        power = 0.0;

        defendingPucks = 0;
        collisions = 0;
        ballNearEnemyWall = false;
        ballX = 0.0;
        ballY = 0.0;
    }
}
