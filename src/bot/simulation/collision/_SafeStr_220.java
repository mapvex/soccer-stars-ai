package bot.simulation.collision;

import bot.simulation.objects.Movable;

public class _SafeStr_220 extends CollisionResult {
    public Movable ball;

    public _SafeStr_220(Movable _arg_1, double _arg_2, double _arg_3) {
        super(_arg_2, _arg_3);
        this.ball = _arg_1;
    }
}
