/**
 * GooeyGame: make real 2D games in Java with a simple game loop, built on
 * top of Swing.
 *
 * <p>Add {@code import gooeygame.*;} to the top of your file. Every game is one
 * class that implements {@link gooeygame.GameListener} and fills in three methods:
 * {@code init()} runs once at the start, {@code update(double dt)} runs about
 * 60 times per second to move things and check the rules, and
 * {@code draw(Draw d)} draws the current state. Start the game with
 * {@link gooeygame.GameRunner#start GameRunner.start}.</p>
 *
 * <h2>The smallest possible game</h2>
 *
 * <pre>{@code
 * import gooeygame.*;
 *
 * public class Hello implements GameListener {
 *     private double x = 0;
 *
 *     public void init() { }
 *
 *     public void update(double dt) {
 *         x += 200 * dt;    // 200 pixels per second
 *     }
 *
 *     public void draw(Draw d) {
 *         d.clear("black");
 *         d.fillCircle(x, 300, 20, "yellow");
 *     }
 *
 *     public static void main(String[] args) {
 *         GameRunner.start(new Hello(), "Hello", 800, 600);
 *     }
 * }
 * }</pre>
 *
 * <h2>Drawing</h2>
 *
 * <p>(0, 0) is the top-left corner of the window, and y increases downward.
 * Positions and sizes are {@code double}, so ints work too. Pass a color
 * name such as {@code "red"} or a hex code such as {@code "#FF8800"} to any
 * drawing method. See {@link gooeygame.Draw} for shapes, text, images and the
 * camera, and {@link gooeygame.SpriteSheet} for animation frames and tiles.</p>
 *
 * <h2>Keyboard and mouse</h2>
 *
 * <p>Ask {@link gooeygame.Input} what is happening right now, for example
 * {@code Input.isKeyDown(Key.LEFT)} while a key is held, or
 * {@code Input.wasKeyPressed(Key.SPACE)} for one tick per press. Type
 * {@code Key.} and VS Code suggests every key.</p>
 *
 * <h2>Images and sounds</h2>
 *
 * <p>Put images and sounds in your project's {@code assets} folder (next to
 * {@code src} and {@code lib}) and load them by name: {@code "ship.png"} reads
 * {@code assets/ship.png}. Use {@link gooeygame.Sound} for short effects that
 * play often and {@link gooeygame.Music} for long background tracks.
 * {@code .wav} files work out of the box. MP3 and OGG need extra decoder jars
 * in your {@code lib} folder.</p>
 *
 * <h2>Screens (optional)</h2>
 *
 * <p>Once a game has a menu, levels or a game-over page, each one can be a
 * {@link gooeygame.Screen}. The library does not switch screens for you: your
 * own game class keeps the current screen and passes {@code update} and
 * {@code draw} along to it.</p>
 *
 * <h2>Errors</h2>
 *
 * <p>Mistakes print friendly messages instead of crashing. A missing image
 * draws a pink box and warns once, and a missing sound warns once and stays
 * silent. If your own code throws an exception, the game stops and prints a
 * short explanation.</p>
 */
package gooeygame;
