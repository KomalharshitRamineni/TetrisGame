package uk.ac.soton.comp1206.event;

/**
 * The AimChangedListener is used for listening changes to the aim of set by the keyboard,
 * it passes the previous aim and the new aim received.
 */
public interface AimChangedListener {

    /**
     * Handle when the aim is changed
     * @param previousAim the block on which was aimed at before
     * @param newAim the block on which the aim is currently at
     */
    public void aimChanged(int[] previousAim, int[] newAim);

}
