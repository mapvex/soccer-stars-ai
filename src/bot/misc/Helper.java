package bot.misc;

import bot.Bot;
import bot.Constants;

import java.awt.*;
import java.awt.event.InputEvent;

public class Helper {
    private Bot bot;
    private Robot robot;

    public Helper(Bot bot) {
        this.bot = bot;
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
            bot.close();
        }
    }

    // Convert field coordinates to on-screen ones.
    public int toScreenX(double fieldX) {
        return (int) (bot.fieldCenterX + (fieldX * Constants.REAL_SCALE));
    }

    public int toScreenY(double fieldY) {
        return (int) (bot.fieldCenterY - (fieldY * Constants.REAL_SCALE));
    }

    public int toScreenScale(double fieldLength) {
        return (int) (fieldLength * Constants.REAL_SCALE);
    }

    // Takes a shot on the game scren with specified parameters.
    public void takeShot(int x1, int y1, double power, double angle) {
        angle += Math.PI;

        double x2 = -1.0;
        double y2 = -1.0;

        int i = 0;
        while (!coordinateInScreenBounds(x2, y2) && i != 10) {
            i++;
            x2 = (double) x1 + (power * Math.cos(angle));
            y2 = (double) y1 - (power * Math.sin(angle));

            power = calculateNewPower(x1, y1, x2, y2, angle, power);
        }

        int finalX = (int) x2;
        int finalY = (int) y2;

        try {
            // Go to disc location and hold the mouse button down
            glideMouseTo(x1, y1, 300);
            robot.delay(50);
            robot.mousePress(InputEvent.BUTTON1_MASK);

            // Charge and release
            glideMouseTo(finalX, finalY, 300);
            robot.delay(50);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Is coordinate in screen bounds
    public boolean coordinateInScreenBounds(double x, double y) {
        return (x >= 0.0 && y >= 0.0 && x <= bot.screenWidth && y <= bot.screenHeight);
    }

    // Calculate new power considering screen bounds
    private double calculateNewPower(double start_x, double start_y, double x, double y, double angle, double power) {
        if (x < 0.0) {
            power = (0.0 - start_x) / Math.cos(angle);
        } else if (y < 0.0) {
            power = (0.0 - start_y) / Math.sin(angle);
        } else if (x > bot.screenWidth) {
            power = (bot.screenWidth - start_x) / Math.cos(angle);
        } else if (y > bot.screenHeight) {
            power = (bot.screenHeight - start_y) / Math.sin(angle);
        }
        return Math.abs(power);
    }

    // Smoothly move mouse to specified coordinates.
    public void glideMouseTo(int x2, int y2, int t) throws Exception {
        java.awt.Point p = MouseInfo.getPointerInfo().getLocation();
        int x1 = p.x;
        int y1 = p.y;

        int n = t / 2;
        double dx = (x2 - x1) / ((double) n);
        double dy = (y2 - y1) / ((double) n);
        double dt = t / ((double) n);
        for (int step = 1; step <= n; step++) {
            Thread.sleep((int) dt);
            robot.mouseMove((int) (x1 + dx * step), (int) (y1 + dy * step));
        }
    }

    // Truncate double to given precision
    public static double toPrecision(double n) {
        int p = 6;
        if (n == 0) return 0;

        double e = Math.floor(Math.log10(Math.abs(n)));
        double f = Math.round(Math.exp((Math.abs(e - p + 1)) * Math.log(10)));

        if (e - p + 1 < 0) {
            return Math.round(n * f) / f;
        }

        return Math.round(n / f) * f;
    }

    public static boolean areCoordinatesInOwnGoalBox(double x, double y) {
        return (x > -Constants.FIELD_WIDTH / 2.0 + 3.0 && x < -Constants.FIELD_WIDTH / 2.0 + Constants.SCOREBOX_WIDTH &&
                y > -Constants.SCOREBOX_HEIGHT / 2.0 && y < Constants.SCOREBOX_HEIGHT / 2.0);
    }

    public static boolean areCoordinatesInBadPosition(double x, double y) {
        return x > Constants.FIELD_WIDTH / 2.0 - 10.0 && y > -Constants.SCORE_HEIGHT && y < Constants.SCORE_HEIGHT;
    }

    public static boolean areCoordinatesNearBadPosition(double x, double y) {
        return x > Constants.FIELD_WIDTH / 2.0 - 20.0 && y > -Constants.SCORE_HEIGHT - 10.0 && y < Constants.SCORE_HEIGHT + 10.0;
    }
}
