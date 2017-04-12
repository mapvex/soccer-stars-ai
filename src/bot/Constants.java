package bot;

public class Constants {
    // To identify our player in the game packets
    public static final String MY_PLAYER_ID = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
    public static final String NETWORK_ADAPTER_NAME = "Microsoft";

    // About the applet
    public static final int APPLET_WIDTH = 1109;
    public static final int APPLET_HEIGHT = 624;
    public static final double REAL_SCALE = 3.0 + 1.0 / 3.0; // 125% windows scaling, might need adjusting for different displays

    // Field properties
    public static final double FIELD_WIDTH = 259.8796;
    public static final double FIELD_HEIGHT = 152.2824;
    public static final double SCORE_WIDTH = 16.46297;
    public static final double SCORE_HEIGHT = 54.092593;
    public static final double CORNER_RADIUS = 7.05;
    public static final double FRICTION_FACTOR = 1.0;

    // Movable properties
    public static final double BALL_RADIUS = 3.9;
    public static final double BALL_MASS = 1.18;
    public static final double BALL_RESTITUTION = 0.62;
    public static final double BALL_FRICTION = 0.04;
    public static final double PUCK_RADIUS = 7.84;
    public static final double PUCK_MASS = 4.13;
    public static final double PUCK_RESTITUTION = 0.73;
    public static final double PUCK_FRICTION = 0.07;

    // Shot properties
    public static final double MAX_POWER = 240.0;
    public static final double MIN_POWER = 20.0;

    // Extracted constants
    public static final double _SafeStr_1090 = 2.65748;
    public static final int HOURS_12 = 43200000;
    public static final double _SafeStr_1096 = 5.0;
    public static final double SMALL_VALUE = 1E-11;
    public static final double DEGREES_90 = 1.570796;
    public static final double DEGREES_270 = 4.712388;
    public static final double _SafeStr_1164 = 2.5;
    public static final double _SafeStr_1278 = 0.285714;
    public static final double _SafeStr_1279 = 980.0;
    public static final double _SafeStr_1165 = 0.2;
    public static final double _SafeStr_1733 = 0.111;
    public static final double _SafeStr_1280 = 0.025;
    public static final double _SafeStr_1163 = 0.54;
    public static final double _SafeStr_1734 = 0.001457;
    public static final double _SafeStr_1735 = 196;
    public static final double _SafeStr_1736 = 9.8;
    public static final double _SafeStr_1103 = 0.689655;
    public static final double _SafeStr_1737 = 10.0;

    // Packet IDs
    public static final int PACKET_ID_GAME_STARTED = 6;
    public static final int PACKET_ID_GAME_ENDED = 7;
    public static final int PACKET_ID_SHOT_OUTCOME = 15;

    // Misc
    public static final double SCOREBOX_WIDTH = 20.0;
    public static final double SCOREBOX_HEIGHT = SCORE_HEIGHT;
}
