package gooeygame;

import java.util.HashSet;
import java.util.Set;

/**
 * Ask about the keyboard and mouse. Every method is static, so you can call
 * them from anywhere:
 *
 *   if (Input.isKeyDown(Key.LEFT)) { ... }       // true every tick while held
 *   if (Input.wasKeyPressed(Key.SPACE)) { ... }  // true for ONE tick per press
 *
 * Mouse positions are in game coordinates: (0, 0) is the top-left corner.
 */
public final class Input {

    private static final Set<Integer> down = new HashSet<Integer>();
    private static final Set<Integer> pressed = new HashSet<Integer>();
    private static boolean mouseDown = false;
    private static boolean mousePressed = false;
    private static int mouseX = 0;
    private static int mouseY = 0;

    private Input() { }

    /** Is this key being held down right now? Use for continuous actions like moving. */
    public static boolean isKeyDown(int key) {
        return down.contains(key);
    }

    /** Was this key pressed since the last tick? Use for one-time actions like jumping or shooting. */
    public static boolean wasKeyPressed(int key) {
        return pressed.contains(key);
    }

    /** Is the left mouse button held down right now? */
    public static boolean isMouseDown() {
        return mouseDown;
    }

    /** Was the left mouse button clicked since the last tick? */
    public static boolean wasMousePressed() {
        return mousePressed;
    }

    /** The mouse's x position (0 is the left edge of the window). */
    public static int getMouseX() {
        return mouseX;
    }

    /** The mouse's y position (0 is the TOP edge of the window). */
    public static int getMouseY() {
        return mouseY;
    }

    // ---- Used by Game only (package-private) ----

    static void keyPressed(int code) {
        if (down.add(code)) {          // ignore the keyboard's auto-repeat
            pressed.add(code);
        }
    }

    static void keyReleased(int code) {
        down.remove(code);
    }

    static void mousePressed(int button, int x, int y) {
        mouseX = x;
        mouseY = y;
        if (button == 1 && !mouseDown) {
            mouseDown = true;
            mousePressed = true;
        }
    }

    static void mouseReleased(int button, int x, int y) {
        mouseX = x;
        mouseY = y;
        if (button == 1) {
            mouseDown = false;
        }
    }

    static void mouseMoved(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    /** Called after each update so "was pressed" lasts exactly one tick. */
    static void endFrame() {
        pressed.clear();
        mousePressed = false;
    }

    /** Called when the window loses focus so keys don't get "stuck". */
    static void reset() {
        down.clear();
        pressed.clear();
        mouseDown = false;
        mousePressed = false;
    }
}
