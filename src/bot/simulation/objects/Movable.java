package bot.simulation.objects;

import bot.Bot;
import bot.Constants;
import bot.simulation.Simulation;

import java.awt.*;

public class Movable {
    public static final int STATE_ON_FIELD_UNUSED = 0; // on field
    public static final int STATE_ON_FIELD = 1;
    public static final int STATE_OFF_FIELD = 2; // off field

    public static final int TYPE_BALL = 0;
    public static final int TYPE_PUCK = 1;
    public Bot bot;
    public Simulation simulation;

    public int type;
    public int state;
    public double radius;
    public double mass;
    public double friction;
    public double restitution;

    public Vector2d position;
    public Vector2d velocity;
    public Vector3d spin;

    public Movable(Bot bot, Simulation simulation) {
        this.bot = bot;
        this.simulation = simulation;

        this.position = new Vector2d();
        this.velocity = new Vector2d();
        this.spin = new Vector3d();

        this.state = STATE_ON_FIELD;
    }

    public void move(double elapsedSeconds) {
        // if not zero...
        this.position = Vector2d.sum(this.position, Vector2d.multiply(this.velocity, elapsedSeconds));
    }

    public void render(Graphics2D g2) {
        if (bot == null) {
            return;
        }

        g2.setColor(Color.GREEN);

        int X = bot.helper.toScreenX(position.x - radius);
        int Y = bot.helper.toScreenY(position.y + radius);
        int scaledRadius = bot.helper.toScreenScale(radius * 2.0);

        g2.fillOval(X, Y, scaledRadius, scaledRadius);
    }

    public boolean isMovingOrSpinning() {
        return Math.abs(this.velocity.x) > Constants.SMALL_VALUE || Math.abs(this.velocity.y) > Constants.SMALL_VALUE || Math.abs(this.spin.x) > Constants.SMALL_VALUE || Math.abs(this.spin.y) > Constants.SMALL_VALUE || Math.abs(this.spin.z) > Constants.SMALL_VALUE;
    }

    public boolean isMoving() {
        return Math.abs(this.velocity.x) > Constants.SMALL_VALUE || Math.abs(this.velocity.y) > Constants.SMALL_VALUE;
    }

    public boolean hasFieldState() {
        return ((this.state == STATE_ON_FIELD) || (this.state == STATE_OFF_FIELD));
    }

    public void ballInGoal(String _arg_1) {
        if (this.state == STATE_ON_FIELD) {
            this.state = Movable.STATE_OFF_FIELD;
            this.velocity.scale(0.1);
            //System.out.println("ballInGoal!!");

            if (this.simulation != null) {
                if (_arg_1 == "TableEvent.LOCAL_PLAYER_SCORED") {
                    simulation.result.scored = true;
                } else {
                    simulation.result.badShot = true; // own goal
                }
            }
        }
        ;
    }
}
