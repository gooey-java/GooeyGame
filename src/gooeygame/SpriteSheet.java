package gooeygame;

import java.awt.image.BufferedImage;

/**
 * One image cut into a grid of equal-sized frames: the frames of an animation, a set of
 * tiles, or both. Load it once (in init), then draw any frame by number.
 *
 *   sheet = new SpriteSheet("hero.png", 16, 16);       // an image made of 16 x 16 pixel frames
 *   sheet.draw(d, 3, x, y);                            // draw frame 3 at (x, y)
 *
 * Frames are numbered from 0, left to right and then top to bottom, like reading a page.
 * So in a sheet that is 4 frames wide, the frame in row 1, column 2 is number 1 * 4 + 2 = 6.
 *
 * Some tile packs leave a gap between tiles, or a border around the edge. Describe those
 * with the five-number constructor: new SpriteSheet("tiles.png", 16, 16, margin, spacing).
 *
 * If the image can't be loaded you get one warning and a pink box instead of crashing.
 */
public class SpriteSheet {

    private final BufferedImage image;      // null if the file couldn't be loaded
    private final int frameWidth;
    private final int frameHeight;
    private final int margin;
    private final int spacing;
    private final int columns;
    private final int rows;

    /** A sheet whose frames touch each other and the edges of the image. */
    public SpriteSheet(String path, int frameWidth, int frameHeight) {
        this(path, frameWidth, frameHeight, 0, 0);
    }

    /**
     * @param path        the image in the assets folder
     * @param frameWidth  width of one frame in pixels
     * @param frameHeight height of one frame in pixels
     * @param margin      pixels of border around the whole sheet
     * @param spacing     pixels of gap between neighboring frames
     */
    public SpriteSheet(String path, int frameWidth, int frameHeight, int margin, int spacing) {
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("A frame's width and height must be positive, but you gave "
                + frameWidth + " x " + frameHeight + ".");
        }
        if (margin < 0 || spacing < 0) {
            throw new IllegalArgumentException("The margin and spacing can't be negative.");
        }
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.margin = margin;
        this.spacing = spacing;
        this.image = Assets.getImage(path);

        if (image == null) {
            this.columns = 1;     // behave like a one-frame sheet so frame % getFrameCount() still works
            this.rows = 1;
        } else {
            int cols = (image.getWidth() - 2 * margin + spacing) / (frameWidth + spacing);
            int rws = (image.getHeight() - 2 * margin + spacing) / (frameHeight + spacing);
            if (cols < 1 || rws < 1) {
                throw new IllegalArgumentException("A frame of " + frameWidth + " x " + frameHeight
                    + " pixels doesn't fit in the image '" + path + "', which is "
                    + image.getWidth() + " x " + image.getHeight() + " pixels.");
            }
            this.columns = cols;
            this.rows = rws;
        }
    }

    /** The width of one frame in pixels. */
    public int getFrameWidth() {
        return frameWidth;
    }

    /** The height of one frame in pixels. */
    public int getFrameHeight() {
        return frameHeight;
    }

    /** How many frames fit across the sheet. */
    public int getColumns() {
        return columns;
    }

    /** How many frames fit down the sheet. */
    public int getRows() {
        return rows;
    }

    /** The total number of frames. Valid frame numbers are 0 up to getFrameCount() - 1. */
    public int getFrameCount() {
        return columns * rows;
    }

    /** Draws a frame at its natural size. (x, y) is the top-left corner. */
    public void draw(Draw d, int frame, double x, double y) {
        draw(d, frame, x, y, frameWidth, frameHeight, 0, false, false);
    }

    /** Draws a frame stretched to w by h. Pixel art stays crisp, so whole-number scales (x2, x3) look best. */
    public void draw(Draw d, int frame, double x, double y, double w, double h) {
        draw(d, frame, x, y, w, h, 0, false, false);
    }

    /** Draws a frame stretched to w by h and rotated by degrees (clockwise, around its center). */
    public void draw(Draw d, int frame, double x, double y, double w, double h, double degrees) {
        draw(d, frame, x, y, w, h, degrees, false, false);
    }

    /** Draws a frame stretched to w by h, mirrored left-to-right (flipX) and/or upside-down (flipY). */
    public void drawFlipped(Draw d, int frame, double x, double y, double w, double h,
                            boolean flipX, boolean flipY) {
        draw(d, frame, x, y, w, h, 0, flipX, flipY);
    }

    /** Draws a frame stretched to w by h, rotated by degrees (clockwise), then mirrored if asked. */
    public void draw(Draw d, int frame, double x, double y, double w, double h,
                     double degrees, boolean flipX, boolean flipY) {
        if (frame < 0 || frame >= getFrameCount()) {
            throw new IllegalArgumentException("Frame " + frame + " doesn't exist. This sheet has "
                + getFrameCount() + " frames, numbered 0 to " + (getFrameCount() - 1)
                + " (counting left to right, then top to bottom).");
        }
        if (image == null) {
            d.placeholder(x, y, w, h);
            return;
        }
        int column = frame % columns;
        int row = frame / columns;
        int sourceX = margin + column * (frameWidth + spacing);
        int sourceY = margin + row * (frameHeight + spacing);
        d.drawRegion(image, sourceX, sourceY, frameWidth, frameHeight, x, y, w, h, degrees, flipX, flipY);
    }
}
