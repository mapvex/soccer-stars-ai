package bot.simulation;

import bot.Bot;
import bot.Constants;
import bot.misc.Helper;
import bot.simulation.objects.Movable;
import bot.simulation.objects.Vector2d;
import bot.simulation.objects.Vector3d;

import java.util.Vector;

public class SimulationRunner extends Thread {
    public Bot bot;
    public Simulation simulation;
    public Vector<SimulationResult> results;

    public double xCoordinates[];
    public double yCoordinates[];
    public double startAngle;
    public double endAngle;
    public double step;

    public SimulationRunner(Bot bot, double xCoordinates[], double yCoordinates[], double startAngle, double endAngle, double step) {
        this.bot = bot;
        this.simulation = new Simulation(bot);
        this.results = new Vector<SimulationResult>();
        this.xCoordinates = xCoordinates;
        this.yCoordinates = yCoordinates;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.step = step;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            for (double theta = startAngle; theta < endAngle; theta += step) {
                results.add(runSimulation(i, Constants.MAX_POWER, theta, bot != null));
                //System.out.println("puck: " + i + " simulation!");
            }
        }

        //System.out.println("results length: " + results.size());
    }

    // Run a simulation with given values
    SimulationResult runSimulation(int puckId, double power, double angle, boolean gui) {
        // set up the simulation
        populateMovables();

        // give velocity to the disc we are shooting with
        Movable currentPuck = simulation.movables.get(puckId);
        shotToVelocity(currentPuck, power, angle, new Vector2d());
        currentPuck.velocity = currentPuck.velocity.multiply(currentPuck.velocity, 1.0);

        // run the simulation
        simulation.result.puckId = puckId;
        simulation.result.power = power;
        simulation.result.angle = angle;
        SimulationResult result = simulation.run(gui);
        simulation.clear();
        return result;
    }

    // Convert power and angle to velocity
    public void shotToVelocity(Movable _arg_1, double _arg_2, double _arg_3, Vector2d _arg_4) {
        double _local_6 = (_arg_2 / Constants.MAX_POWER);
        double _local_7 = Helper.toPrecision((Constants.MIN_POWER + ((1 - Math.sqrt((1 - _local_6))) * (Constants.MAX_POWER - Constants.MIN_POWER))));
        if (_local_7 == Constants.MAX_POWER) {
            // apply extra max power here
        }
        ;
        Vector2d _local_8 = Vector2d.multiply(_arg_4, Constants._SafeStr_1103);
        _arg_1.velocity = new Vector2d(Helper.toPrecision((Math.cos(_arg_3) * _local_7)), Helper.toPrecision((Math.sin(_arg_3) * _local_7)));
        _arg_1.spin = new Vector3d((-(Math.sin(_arg_3)) * ((-(_local_8.y) * _local_7) / _arg_1.radius)), (Math.cos(_arg_3) * ((-(_local_8.y) * _local_7) / _arg_1.radius)), ((_local_8.x * _local_7) / _arg_1.radius));
    }

    // Feed movables to a simulation
    public void populateMovables() {
        simulation.movables.clear();
        for (int i = 0; i < xCoordinates.length; i++) {
            Movable movable = new Movable(bot, simulation);
            movable.position.x = xCoordinates[i];
            movable.position.y = yCoordinates[i];

            if (i == 0) { // ball
                movable.type = movable.TYPE_BALL;
                movable.radius = Constants.BALL_RADIUS;
                movable.mass = Constants.BALL_MASS;
                movable.restitution = Constants.BALL_RESTITUTION;
                movable.friction = Constants.BALL_FRICTION;
            } else { // puck
                movable.type = movable.TYPE_PUCK;
                movable.radius = Constants.PUCK_RADIUS;
                movable.mass = Constants.PUCK_MASS;
                movable.restitution = Constants.PUCK_RESTITUTION;
                movable.friction = Constants.PUCK_FRICTION;
            }

            simulation.movables.add(movable);
        }
    }
}
