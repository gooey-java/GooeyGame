package gooeygame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Launches your game. One call is all it takes:
 *
 *   public static void main(String[] args) {
 *       GameRunner.start(new MyGame(), "My Game", 800, 600);
 *   }
 *
 * The runner opens the window, then calls your game's init() once and its
 * update() and draw() about 60 times per second.
 */
public final class GameRunner {

    private static final double MAX_DT = 0.05;   // avoids huge jumps after a pause
    private static final int TICK_MS = 16;       // about 60 ticks per second

    private static boolean started = false;
    private static GameListener game;
    private static int width;
    private static int height;

    private static JFrame frame;
    private static JPanel panel;
    private static javax.swing.Timer timer;

    private static boolean initialized = false;
    private static boolean crashed = false;
    private static boolean debug = false;
    private static long lastTime;
    private static double fps = 60;

    private GameRunner() { }

    /**
     * Opens a window and runs your game in it.
     *
     * @param listener your game (an object of a class that implements GameListener)
     * @param title    the text in the window's title bar
     * @param w        the width of the play area in pixels
     * @param h        the height of the play area in pixels
     */
    public static void start(GameListener listener, String title, int w, int h) {
        if (listener == null) {
            throw new IllegalArgumentException("GameRunner.start needs a game. Did you write new MyGame() as the first argument?");
        }
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("A game's width and height must be positive, but you gave "
                + w + " x " + h + ".");
        }
        if (started) {
            throw new IllegalStateException("GameRunner.start was already called. A program can run only one game window.");
        }
        started = true;
        game = listener;
        width = w;
        height = h;
        SwingUtilities.invokeLater(() -> launch(title));
    }

    /** The width of the play area in pixels (the width you passed to start). */
    public static int getWidth() {
        return width;
    }

    /** The height of the play area in pixels (the height you passed to start). */
    public static int getHeight() {
        return height;
    }

    /** Turns the debug overlay (frames per second and mouse position) on or off. */
    public static void setDebug(boolean on) {
        debug = on;
    }

    /** Closes the window and ends the program. */
    public static void quit() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    // ------------------------------------------------------------------
    // Everything below is machinery students don't need to read.
    // ------------------------------------------------------------------

    private static void launch(String title) {
        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(width, height));
        panel.setFocusable(true);
        panel.setFocusTraversalKeysEnabled(false);   // let the game see Tab

        panel.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e)  { Input.keyPressed(e.getKeyCode()); }
            @Override public void keyReleased(KeyEvent e) { Input.keyReleased(e.getKeyCode()); }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { Input.mousePressed(e.getButton(), e.getX(), e.getY()); }
            @Override public void mouseReleased(MouseEvent e) { Input.mouseReleased(e.getButton(), e.getX(), e.getY()); }
            @Override public void mouseMoved(MouseEvent e)    { Input.mouseMoved(e.getX(), e.getY()); }
            @Override public void mouseDragged(MouseEvent e)  { Input.mouseMoved(e.getX(), e.getY()); }
        };
        panel.addMouseListener(mouse);
        panel.addMouseMotionListener(mouse);
        panel.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { Input.reset(); }
        });

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        timer = new javax.swing.Timer(TICK_MS, e -> tick());

        frame.setVisible(true);
        panel.requestFocusInWindow();

        try {
            game.init();
            initialized = true;
        } catch (RuntimeException | Error ex) {
            crash("init()", ex);
        }

        lastTime = System.nanoTime();
        if (!crashed) {
            timer.start();
        }
    }

    private static void tick() {
        long now = System.nanoTime();
        double dt = (now - lastTime) / 1e9;
        lastTime = now;
        if (dt > 0) {
            fps = 0.9 * fps + 0.1 * (1.0 / dt);
        }
        if (dt > MAX_DT) {
            dt = MAX_DT;
        }
        try {
            game.update(dt);
        } catch (RuntimeException | Error ex) {
            crash("update()", ex);
            return;
        }
        Input.endFrame();
        panel.repaint();
    }

    private static void crash(String where, Throwable ex) {
        if (crashed) {
            return;
        }
        crashed = true;
        if (timer != null) {
            timer.stop();
        }
        System.err.println();
        System.err.println("*** Your game stopped because of an error in " + where + " ***");
        System.err.println("Look for the first line below that names one of YOUR classes.");
        if (ex instanceof NoClassDefFoundError) {
            System.err.println();
            System.err.println(AudioUtil.missingClassHelp(ex));
        }
        System.err.println();
        ex.printStackTrace();
        if (panel != null) {
            panel.repaint();
        }
    }

    private static void drawDebug(Draw d) {
        d.drawText("FPS: " + (int) Math.round(fps), 8, 6, 14, "#00FF00");
        d.drawText("Mouse: " + Input.getMouseX() + ", " + Input.getMouseY(), 8, 24, 14, "#00FF00");
    }

    /** The surface the game is drawn on. */
    @SuppressWarnings("serial")
    private static class GamePanel extends JPanel {

        GamePanel() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!initialized && !crashed) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Draw d = new Draw(g2, width, height);
                if (crashed) {
                    d.drawTextCentered("Your game crashed. Check the console for details.",
                                       width / 2.0, height / 2.0 - 10, 20, "#FF5A5A");
                    return;
                }
                game.draw(d);
                if (debug) {
                    d.resetCamera();
                    d.setOpacity(1.0);
                    drawDebug(d);
                }
            } catch (RuntimeException | Error ex) {
                crash("draw()", ex);
            } finally {
                g2.dispose();
            }
        }
    }
}
