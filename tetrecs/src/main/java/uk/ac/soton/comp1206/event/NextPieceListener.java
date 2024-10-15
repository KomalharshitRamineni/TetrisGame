package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;
/**
 * The NextPieceListener is used for listening for when a new piece is set
 */
public interface NextPieceListener {

    /**
     * Handle when a new piece is set after a piece has been placed.
     * @param currentPiece the new current GamePiece
     * @param nextPiece the new next GamePiece
     */
    public void nextPiece(GamePiece currentPiece, GamePiece nextPiece);

}
