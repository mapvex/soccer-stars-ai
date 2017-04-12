package bot.simulation.collision;

import bot.simulation.objects.Movable;

public class MovableNumberCollision extends _SafeStr_220 {
    private double _SafeStr_1746;

    public MovableNumberCollision(Movable _arg_1, double _arg_2, double _arg_3, double _arg_4) {
        super(_arg_1, _arg_3, _arg_4);
        this._SafeStr_1746 = _arg_2;
    }

    public double _SafeStr_1157() {
        return (this._SafeStr_1746);
    }
}
