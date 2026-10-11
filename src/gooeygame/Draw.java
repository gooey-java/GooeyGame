package gooeygame;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * What you draw with. You get one in your game's draw method.
 *
 * Coordinates: (0, 0) is the TOP-LEFT corner. x grows to the right and y
 * grows DOWNWARD. Every position and size can be an int or a double.
 *
 * IMAGES are loaded by name from the project's assets folder:
 *   d.drawImage("ship.png", x, y);       // reads assets/ship.png
 * Images can be stretched, rotated (degrees, clockwise, around the center) and flipped.
 * Scaled pixel art stays crisp; call setSmoothImages(true) for smooth scaling instead.
 *
 * CAMERA: setCamera(x, y) scrolls the world so the point (x, y) is at the top-left of the
 * window. resetCamera() puts it back, for drawing scores and other fixed screen elements.
 * Like colors, the camera and opacity reset at the start of every frame.
 *
 * COLORS can be given three ways:
 *   - a name or hex code in the call itself:  d.fillRect(10, 10, 50, 50, "red");
 *                                             d.fillRect(10, 10, 50, 50, "#FF8800");
 *   - set once with setColor and used by every call after it:
 *                                             d.setColor("red");  d.fillRect(...);
 *   - as red/green/blue numbers (0 to 255):   d.setColor(255, 136, 0);
 * A color given inside a call applies to that call only. Colors reset to
 * white at the start of every frame.
 */
public class Draw {

    private static final Map<String, Color> NAMED = new HashMap<String, Color>();
    private static final Map<String, Color> parsed = new HashMap<String, Color>();
    private static final Map<Integer, Font> fonts = new HashMap<Integer, Font>();

    static {
        NAMED.put("red", Color.RED);
        NAMED.put("green", Color.GREEN);
        NAMED.put("blue", Color.BLUE);
        NAMED.put("yellow", Color.YELLOW);
        NAMED.put("cyan", Color.CYAN);
        NAMED.put("magenta", Color.MAGENTA);
        NAMED.put("orange", Color.ORANGE);
        NAMED.put("pink", Color.PINK);
        NAMED.put("black", Color.BLACK);
        NAMED.put("white", Color.WHITE);
        NAMED.put("gray", Color.GRAY);
        NAMED.put("grey", Color.GRAY);
        NAMED.put("lightgray", Color.LIGHT_GRAY);
        NAMED.put("darkgray", Color.DARK_GRAY);
        NAMED.put("purple", new Color(128, 0, 128));
        NAMED.put("brown", new Color(139, 69, 19));
    }

    /** Size of the pink box shown for a missing image. */
    private static final int MISSING_SIZE = 50;

    private final Graphics2D g;
    private final int width;
    private final int height;
    private final AffineTransform base;   // the screen's own transform (not always the identity, e.g. on high-DPI displays)
    private int fontSize = 20;
    private double cameraX = 0;
    private double cameraY = 0;

    Draw(Graphics2D g, int width, int height) {
        this.g = g;
        this.width = width;
        this.height = height;
        this.base = g.getTransform();
        g.setColor(Color.WHITE);
        g.setFont(fontOf(fontSize));
        setSmoothImages(false);
    }

    /** The width of the drawing area in pixels. */
    public int getWidth() {
        return width;
    }

    /** The height of the drawing area in pixels. */
    public int getHeight() {
        return height;
    }

    // ------------------------------------------------------------
    // Colors
    // ------------------------------------------------------------

    /** Sets the color for everything drawn after this call: a name ("red") or hex code ("#FF8800"). */
    public void setColor(String color) {
        g.setColor(parseColor(color));
    }

    /** Sets the color from red, green, and blue values (each 0 to 255). */
    public void setColor(int red, int green, int blue) {
        g.setColor(new Color(check(red, "red"), check(green, "green"), check(blue, "blue")));
    }

    /** Sets the color using a java.awt.Color such as Color.RED. */
    public void setColor(Color color) {
        g.setColor(color);
    }

    /** Fills the whole screen with one color. A good first line in draw. */
    public void clear(String color) {
        Color old = use(color);
        fillScreen();
        g.setColor(old);
    }

