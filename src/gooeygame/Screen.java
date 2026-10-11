package gooeygame;

/**
 * OPTIONAL. A Screen is one "page" of a bigger game: a menu, a level, a
 * game-over page. You never NEED one, since a game with a single GameListener
 * works fine. Use screens once your game has more than one state.
 *
 * The library does not switch screens for you. Your own GameListener class
 * keeps track of the current Screen and passes update and draw along to it.
 * Each screen is given a reference to your game class (through its
 * constructor) so it can ask for a different screen when it is done.
 * See the Pong example.
 */
public interface Screen {

    /** Called when this screen becomes the current screen. */
    default void show() { }

    /** Called every tick while this screen is current. */
    void update(double dt);

    /** Called every frame while this screen is current. */
    void draw(Draw d);

    /** Called when your game switches away from this screen. */
    default void hide() { }
}
