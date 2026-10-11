package gooeygame;

/**
 * Simple collision checks. All methods are static, so call them like
 * Collision.rectsOverlap(...).
 */
public final class Collision {

    private Collision() { }

    /**
     * Do two rectangles overlap? Each rectangle is given by its top-left
     * corner (x, y) plus its width and height.
     */
    public static boolean rectsOverlap(double x1, double y1, double w1, double h1,
                                       double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2
            && y1 < y2 + h2 && y1 + h1 > y2;
    }

    /** Do two circles overlap? Each circle is given by its center and radius. */
    public static boolean circlesOverlap(double cx1, double cy1, double r1,
                                         double cx2, double cy2, double r2) {
        double dx = cx1 - cx2;
        double dy = cy1 - cy2;
        double radii = r1 + r2;
        return dx * dx + dy * dy < radii * radii;
    }

    /** Is the point (px, py) inside the rectangle? */
    public static boolean pointInRect(double px, double py,
                                      double x, double y, double w, double h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }
}
