package bot.simulation.collision;

import bot.Constants;
import bot.simulation.Simulation;
import bot.simulation.objects.*;

import java.util.Vector;

public class CollisionManager {
    public static Vector<Point> tableShape;
    public static Rectangle tableCollisionBounds;
    public static Rectangle goalBounds;
    public static double tableRestitution;
    public static Rectangle goalRect;

    public static void buildBounds(double _arg_1, double _arg_2, double _arg_3) // _SafeStr_1098
    {
        tableCollisionBounds = new Rectangle((-(_arg_1 - _arg_3) / 2), (-(_arg_2 - _arg_3) / 2), (_arg_1 - _arg_3), (_arg_2 - _arg_3));
        goalBounds = new Rectangle((-(_arg_1) / 2), (-(_arg_2) / 2), _arg_1, _arg_2);
        CollisionManager.goalRect = new Rectangle(0, 0, Constants.SCORE_WIDTH, Constants.SCORE_HEIGHT);
    }

    public static void handleCollision(CollisionResult _arg_1) {
        Movable _local_2;
        Movable _local_3;
        MovablePointCollision _local_4;
        double _local_5;
        double _local_6;
        double _local_7;
        _arg_1.time = (_arg_1.time + Simulation.getTimer()); // ud
        if (_arg_1 instanceof MovableMovableCollision) {
            _local_2 = ((MovableMovableCollision) _arg_1).item1;
            _local_3 = ((MovableMovableCollision) _arg_1).item2;

            ballBallCollision(_local_2, _local_3);
        } else {
            if ((_arg_1 instanceof MovableNumberCollision)) {
                _local_2 = ((MovableNumberCollision) _arg_1).ball; // used for detector, not needed here

                ballLineCollision(((MovableNumberCollision) _arg_1).ball, ((MovableNumberCollision) _arg_1)._SafeStr_1157());
            } else {
                if ((_arg_1 instanceof MovablePointCollision)) {
                    _local_4 = (MovablePointCollision) _arg_1;
                    _local_5 = (_local_4._SafeStr_1158().x - _local_4.ball.position.x);
                    _local_6 = (_local_4._SafeStr_1158().y - _local_4.ball.position.y);
                    _local_7 = -(calculateTheta(_local_6, -(_local_5)));
                    //System.out.println("pointcollision atanfunc: " + Math.toDegrees(_local_7));
                    ballLineCollision(_local_4.ball, _local_7);
                    return;
                }
            }
        }
    }

    public static CollisionResult doCollisionDetection(Vector<Movable> movables, double elapsedSeconds) {
        CollisionResult _local_5;
        CollisionResult _local_3 = null;
        int i = 0;
        while (i < movables.size()) {
            if (movables.get(i).hasFieldState()) // _arg_1[_local_4].hasFieldState
            {
                //System.out.println("findFirstCollisionBall" + i);
                _local_5 = findFirstCollisionBall(movables.get(i), movables, elapsedSeconds);
                if (_local_5 != null) {
                    _local_3 = _local_5;
                    elapsedSeconds = _local_3.time;
                    i++;
                    continue;
                }
            }

            i++;
        }

        return (_local_3);
    }

    private static void ballBallCollision(Movable first, Movable second) {
        double _local_8;
        double _local_14;
        double distanceX = (first.position.x - second.position.x);
        double distanceY = (first.position.y - second.position.y);
        double radiiSum = (first.radius + second.radius);
        double distance = Math.sqrt(((distanceX * distanceX) + (distanceY * distanceY)));
        double _local_7 = ((radiiSum - distance) / distance);
        _local_8 = (distanceX * _local_7);
        double _local_9 = (distanceY * _local_7);
        double firstInverseMass = (1 / first.mass);
        double secondInverseMass = (1 / second.mass);
        double totalInverseMass = (firstInverseMass + secondInverseMass);
        double _local_13 = (firstInverseMass / totalInverseMass);
        _local_14 = (secondInverseMass / totalInverseMass);
        double _local_15 = Math.atan2(distanceX, distanceY);
        if (first.type == Movable.TYPE_PUCK) // first.type == _SafeStr_143.DYNAMIC
        {
            discCollisionSpinEffect(first, (-(_local_15) - Constants.DEGREES_90));
        }

        if (second.type == Movable.TYPE_PUCK) // second.type == _SafeStr_143.DYNAMIC
        {
            discCollisionSpinEffect(second, (-(_local_15) + Constants.DEGREES_90));
        }

        first.position = Vector2d.sum(first.position, new Vector2d((_local_8 * _local_13), (_local_9 * _local_13)));
        second.position = Vector2d.sum(second.position, new Vector2d((_local_8 * _local_14), (_local_9 * _local_14)));
        manageBounce(first, second);
        return;
    }

