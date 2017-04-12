package bot;

import bot.misc.Helper;
import bot.misc.NetworkWatcher;
import bot.misc.Overlay;
import org.sikuli.script.Key;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

public class Bot {
    // Calculated variables
    public int screenWidth, screenHeight;
    public int gameWindowX, gameWindowY;
    public int fieldCenterX, fieldCenterY;
    public double[] xCoordinates;
    public double[] yCoordinates;
    public long turnStartTime;

    public String workingDirectory;
    public NetworkWatcher netWatcher;
    public Overlay overlay;
    public Script script;
    public Helper helper;

    public Screen screen;
    public boolean testMode = false;
    public boolean firstShotDone = false;
    public boolean invertedMode = false;

    public Bot() {
        // Initialize
        workingDirectory = System.getProperty("user.dir");
        screen = new Screen();
        screenWidth = screen.getBounds().width;
        screenHeight = screen.getBounds().height;

        // Initialize helper functions
        helper = new Helper(this);

        // Initialize overlay
        overlay = new Overlay(this);

        // Initialize script
        script = new Script(this);

        // Test mode
        if (testMode) {
            double[] testPucksX = {0.0, -120.0, -90.0, -90.0, -60.0, -30.0, 120.0, 90.0, 90.0, 60.0, 30.0};
            double[] testPucksY = {0.0, 0.0, -40.0, 40.0, 0.0, 0.0, 0.0, -40.0, 40.0, 0.0, 0.0};

            // test turn with prdefined puck positions
            onMyTurn(testPucksX, testPucksY, System.currentTimeMillis());
        }

        // Initialize Network Watcher
        netWatcher = new NetworkWatcher(this);

        // Run network watcher, it will call back to onMyTurn() function
        netWatcher.loop();
    }

    public void close() {
        netWatcher.close();
    }

    public boolean isFirstShot() {
        return !firstShotDone && xCoordinates[0] == 0.0 && yCoordinates[0] == 0.0;
    }

    // Table state has been updated and it is our turn
    public void onMyTurn(double[] xCoordinates, double[] yCoordinates, long timestamp) {
        if (System.currentTimeMillis() - timestamp > 7000) {
            System.out.println("Turn started more than 7 seconds ago. Skipping move.");
            return;
        }

        this.xCoordinates = xCoordinates;
        this.yCoordinates = yCoordinates;

        this.turnStartTime = timestamp;

        if (!testMode) {
            // first locate the game window
            Match match;
            screen.setAutoWaitTimeout(1);
            match = screen.exists("resources/game_corner.png");
            if (match == null || match.getScore() < 0.99) {
                System.out.println("Unable to find game window. Skipping move.");
                return;
            }

            gameWindowX = match.getX() + 12;
            gameWindowY = match.getY() + 13;
            System.out.println("Found game window at: " + gameWindowX + " " + gameWindowY);
        } else {
            // place the field approximately at the center of the screen when in testMode
            gameWindowX = screenWidth / 2 - Constants.APPLET_WIDTH / 2;
            gameWindowY = screenHeight / 2 - Constants.APPLET_HEIGHT / 2;
        }

        // calculate the center point of the field in screen coordinates
        fieldCenterX = gameWindowX + Constants.APPLET_WIDTH / 2 + 1;
        fieldCenterY = gameWindowY + 349; // field is not vertically centered relative to the game window

        // show overlay
        overlay.hidden = false;
        overlay.repaint();

        script.turn();

        // hide overlay after the move is finished
        overlay.hidden = true;
        overlay.repaint();

        firstShotDone = true;
    }

    // Game has been ended
    public void onGameEnded() {
        firstShotDone = false;
        invertedMode = false;
        boolean rematched = false;

        try {
            rematched = clickBackFromWinningScreen(true);
        } catch (Exception e) {
            Match match;
            match = screen.exists("resources/closebutton.png");
            if (match != null && match.getScore() >= 0.80) {
                match.click();
                screen.wait(10.0);
                try {
                    rematched = clickBackFromWinningScreen(true);
                } catch (Exception e2) {

                }
            } else {
                try {
                    rematched = clickBackFromWinningScreen(true);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        if (rematched) {
            System.out.println("Rematched!");
            return;
        }

        try {
            Match match;
            // click on the back arrow until the desired match screen is present
            while (true) {
                match = screen.exists("resources/field_select_country.png");
                if (match != null && match.getScore() >= 0.80) {
                    break; // found the right screen
                }

                match = screen.wait("resources/field_select_back_button.png");
                match.click();
                screen.wait(0.75);
            }

            // now at the right screen, click play
            match = screen.wait("resources/field_play.png");
            match.click();
        } catch (Exception e) {
            System.out.println("Unable to find a way back from game over screen!");
            e.printStackTrace();
        }
    }

    public boolean clickBackFromWinningScreen(boolean tryRematch) throws Exception {
        Match match;
        match = screen.wait("resources/back_from_winning_Screen.png", 30.0);
        if (tryRematch) {
            screen.wait(0.5);
            Match match2;
            match2 = screen.exists("resources/button_rematch.png");
            if (match2 != null && match2.getScore() >= 0.80) {
                match2.click();
                screen.wait(3.5);
                Match match3 = screen.exists("resources/back_from_winning_Screen.png");
                if (match3 != null && match3.getScore() >= 0.80) {
                    // rematch failed
                    return clickBackFromWinningScreen(false);
                }
                return true;
            }
        }

        match.click();
        return false;
    }
}
