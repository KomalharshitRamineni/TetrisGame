package uk.ac.soton.comp1206.event;


import java.util.Timer;

/**
 * The GameLoopListener is used to listen to changes to the loop of the game, i.e.
 * repeat of the game loop or end of the game loop.
 */
public interface GameLoopListener {
    /**
     * Handle changes are made to the game which cause the game loop to repeat or end
     * @param timer the timer of the current gameLoop
     * @param runningOnDifferentThread used to know which thread the gameLoop is running on
     * @param endGame used to check if the game has ended or not
     */
    public void gameLoop(Timer timer,boolean runningOnDifferentThread, boolean endGame);

}
