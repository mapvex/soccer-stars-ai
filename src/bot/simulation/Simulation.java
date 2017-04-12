package bot.simulation;

import bot.Bot;
import bot.Constants;
import bot.misc.Helper;
import bot.simulation.collision.CollisionManager;
import bot.simulation.collision.CollisionResult;
import bot.simulation.collision.MovableMovableCollision;
import bot.simulation.collision.MovableNumberCollision;
import bot.simulation.objects.*;
import bot.simulation.objects.Point;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Vector;

public class Simulation {
    public Bot bot;

    public Vector<Movable> movables = new Vector<Movable>();
    public SimulationResult result;

    public boolean usingGUI;
    public boolean running;

    public static long simulationStartTime;

    public static double getTimer() {
        return (double) (System.currentTimeMillis() - simulationStartTime);
    }

    public Simulation(Bot bot) {
        this.bot = bot;
        clear();

        CollisionManager.buildBounds(Constants.FIELD_WIDTH, Constants.FIELD_HEIGHT, Constants.CORNER_RADIUS);
        CollisionManager.tableShape = getTableShape(Constants.FIELD_WIDTH, Constants.FIELD_HEIGHT, Constants.SCORE_WIDTH, Constants.SCORE_HEIGHT, Constants.CORNER_RADIUS);
        CollisionManager.tableRestitution = 1.0;
    }

    public void clear() {
        movables.clear();
        running = false;
        result = null;
        result = new SimulationResult();
    }

    public boolean isUsingGUI() {
        return usingGUI && bot != null;
    }

