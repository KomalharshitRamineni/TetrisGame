package uk.ac.soton.comp1206.event;

import java.util.Set;

/**
 * The LineClearedListener is used for listening for when a line of blocks are cleared in the game
 */
public interface LineClearedListener {

    /**
     * Handle when a line of blocks have been cleared is changed
     * @param gameBlockcoordinates the set of blocks which were cleared
     */
    public void lineCleared(Set<String> gameBlockcoordinates);

}
