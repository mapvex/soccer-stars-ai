package bot.simulation.collision;

import bot.simulation.objects.Movable;
import bot.simulation.objects.Point;

public class MovablePointCollision extends _SafeStr_220 {
    private Point _SafeStr_1747;

    public MovablePointCollision(Movable _arg_1, Point _arg_2, double _arg_3, double _arg_4) {
        super(_arg_1, _arg_3, _arg_4);
        this._SafeStr_1747 = _arg_2;
    }

    public Point _SafeStr_1158() {
        return (this._SafeStr_1747);
    }
}