    /** Fills the whole screen with one color given as red, green, blue (0 to 255). */
    public void clear(int red, int green, int blue) {
        Color old = g.getColor();
        setColor(red, green, blue);
        fillScreen();
        g.setColor(old);
    }

    /** Fills the whole window with the current color, ignoring the camera and opacity. */
    private void fillScreen() {
        AffineTransform savedTransform = g.getTransform();
        Composite savedComposite = g.getComposite();
        g.setTransform(base);
        g.setComposite(AlphaComposite.SrcOver);
        g.fill(new Rectangle2D.Double(0, 0, width, height));
        g.setComposite(savedComposite);
        g.setTransform(savedTransform);
    }

    /**
     * Sets how see-through everything drawn after this call is: 1.0 is solid (the
     * default), 0.5 is half see-through, 0.0 is invisible. Resets every frame.
     */
    public void setOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0.0 (invisible) and 1.0 (solid), but you gave "
                + opacity + ".");
        }
        g.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
    }

    // ------------------------------------------------------------
    // Camera
    // ------------------------------------------------------------

    /**
     * Scrolls the world: after this call, the world point (x, y) is drawn at the top-left of
     * the window, so everything you draw is shifted by (-x, -y). To follow a player, set the
     * camera to the player's position minus half the window size. The camera is rounded to
     * whole pixels so tiles never show gaps. clear() is not affected by the camera.
     */
    public void setCamera(double x, double y) {
        cameraX = Math.round(x);
        cameraY = Math.round(y);
        g.setTransform(base);
        g.translate(-cameraX, -cameraY);
    }

    /** Puts the camera back at (0, 0). Use it before drawing scores and other fixed screen elements. */
    public void resetCamera() {
        cameraX = 0;
        cameraY = 0;
        g.setTransform(base);
    }

    /** The camera's current x position (the world x shown at the left edge of the window). */
    public double getCameraX() {
        return cameraX;
    }

    /** The camera's current y position (the world y shown at the top edge of the window). */
    public double getCameraY() {
        return cameraY;
    }

    // ------------------------------------------------------------
    // Rectangles
    // ------------------------------------------------------------

    /** Filled rectangle. (x, y) is its top-left corner. */
    public void fillRect(double x, double y, double w, double h) {
        g.fill(new Rectangle2D.Double(x, y, w, h));
    }

    /** Filled rectangle in the given color. */
    public void fillRect(double x, double y, double w, double h, String color) {
        Color old = use(color);
        fillRect(x, y, w, h);
        g.setColor(old);
    }

    /** Rectangle outline. (x, y) is its top-left corner. */
    public void drawRect(double x, double y, double w, double h) {
        g.draw(new Rectangle2D.Double(x, y, w, h));
    }

    /** Rectangle outline in the given color. */
    public void drawRect(double x, double y, double w, double h, String color) {
        Color old = use(color);
        drawRect(x, y, w, h);
        g.setColor(old);
    }

    // ------------------------------------------------------------
    // Circles
    // ------------------------------------------------------------

    /** Filled circle. (centerX, centerY) is the CENTER of the circle. */
    public void fillCircle(double centerX, double centerY, double radius) {
        g.fill(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
    }

    /** Filled circle in the given color. */
    public void fillCircle(double centerX, double centerY, double radius, String color) {
        Color old = use(color);
        fillCircle(centerX, centerY, radius);
        g.setColor(old);
    }

    /** Circle outline. (centerX, centerY) is the CENTER of the circle. */
    public void drawCircle(double centerX, double centerY, double radius) {
        g.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
    }

    /** Circle outline in the given color. */
    public void drawCircle(double centerX, double centerY, double radius, String color) {
        Color old = use(color);
        drawCircle(centerX, centerY, radius);
        g.setColor(old);
    }

    // ------------------------------------------------------------
    // Lines
    // ------------------------------------------------------------

    /** A line between two points. */
    public void drawLine(double x1, double y1, double x2, double y2) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    /** A line between two points in the given color. */
    public void drawLine(double x1, double y1, double x2, double y2, String color) {
        Color old = use(color);
        drawLine(x1, y1, x2, y2);
        g.setColor(old);
    }

    /** Sets how thick outlines and lines are, in pixels. */
    public void setLineWidth(double lineWidth) {
        g.setStroke(new BasicStroke((float) lineWidth));
    }

    // ------------------------------------------------------------
    // Text
    // ------------------------------------------------------------

    /** Sets the size of text drawn after this call. */
    public void setFontSize(int size) {
        fontSize = size;
        g.setFont(fontOf(size));
    }

    /** Draws text with its TOP-LEFT corner at (x, y), using the current color and font size. */
    public void drawText(String text, double x, double y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(String.valueOf(text), (float) x, (float) (y + fm.getAscent()));
    }

    /** Draws text with its TOP-LEFT corner at (x, y) in the given font size and color. */
    public void drawText(String text, double x, double y, int size, String color) {
        int oldSize = fontSize;
        Color old = use(color);
        setFontSize(size);
        drawText(text, x, y);
        setFontSize(oldSize);
        g.setColor(old);
    }

    /** Draws text centered horizontally on centerX, with its top edge at y. */
    public void drawTextCentered(String text, double centerX, double y) {
        drawText(text, centerX - getTextWidth(text) / 2.0, y);
    }

    /** Centered text in the given font size and color. */
    public void drawTextCentered(String text, double centerX, double y, int size, String color) {
        int oldSize = fontSize;
        Color old = use(color);
        setFontSize(size);
        drawTextCentered(text, centerX, y);
        setFontSize(oldSize);
        g.setColor(old);
    }

    /** How wide, in pixels, this text would be at the current font size. */
    public double getTextWidth(String text) {
        return g.getFontMetrics().stringWidth(String.valueOf(text));
    }

    // ------------------------------------------------------------
    // Images
    // ------------------------------------------------------------

    /** The width of an image in pixels. (A missing image counts as 50, the size of its pink box.) */
    public static int getImageWidth(String path) {
        BufferedImage img = Assets.getImage(path);
        return img == null ? MISSING_SIZE : img.getWidth();
    }

    /** The height of an image in pixels. (A missing image counts as 50, the size of its pink box.) */
    public static int getImageHeight(String path) {
        BufferedImage img = Assets.getImage(path);
        return img == null ? MISSING_SIZE : img.getHeight();
    }

    /** Draws an image at its natural size. (x, y) is the top-left corner. */
    public void drawImage(String path, double x, double y) {
        BufferedImage img = Assets.getImage(path);
        if (img == null) {
            placeholder(x, y, MISSING_SIZE, MISSING_SIZE);
        } else {
            drawRegion(img, 0, 0, img.getWidth(), img.getHeight(),
                       x, y, img.getWidth(), img.getHeight(), 0, false, false);
        }
    }

    /** Draws an image stretched to w by h. (x, y) is the top-left corner. */
    public void drawImage(String path, double x, double y, double w, double h) {
        drawImage(path, x, y, w, h, 0, false, false);
    }

    /**
     * Draws an image stretched to w by h and rotated by degrees (clockwise, around the
     * image's center). (x, y) is the top-left corner of the image before it is rotated.
     */
    public void drawImage(String path, double x, double y, double w, double h, double degrees) {
        drawImage(path, x, y, w, h, degrees, false, false);
    }

    /**
     * Draws an image stretched to w by h, mirrored left-to-right if flipX is true and
     * upside-down if flipY is true. Flipping flipX is how a character faces left.
     */
    public void drawImageFlipped(String path, double x, double y, double w, double h,
                                 boolean flipX, boolean flipY) {
        drawImage(path, x, y, w, h, 0, flipX, flipY);
    }

    /** Draws an image stretched to w by h, rotated by degrees (clockwise), then mirrored if asked. */
    public void drawImage(String path, double x, double y, double w, double h,
                          double degrees, boolean flipX, boolean flipY) {
        BufferedImage img = Assets.getImage(path);
        if (img == null) {
            placeholder(x, y, w, h);
        } else {
            drawRegion(img, 0, 0, img.getWidth(), img.getHeight(), x, y, w, h, degrees, flipX, flipY);
        }
    }

    /** Smooth scaling (true) or crisp, blocky pixels (false, the default). Resets every frame. */
    public void setSmoothImages(boolean smooth) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, smooth
            ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
            : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    /**
     * Draws part of an image (the source rectangle sx, sy, sw, sh) into the destination
     * rectangle, optionally rotated and flipped. Used by drawImage and SpriteSheet.
     * Positions are rounded to whole pixels so pixel art never shimmers.
     */
    void drawRegion(BufferedImage img, int sx, int sy, int sw, int sh,
                    double x, double y, double w, double h,
                    double degrees, boolean flipX, boolean flipY) {
        int dx = (int) Math.round(x);
        int dy = (int) Math.round(y);
        int dw = (int) Math.round(w);
        int dh = (int) Math.round(h);
        if (dw == 0 || dh == 0) {
            return;
        }
        AffineTransform saved = g.getTransform();
        if (degrees != 0) {
            g.rotate(Math.toRadians(degrees), dx + dw / 2.0, dy + dh / 2.0);
        }
        int x1 = flipX ? dx + dw : dx;
        int x2 = flipX ? dx : dx + dw;
        int y1 = flipY ? dy + dh : dy;
        int y2 = flipY ? dy : dy + dh;
        g.drawImage(img, x1, y1, x2, y2, sx, sy, sx + sw, sy + sh, null);
        g.setTransform(saved);
    }

    /** Draws an image at its natural size, centered on (centerX, centerY). */
    public void drawImageCentered(String path, double centerX, double centerY) {
        BufferedImage img = Assets.getImage(path);
        if (img == null) {
            placeholder(centerX - MISSING_SIZE / 2.0, centerY - MISSING_SIZE / 2.0, MISSING_SIZE, MISSING_SIZE);
        } else {
            drawImage(path, centerX - img.getWidth() / 2.0, centerY - img.getHeight() / 2.0);
        }
    }

    /** Draws an image stretched to w by h, centered on (centerX, centerY). */
    public void drawImageCentered(String path, double centerX, double centerY, double w, double h) {
        drawImage(path, centerX - w / 2.0, centerY - h / 2.0, w, h);
    }

    /** A pink box with an X, so a missing image is obvious but doesn't crash the game. */
    void placeholder(double x, double y, double w, double h) {
        Color old = g.getColor();
        g.setColor(Color.MAGENTA);
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(Color.BLACK);
        g.draw(new Rectangle2D.Double(x, y, w, h));
        g.draw(new Line2D.Double(x, y, x + w, y + h));
        g.draw(new Line2D.Double(x + w, y, x, y + h));
        g.setColor(old);
    }

    // ------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------

    /** Switches to the named color and returns the previous color so it can be restored. */
    private Color use(String color) {
        Color old = g.getColor();
        g.setColor(parseColor(color));
        return old;
    }

    private static int check(int value, String name) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("The " + name + " value must be between 0 and 255, but you gave "
                + value + ".");
        }
        return value;
    }

    private static Font fontOf(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("A font size must be positive, but you gave " + size + ".");
        }
        Font font = fonts.get(size);
        if (font == null) {
            font = new Font(Font.SANS_SERIF, Font.BOLD, size);
            fonts.put(size, font);
        }
        return font;
    }

    /** Turns "red" or "#FF8800" into a Color. Unknown colors warn once and become white. */
    static Color parseColor(String name) {
        if (name == null) {
            Warnings.once("color:null", "A color was null. Use a name like \"red\" or a hex code like \"#FF8800\".");
            return Color.WHITE;
        }
        Color color = parsed.get(name);
        if (color != null) {
            return color;
        }
        String clean = name.trim().toLowerCase();
        color = NAMED.get(clean);
        if (color == null && clean.startsWith("#")) {
            color = decodeHex(clean);
        }
        if (color == null) {
            Warnings.once("color:" + name, "Unknown color \"" + name + "\". Use a hex code like \"#FF8800\" or one of: "
                + new TreeSet<String>(NAMED.keySet()));
            color = Color.WHITE;
        }
        parsed.put(name, color);
        return color;
    }

    private static Color decodeHex(String hex) {
        try {
            if (hex.length() == 4) {   // "#F80" means "#FF8800"
                hex = "#" + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2)
                          + hex.charAt(3) + hex.charAt(3);
            }
            if (hex.length() == 7) {
                return Color.decode(hex);
            }
        } catch (NumberFormatException e) {
            // fall through: caller will warn
        }
        return null;
    }
}
