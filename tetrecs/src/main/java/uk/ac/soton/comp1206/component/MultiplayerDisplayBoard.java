package uk.ac.soton.comp1206.component;


/**
 * A MultiplayerDisplayBoard is a visual component which inherits from PieceBoard.
 */
public class MultiplayerDisplayBoard extends PieceBoard{

    /**
     * Create a new PieceBoard, based off the number of columns,rows and a visual width and height.
     * @param cols number of columns
     * @param rows number of rows
     * @param width the visual width
     * @param height the visual height
     */
    public MultiplayerDisplayBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * Sets and displays a custom state of the board
     * @param boardState the state of the board which is to be displayed
     */
    public void setBoardDisplay(String boardState) {
        grid.setGridState(boardState);
    }

}