    private static double ballBallCollisionTime(Movable _arg_1, Movable _arg_2, double elapsedSeconds) {
        double _local_4 = (_arg_1.radius + _arg_2.radius);
        double _local_5 = (_arg_2.position.x - _arg_1.position.x);
        double _local_6 = (_arg_2.position.y - _arg_1.position.y);
        double _local_7 = (_arg_2.velocity.x - _arg_1.velocity.x);
        double _local_8 = (_arg_2.velocity.y - _arg_1.velocity.y);
        double _local_9 = ((_local_7 * _local_7) + (_local_8 * _local_8));
        double _local_10 = (2 * ((_local_5 * _local_7) + (_local_6 * _local_8)));
        double _local_11 = (((_local_5 * _local_5) + (_local_6 * _local_6)) - (_local_4 * _local_4));
        double _local_12 = ((-(_local_10) - Math.sqrt(((_local_10 * _local_10) - ((4 * _local_9) * _local_11)))) / (2 * _local_9));
        if ((((_local_12 < 0) || ((_local_12 - Constants.SMALL_VALUE) > elapsedSeconds)) || (_local_10 >= 0))) {
            return (Double.POSITIVE_INFINITY);
        }

        return (_local_12);
    }

    public static void ballLineCollision(Movable _arg_1, double _arg_2) // arg2 = degrees?
    {
        //System.out.println("we should now bounce in angle: " + Math.toDegrees(_arg_2));
        double _local_3;
        double _local_4;
        double _local_7;
        _local_3 = Math.cos(_arg_2);
        _local_4 = Math.sin(_arg_2);
        double _local_5 = ((_local_3 * _arg_1.velocity.x) - (_local_4 * _arg_1.velocity.y)); // -yvelocity
        double _local_6 = ((_local_4 * _arg_1.velocity.x) + (_local_3 * _arg_1.velocity.y)); // +xvelocity
        //System.out.println(" local6: " + _local_6);
        _local_7 = ((_local_3 * _arg_1.spin.x) - (_local_4 * _arg_1.spin.y));
        double _local_8 = ((_local_4 * _arg_1.spin.x) + (_local_3 * _arg_1.spin.y));
        _local_7 = (_local_7 - ((_local_6 * Constants._SafeStr_1163) / _arg_1.radius));
        double _local_9 = (_local_5 - (_arg_1.spin.z * _arg_1.radius));
        double _local_10 = Math.abs(_local_9);
        double _local_11 = ((_local_9 > 0.0) ? 1.0 : -1.0);
        double _local_12 = (_local_10 / Constants._SafeStr_1164);
        double _local_13 = ((2.0 * Constants._SafeStr_1165) * Math.abs(_local_6));
        double _local_14 = (-(_local_11) * Math.min(_local_12, _local_13));
        _local_5 = (_local_5 + (_local_14 / Constants._SafeStr_1164));

        _arg_1.spin.z = (_arg_1.spin.z - ((Constants._SafeStr_1164 * _local_14) / _arg_1.radius));
        _local_6 = ((-(_local_6) * _arg_1.restitution) * tableRestitution);
        _arg_1.velocity.x = ((_local_3 * _local_5) + (_local_4 * _local_6)); // xvelocity = (0 * -yvelocity) + (1 * -xvelocity * restitution)
        _arg_1.velocity.y = ((-(_local_4) * _local_5) + (_local_3 * _local_6)); // yvelocity = (-1 * -yvelocity) + (0 * +xvelocity)
        _arg_1.spin.x = ((_local_3 * _local_7) + (_local_4 * _local_8));
        _arg_1.spin.y = ((-(_local_4) * _local_7) + (_local_3 * _local_8));
        return;
    }

