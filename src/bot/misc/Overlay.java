package bot.misc;

import bot.Bot;
import bot.simulation.SimulationRunner;
import com.sun.awt.AWTUtilities;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;
import java.awt.*;

import javax.swing.SwingUtilities;

public class Overlay {
    private Bot bot;

    private JFrame frame;
    private FrameContent frameContent;
    private Font textFont;
    public String statusText = "";
    public boolean hidden = true;

    public Overlay(Bot bot) {
        this.bot = bot;
        initialize();
    }

    private void initialize() {
        textFont = new Font("TimesRoman", Font.BOLD, 28);

        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setSize(bot.screenWidth, bot.screenHeight);
        frame.setType(Window.Type.UTILITY);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
                frame.setAlwaysOnTop(true);
            }
        });

        frameContent = new FrameContent(this);
        frame.add(frameContent);
        frame.getContentPane().validate();
        frame.getContentPane().repaint();

        AWTUtilities.setWindowOpaque(frame, false);
        setTransparent(frame);
    }

    class FrameContent extends JLabel {
        private Overlay overlay;

        FrameContent(Overlay overlay) {
            this.overlay = overlay;
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(bot.screenWidth, bot.screenHeight);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(bot.screenWidth, bot.screenHeight);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            overlay.paintCallback(g);
        }
    }

    public void paintCallback(Graphics g) {
        if (hidden) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Select a color
        g.setColor(Color.BLACK);

        // Mark the field center point
        if (bot.fieldCenterX != 0 && bot.fieldCenterY != 0) {
            drawMarkerAt(g, bot.fieldCenterX, bot.fieldCenterY, 18);
        }

        // Mark the pucks and the ball
        if (bot.xCoordinates != null && bot.yCoordinates != null) {
            for (int i = 0; i < bot.xCoordinates.length; i++) {
                int x = bot.helper.toScreenX(bot.xCoordinates[i]);
                int y = bot.helper.toScreenY(bot.yCoordinates[i]);
                drawMarkerAt(g, x, y, 18);
            }
        }

        // Render the simulation if it is running and in GUI mode
        SimulationRunner guiSimulationRunner = bot.script.guiSimulationRunner;

        if (guiSimulationRunner != null && guiSimulationRunner.simulation.running && guiSimulationRunner.simulation.isUsingGUI()) {
            guiSimulationRunner.simulation.render(g2);
        }

        // Draw informative text
        g.setColor(Color.RED);
        g2.setFont(textFont);
        g2.drawString(statusText, 50, bot.gameWindowY);

        // can't do this until we start threading the simulations
        //double timeInSeconds = (System.currentTimeMillis() - bot.turnStartTime) / 1000.0;
        //g2.drawString("Time: " + String.format("%.2f", timeInSeconds), 50, bot.gameWindowY + 50);
    }

    // Draw a marker at given screen coordinates
    private void drawMarkerAt(Graphics g, int x, int y, int size) {
        g.drawLine(x - size, y - size, x + size, y + size);
        g.drawLine(x - size, y + size, x + size, y - size);
    }

    // Functions to make a Java window transparent and through clickable
    private void setTransparent(Component w) {
        WinDef.HWND hwnd = getHWnd(w);
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    private WinDef.HWND getHWnd(Component w) {
        // Get the window handle from the OS
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(w));
        return hwnd;
    }

    // Other classes can request a repaint by calling this
    public void repaint() {
        frame.repaint();
    }

    public void setStatusText(String text) {
        statusText = text;
        System.out.println(text);
    }
}
