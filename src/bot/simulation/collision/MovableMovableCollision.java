package bot.simulation.collision;

import bot.simulation.objects.Movable;

public class MovableMovableCollision extends CollisionResult {
    public Movable item1;
    public Movable item2;

    public MovableMovableCollision(Movable _arg_1, Movable _arg_2, double _arg_3, double _arg_4) {
        super(_arg_3, _arg_4);
        this.item1 = _arg_1;
        this.item2 = _arg_2;
    }

    public boolean _SafeStr_1748(Movable _arg_1) {
        return ((_arg_1 == this.item1) || (_arg_1 == this.item2));
    }
}