    private static void discCollisionSpinEffect(Movable _arg_1, double _arg_2) {
        double _local_3 = Math.cos(_arg_2);
        double _local_4 = Math.sin(_arg_2);
        Vector2d _local_5 = new Vector2d(_arg_1.velocity.x, _arg_1.velocity.y);
        Vector3d _local_6 = new Vector3d(_arg_1.spin.x, _arg_1.spin.y, _arg_1.spin.z);
        double _local_7 = _arg_1.radius;
        Vector2d _local_8 = new Vector2d(((_local_3 * _local_5.x) - (_local_4 * _local_5.y)), ((_local_4 * _local_5.x) + (_local_3 * _local_5.y)));
        Vector2d _local_9 = new Vector2d(((_local_3 * _local_6.x) - (_local_4 * _local_6.y)), ((_local_4 * _local_6.x) + (_local_3 * _local_6.y)));
        _local_9.x = (_local_9.x - ((_local_8.y * 0.7) / _local_7));
        double _local_10 = (_local_8.x - (_local_6.z * _local_7));
        double _local_11 = Math.abs(_local_10);
        double _local_12 = ((_local_10 > 0) ? 1 : -1);
        double _local_13 = (_local_11 / Constants._SafeStr_1164);
        double _local_14 = ((2 * 0.2) * Math.abs(_local_8.y));
        double _local_15 = (-(_local_12) * Math.min(_local_13, _local_14));
        _local_6.z = (_local_6.z - ((Constants._SafeStr_1164 * _local_15) / _local_7));
        _arg_1.spin = _local_6;
    }

    private static double ballLineCollisionTime(Movable _arg_1, Point _arg_2, Point _arg_3, double _arg_4) {
        if (!_arg_1.isMoving()) {
            return (Double.POSITIVE_INFINITY);
        }

        double _local_5 = (_arg_3.x - _arg_2.x);
        double _local_6 = (_arg_3.y - _arg_2.y);
        double _local_7 = Math.sqrt(((_local_5 * _local_5) + (_local_6 * _local_6)));
        double _local_8 = (-(_local_6) / _local_7);
        double _local_9 = (_local_5 / _local_7);
        double _local_10 = ((_arg_1.position.x - _arg_2.x) - (_local_8 * _arg_1.radius));
        double _local_11 = ((_arg_1.position.y - _arg_2.y) - (_local_9 * _arg_1.radius));
        double _local_12 = ((_local_5 * -(_arg_1.velocity.y)) - (_local_6 * -(_arg_1.velocity.x)));
        if (_local_12 == 0) {
            return (Double.POSITIVE_INFINITY);
        }

        double _local_13 = ((-(_arg_1.velocity.y) * _local_10) - (-(_arg_1.velocity.x) * _local_11));
        double _local_14 = (_local_13 / _local_12);
        if (((_local_14 <= 0) || (_local_14 >= 1))) {
            return (Double.POSITIVE_INFINITY);
        }

        double _local_15 = ((_local_5 * _local_11) - (_local_6 * _local_10));
        double _local_16 = (_local_15 / _local_12);
        if (((_local_16 <= 0) || ((_local_16 - Constants.SMALL_VALUE) > _arg_4))) {
            return (Double.POSITIVE_INFINITY);
        }

        double _local_17 = ((_local_8 * _arg_1.velocity.x) + (_local_9 * _arg_1.velocity.y));
        if (_local_17 > 0) {
            return (Double.POSITIVE_INFINITY);
        }

        return (_local_16);
    }


