package uk.ac.soton.comp1206.event;

/**
 * The RotateClickedListener is used for listening for when a piece is to be rotated
 */
public interface RotateClickedListener {

    /**
     * Handle when a piece is to be rotated
     * @param message the message set when the piece has been rotated
     * @param rotateRight the direction in which to rotate, left or right.
     */
    public void rotateClicked(String message,Boolean rotateRight);

}
