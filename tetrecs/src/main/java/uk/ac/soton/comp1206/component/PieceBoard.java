package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * A MultiplayerDisplayBoard is a visual component which inherits from GameBoard.
 */
public class PieceBoard extends GameBoard{

    /**
     * Create a new PieceBoard, based off the number of columns,rows and a visual width and height.
     * @param cols number of columns
     * @param rows number of rows
     * @param width the visual width
     * @param height the visual height
     */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * Displays a piece on the board
     * @param piece the piece to display
     */
    public void displayPiece(GamePiece piece) {
        //Clear itself first
        resetBoard();
        grid.playPiece(1,1,piece);
    }

    /**
     * Clears the board
     */
    public void resetBoard() {
        grid.resetGrid();
    }

}