    private static CollisionResult findFirstCollisionBall(Movable movable, Vector<Movable> movables, double elapsedSeconds) {
        double _local_4;
        double _local_5;
        double _local_6;
        double _local_7;
        double _local_8;
        int i;
        double _local_11;
        double _local_12;
        Point _local_13;
        Point _local_14;
        String _local_15;
        CollisionResult _local_9 = null;
        //long cur = System.nanoTime();
        if (movable.hasFieldState()) // _arg_1.hasFieldState
        {
            i = 0;
            while (i < movables.size()) {
                if (movables.get(i).hasFieldState()) // _arg_2[_local_10].hasFieldState
                {
                    _local_8 = ballBallCollisionTime(movable, movables.get(i), elapsedSeconds);
                    if (_local_8 < elapsedSeconds) // almost always false?
                    {
                        _local_11 = (movable.velocity.x - movables.get(i).velocity.x);
                        _local_12 = (movable.velocity.y - movables.get(i).velocity.y);
                        _local_9 = new MovableMovableCollision(movable, movables.get(i), _local_8, Math.sqrt(((_local_11 * _local_11) + (_local_12 * _local_12))));
                        //System.out.println("MOVABLEMOVABLECLASS!!!!!");
                        //ballBallCollision(movable, movables.get(i));
                        elapsedSeconds = _local_8;
                    }
                }
                i++;
            }
        }

        //System.out.println("coldet took: " + (System.nanoTime() - cur)/1000000.0);
        if (movable.velocity.x > 0) {
            _local_4 = movable.position.x;
            _local_6 = (_local_4 + (movable.velocity.x * elapsedSeconds));
        } else {
            _local_6 = movable.position.x;
            _local_4 = (_local_6 + (movable.velocity.x * elapsedSeconds));
        }

        if (movable.velocity.y > 0) {
            _local_5 = movable.position.y;
            _local_7 = (_local_5 + (movable.velocity.y * elapsedSeconds));
        } else {
            _local_7 = movable.position.y;
            _local_5 = (_local_7 + (movable.velocity.y * elapsedSeconds));
        }

        //System.out.println(movable.velocity.x + "checking bounds " + _local_4 + " < " +tableCollisionBounds.left());
        if (((((_local_4 < (tableCollisionBounds.left() + movable.radius)) || (_local_6 > (tableCollisionBounds.right() - movable.radius))) || (_local_5 < (tableCollisionBounds.top() + movable.radius))) || (_local_7 > (tableCollisionBounds.bottom() - movable.radius)))) {
            //System.out.println("oob");
            i = 0;
            while (i < tableShape.size()) {
                _local_13 = tableShape.get(i);
                _local_14 = tableShape.get(((i + 1) % tableShape.size()));
                _local_8 = ballLineCollisionTime(movable, _local_13, _local_14, elapsedSeconds);

                if (_local_8 < elapsedSeconds) {
                    _local_9 = new MovableNumberCollision(movable, -(calculateTheta((_local_14.x - _local_13.x), (_local_14.y - _local_13.y))), _local_8, movable.velocity.getLength());
                    //System.out.println("MOVABLENUMBERCLASS!!!!! degrees: " + Math.toDegrees(-(calculateTheta((_local_14.x - _local_13.x), (_local_14.y - _local_13.y)))));
                    elapsedSeconds = _local_8;
                }

                _local_8 = ballPointCollisionTime(movable, _local_13, elapsedSeconds);
                if (_local_8 < elapsedSeconds) {
                    _local_9 = new MovablePointCollision(movable, _local_13, _local_8, movable.velocity.getLength());
                    //System.out.println("MOVABLEPOINTCLASS!!!!! Point: (" + _local_13.x + "; " + _local_13.y + ") " + Math.toDegrees(_local_8));
                    elapsedSeconds = _local_8;
                }

                i++;
                continue;
            }

            if (movable.type == Movable.TYPE_BALL) // movable is ball
            {
                //System.out.println("goalBoundsLeft:" + goalBounds.left() + "goalBoundsRight:" + goalBounds.right() + " _local_4: " + _local_4 + " _local_6: " + _local_6 + "movable is ball with position " + _local_7 + " " + _local_5);
                if (((_local_4 < (goalBounds.left() - movable.radius)) || (_local_6 > (goalBounds.right() + movable.radius)))) {
                    //System.out.println("BALL ALMOST IN GOAL!");
                    if (((_local_7 > (-(goalRect.height) / 2)) && (_local_5 < (goalRect.height / 2)))) {
                        _local_15 = ((_local_4 < (goalBounds.left() - movable.radius)) ? "TableEvent.REMOTE_PLAYER_SCORED" : "TableEvent.LOCAL_PLAYER_SCORED");
                        movable.ballInGoal(_local_15);
                        //System.out.println("BALL IN GOAL!");
                    } else {
                        movable.velocity.x = -(movable.velocity.x);
                    }
                }

                if (((_local_5 < goalBounds.top()) || (_local_7 > goalBounds.bottom()))) {
                    movable.velocity.y = -(movable.velocity.y);
                }
            } else {
                if (((movable.type == Movable.TYPE_PUCK) && ((_local_4 < goalBounds.left()) || (_local_6 > goalBounds.right())))) // movable is puck (or also ball?)
                {
                    movable.state = Movable.STATE_OFF_FIELD;
                } else {
                    if (((_local_5 < goalBounds.top()) || (_local_7 > goalBounds.bottom()))) {
                        movable.velocity.y = -(movable.velocity.y);
                    } else {
                        if (((_local_6 < tableCollisionBounds.left()) || (_local_4 > tableCollisionBounds.right()))) {
                            if (((_local_7 < (-(goalRect.height) / 2.0)) || (_local_5 > (goalRect.height / 2.0)))) {
                                movable.velocity.x = -(movable.velocity.x);
                            }
                        } else {
                            movable.state = Movable.STATE_ON_FIELD;
                        }
                    }
                }
            }
        }

        return (_local_9);
    }

