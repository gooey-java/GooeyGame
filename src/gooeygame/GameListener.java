package gooeygame;

/**
 * The one interface every game implements. Write a class that implements
 * GameListener, fill in the three methods, and launch it with
 * GameRunner.start(...).
 *
 * That is all you need for a complete game. (Screens are an optional
 * extra for later; see the Screen interface.)
 */
public interface GameListener {

    /**
     * Called once, before the first update. Create your objects and set up
     * your starting state here.
     */
    void init();

    /**
     * Called about 60 times per second. Move things and check the rules here.
     *
     * @param dt seconds since the last update (about 0.016).
     *           Multiply speeds by dt: x += speed * dt;
     */
    void update(double dt);

    /**
     * Called after every update. Draw the current state of your game with
     * the Draw object. Don't change your game's state in here.
     *
     * @param d the thing you draw with
     */
    void draw(Draw d);
}