    public void render(Graphics2D g2) {
        if (!isUsingGUI()) {
            return;
        }

        // Draw movables
        for (Movable movable : movables) {
            movable.render(g2);
        }

        // Draw table shape
        g2.setColor(Color.BLACK);
        for (int i = 0; i < CollisionManager.tableShape.size(); i++) {
            if (i == CollisionManager.tableShape.size() - 1) {
                continue;
            }

            Point currentPoint = CollisionManager.tableShape.get(i);
            Point nextPoint = CollisionManager.tableShape.get(i + 1);
            int x1 = bot.helper.toScreenX(currentPoint.x);
            int y1 = bot.helper.toScreenY(currentPoint.y);
            int x2 = bot.helper.toScreenX(nextPoint.x);
            int y2 = bot.helper.toScreenY(nextPoint.y);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    public SimulationResult run(boolean gui) {
        running = true;
        usingGUI = gui;
        simulationStartTime = System.currentTimeMillis(); // improve precision

        int maxTicks = 2000;
        int i = 0;
        while (running) {
            if (i == maxTicks || result.badShot || result.scored) {
                break;
            }

            process(5.0 / 1000.0);

            if (isUsingGUI()) {
                bot.overlay.repaint();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            i++;
        }

        analyzeOutcome();
        return result;
    }

    public void analyzeOutcome() {
        // ----- mark the ball position in the outcome -----
        result.ballX = movables.get(0).position.x;
        result.ballY = movables.get(0).position.y;

        // ----- look for our defensive status ------
        int movablesInGoalBox = 0;
        for (Movable movable : movables) {
            if (movable.type == Movable.TYPE_BALL) {
                continue;
            }

            if (isInOwnGoalBox(movable)) {
                movablesInGoalBox++;
            }
        }
        result.defendingPucks = movablesInGoalBox;

        // ----- look if the ball is in a bad position for the enemy -----
        if (Helper.areCoordinatesInBadPosition(result.ballX, result.ballY)) {
            result.ballNearEnemyWall = true;
        }
    }

    public boolean isInOwnGoalBox(Movable movable) {
        double x = movable.position.x;
        double y = movable.position.y;

        return Helper.areCoordinatesInOwnGoalBox(x, y);
    }

    public void registerCollision(CollisionResult res) {
        result.collisions++;
    }

    private void process(double elapsedSeconds) {
        //System.out.println("process");
        double _local_6 = 0.0;
        CollisionResult _local_7;
        int _local_8;
        boolean _local_3 = false;
        double _local_4 = elapsedSeconds;
        int _local_5 = this.movables.size();

        while (_local_4 > Constants.SMALL_VALUE) {
            _local_4 = (_local_4 - _local_6);
            _local_6 = _local_4;

            if (_local_6 < Constants.SMALL_VALUE) {
                break;
            }

            //System.out.println("doing collision detection with time: " + _local_6);
            _local_7 = CollisionManager.doCollisionDetection(movables, _local_6);
            if (_local_7 != null) {
                _local_6 = _local_7.time;
            }

            _local_8 = 0;
            while (_local_8 < _local_5) {
                if (movables.get(_local_8).isMovingOrSpinning() && movables.get(_local_8).hasFieldState()) {
                    //System.out.println("moving movable " + _local_8 + " with time " + _local_6);
                    movables.get(_local_8).move(_local_6);
                    _local_3 = true;
                }
                ;

                _local_8++;
            }

            if (_local_7 != null) {
                registerCollision(_local_7);
                CollisionManager.handleCollision(_local_7);
            }
        }

        if (!_local_3) {
            // nothing moved
            running = false;
        }

        int i = 0;
        while (i < movables.size()) {
            Movable movable = movables.get(i);

            if (movable.hasFieldState()) {
                applyFriction(movable, elapsedSeconds, Constants.FRICTION_FACTOR);
            }

            i++;
        }
    }

    public void applyFriction(Movable _arg_1, double elapsedSeconds, double _arg_3) {
        double _local_5;
        double _local_8;
        double _local_9;
        double _local_10;
        if (!_arg_1.isMovingOrSpinning()) {
            return;
        }
        ;
        //System.out.println("spin: " + _arg_1.spin.x + " " + _arg_1.spin.y + " " + _arg_1.spin.z);
        bot.simulation.objects.Point _local_4 = new bot.simulation.objects.Point((-(_arg_1.velocity.x) - (_arg_1.spin.y * _arg_1.radius)), (-(_arg_1.velocity.y) + (_arg_1.spin.x * _arg_1.radius)));
        //System.out.println(_arg_1.velocity.x + " " + _arg_1.velocity.y + " point: " + _local_4.x + " " + _local_4.y + " pointlength: " + _local_4.getLength());
        double _local_6 = ((Constants._SafeStr_1278 * _local_4.getLength()) / (Constants._SafeStr_1165 * Constants._SafeStr_1279));
        if (_local_6 > Constants.SMALL_VALUE) {
            //System.out.println("local_6 larger than epsilon: " + _local_6);
            _local_8 = Math.min(_local_6, elapsedSeconds);
            _local_5 = ((Constants._SafeStr_1165 * Constants._SafeStr_1279) * _local_8);

            _local_4.x = (_local_4.x * (_local_5 / _local_4.getLength()));
            _local_4.y = (_local_4.y * (_local_5 / _local_4.getLength()));
            _arg_1.velocity.x = (_arg_1.velocity.x + _local_4.x);
            _arg_1.velocity.y = (_arg_1.velocity.y + _local_4.y);
            _arg_1.spin.x = (_arg_1.spin.x - ((Constants._SafeStr_1164 * _local_4.y) / _arg_1.radius));
            _arg_1.spin.y = (_arg_1.spin.y + ((Constants._SafeStr_1164 * _local_4.x) / _arg_1.radius));
        }
        if (_local_6 < elapsedSeconds) {
            //System.out.println("local_6 smaller than elapsedTime: " + _local_6);
            _local_9 = (elapsedSeconds - _local_6);
            _local_5 = (((_arg_3 * _arg_1.friction) * Constants._SafeStr_1279) * _local_9);

            _local_10 = Math.max(0.0, (1.0 - (_local_5 / _arg_1.velocity.getLength())));
            _arg_1.velocity = Vector2d.multiply(_arg_1.velocity, _local_10);
            _arg_1.spin.x = (_arg_1.velocity.y / _arg_1.radius);
            _arg_1.spin.y = (-(_arg_1.velocity.x) / _arg_1.radius);
        }
        double _local_7 = (((Constants._SafeStr_1280 / Constants._SafeStr_1164) * Constants._SafeStr_1279) * elapsedSeconds);
        if (_arg_1.spin.z > 0.0) {
            _arg_1.spin.z = Math.max(0.0, (_arg_1.spin.z - _local_7));
        } else {
            _arg_1.spin.z = Math.min(0.0, (_arg_1.spin.z + _local_7));
        }
    }


    public Vector<Point> getTableShape(double width, double height, double scoreWidth, double scoreHeight, double cornerRadius) {
        Point _local_6;
        int _local_7 = 4;
        double _local_8 = (Math.PI / 2);
        double _local_9 = (_local_8 / _local_7);
        double _local_10 = ((height / 2) - ((height / 2) - (scoreHeight / 2)));
        Vector<Point> _local_11 = new Vector<Point>();
        _local_11.add(new Point(((-(width) / 2) + cornerRadius), (-(height) / 2)));
        _local_11.add(new Point(((width / 2) - cornerRadius), (-(height) / 2)));
        _local_6 = new Point(((width / 2) - cornerRadius), ((-(height) / 2) + cornerRadius));
        int _local_12 = _local_7;
        while (_local_12 > 0) {
            _local_11.add(_SafeStr_1068(_local_6, cornerRadius, (-(_local_9) * _local_12)));
            _local_12--;
        }
        ;
        _local_11.add(new Point((width / 2), ((-(height) / 2) + cornerRadius)));

        _local_11.add(new Point((width / 2), -(_local_10)));
        _local_11.add(new Point(((width / 2) + scoreWidth), -(_local_10)));
        _local_11.add(new Point(((width / 2) + scoreWidth), (-(_local_10) + scoreHeight)));
        _local_11.add(new Point((width / 2), (-(_local_10) + scoreHeight)));
        _local_11.add(new Point((width / 2), ((height / 2) - cornerRadius)));
        _local_6 = new Point(((width / 2) - cornerRadius), ((height / 2) - cornerRadius));
        _local_12 = _local_7;

        while (_local_12 > 0) {
            _local_11.add(_SafeStr_1068(_local_6, cornerRadius, ((-(_local_9) * _local_12) + (Math.PI / 2))));
            _local_12--;
        }
        ;
        _local_11.add(new Point(((width / 2) - cornerRadius), (height / 2)));
        _local_11.add(new Point(((-(width) / 2) + cornerRadius), (height / 2)));
        _local_6 = new Point(((-(width) / 2) + cornerRadius), ((height / 2) - cornerRadius));
        _local_12 = _local_7;
        while (_local_12 > 0) {
            _local_11.add(_SafeStr_1068(_local_6, cornerRadius, ((-(_local_9) * _local_12) + Math.PI)));
            _local_12--;
        }
        ;
        _local_11.add(new Point((-(width) / 2), ((height / 2) - cornerRadius)));

        _local_11.add(new Point((-(width) / 2), (-(_local_10) + scoreHeight)));
        _local_11.add(new Point(((-(width) / 2) - scoreWidth), (-(_local_10) + scoreHeight)));
        _local_11.add(new Point(((-(width) / 2) - scoreWidth), -(_local_10)));
        _local_11.add(new Point((-(width) / 2), -(_local_10)));
        _local_11.add(new Point((-(width) / 2), ((-(height) / 2) + cornerRadius)));
        _local_6 = new Point(((-(width) / 2) + cornerRadius), ((-(height) / 2) + cornerRadius));
        _local_12 = _local_7;
        while (_local_12 > 0) {
            _local_11.add(_SafeStr_1068(_local_6, cornerRadius, ((-(_local_9) * _local_12) - (Math.PI / 2))));
            _local_12--;
        }
        ;
        _local_11.add(new Point(((-(width) / 2) + cornerRadius), (-(height) / 2)));
        return (_local_11);
    }

    public Point _SafeStr_1068(Point _arg_1, double _arg_2, double _arg_3) {
        return (new Point((_arg_1.x + (_arg_2 * Math.cos(_arg_3))), (_arg_1.y + (_arg_2 * Math.sin(_arg_3)))));
    }
}