    private static double ballPointCollisionTime(Movable _arg_1, Point _arg_2, double elapsedSeconds) {
        double _local_4 = ((_arg_1.velocity.x * _arg_1.velocity.x) + (_arg_1.velocity.y * _arg_1.velocity.y));
        double _local_5 = ((-(_arg_1.velocity.x * 2) * (_arg_2.x - _arg_1.position.x)) - ((_arg_1.velocity.y * 2) * (_arg_2.y - _arg_1.position.y)));
        double _local_6 = (((_arg_2.x - _arg_1.position.x) * (_arg_2.x - _arg_1.position.x)) + ((_arg_2.y - _arg_1.position.y) * (_arg_2.y - _arg_1.position.y)));
        double _local_7 = (2 * _local_4);
        double _local_8 = (4 * _local_4);
        double _local_9 = (_local_5 * _local_5);
        double _local_10 = (_arg_1.radius * _arg_1.radius);
        if (((-(_local_9) / _local_8) + _local_6) >= _local_10) {
            return (Double.POSITIVE_INFINITY);
        }

        double _local_11 = ((-(_local_5) - Math.sqrt((_local_9 - (_local_8 * (_local_6 - _local_10))))) / _local_7);
        if ((((_local_11 < 0) || ((_local_11 - Constants.SMALL_VALUE) > elapsedSeconds)) || (_local_5 > 0))) {
            return (Double.POSITIVE_INFINITY);
        }

        return (_local_11);
    }

    private static double calculateTheta(double _arg_1, double _arg_2) {
        if (_arg_1 == 0) {
            return ((_arg_2 >= 0) ? Constants.DEGREES_90 : Constants.DEGREES_270);
        }

        double _local_3 = Math.atan((_arg_2 / _arg_1));
        return ((_arg_1 < 0) ? (_local_3 + Math.PI) : _local_3);
    }

    private static void manageBounce(Movable _arg_1, Movable _arg_2) {

        //System.out.println("applying velocities");

        double _local_3 = (_arg_1.position.x - _arg_2.position.x);
        double _local_4 = (_arg_1.position.y - _arg_2.position.y);
        double _local_5 = Math.atan2(_local_4, _local_3);
        double _local_6 = Math.sqrt(((_arg_1.velocity.x * _arg_1.velocity.x) + (_arg_1.velocity.y * _arg_1.velocity.y)));
        double _local_7 = Math.sqrt(((_arg_2.velocity.x * _arg_2.velocity.x) + (_arg_2.velocity.y * _arg_2.velocity.y)));
        double _local_8 = Math.atan2(_arg_1.velocity.y, _arg_1.velocity.x);
        double _local_9 = Math.atan2(_arg_2.velocity.y, _arg_2.velocity.x);
        double _local_10 = (_local_6 * Math.cos((_local_8 - _local_5)));
        double _local_11 = (_local_6 * Math.sin((_local_8 - _local_5)));
        double _local_12 = (_local_7 * Math.cos((_local_9 - _local_5)));
        double _local_13 = (_local_7 * Math.sin((_local_9 - _local_5)));
        double _local_14 = ((((_arg_1.mass - _arg_2.mass) * _local_10) + ((_arg_2.mass + _arg_2.mass) * _local_12)) / (_arg_1.mass + _arg_2.mass));
        double _local_15 = ((((_arg_1.mass + _arg_1.mass) * _local_10) + ((_arg_2.mass - _arg_1.mass) * _local_12)) / (_arg_1.mass + _arg_2.mass));
        double _local_16 = _local_11;
        double _local_17 = _local_13;
        _arg_1.velocity.x = ((Math.cos(_local_5) * _local_14) + (Math.cos((_local_5 + (Math.PI / 2))) * _local_16));
        _arg_1.velocity.y = ((Math.sin(_local_5) * _local_14) + (Math.sin((_local_5 + (Math.PI / 2))) * _local_16));
        _arg_2.velocity.x = ((Math.cos(_local_5) * _local_15) + (Math.cos((_local_5 + (Math.PI / 2))) * _local_17));
        _arg_2.velocity.y = ((Math.sin(_local_5) * _local_15) + (Math.sin((_local_5 + (Math.PI / 2))) * _local_17));
        return;
    }
}
