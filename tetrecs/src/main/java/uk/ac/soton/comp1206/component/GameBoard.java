package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RotateClickedListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Set;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {
    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;

    /**
     * The listener to call if a rotate key is pressed
     */
    private RotateClickedListener rotateClickedListener;

    /**
     * value used to update the hover effect on the game blocks
     */
    private boolean isAimHoverDisplayed;

    /**
     * The current block which the mouse is hovering over
     */
    private GameBlock currentMouseHoverBlock;

    /**
     * The current block which the keyboard is hovering over
     */
    private GameBlock currentKeyboardHoverBlock;


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with its own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }


        //If it is a piece board then the hover effect shouldn't apply to its blocks,
        // and it has different input controls to rotate the block

        if (this instanceof PieceBoard) {
            if (!(this instanceof MultiplayerDisplayBoard)) {
                //Only set the center circle if it is a piece board and not a multiplayer board
                getBlock((int) (double) (cols / 2), (int) (double) (rows / 2)).setCenterCircle();
            }

            //Rotate the piece on the event that
            this.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    rotateClicked(event,"Pieceboard left click",false);
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    rotateClicked(event,"Pieceboard",true);
                }
            });

        } else {

            this.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    rotateClicked(event,"Gameboard",true);
                }
            });
            //Sets the initial value of aimHoveDisplay to false
            setAimHoverDisplayed(false);

        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     * @return gameBlock
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));


        //depending on the instance of the game board, it responds to mouse events differently
        if (!(this instanceof PieceBoard)) {
            block.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    blockClicked(event,block);
                }
            });
            block.setOnMouseEntered(event -> {

                currentMouseHoverBlock = block;
                if (currentKeyboardHoverBlock!=null) {
                    currentKeyboardHoverBlock.removeHover();

                    if (currentKeyboardHoverBlock.equals(currentMouseHoverBlock)) {
                        //Removes hover so that two hover effects aren't applied at the same time by keyboard and mouse
                        currentKeyboardHoverBlock.removeHover();
                    }
                }

                setAimHoverDisplayed(false);
                block.setHover();
                //change state to hovered
            });
            block.setOnMouseExited(event -> {
                block.removeHover();
                //remove hovered state
            });
        }

        return block;
    }

    /**
     * Set the listener when passed in through the parameter as the listener for the class
     * @param rotateClickedListener listener to add
     */
    public void setOnRotateClicked(RotateClickedListener rotateClickedListener) {
        this.rotateClickedListener = rotateClickedListener;
    }

    /**
     * Triggered when a rotate key or mouse button is pressed
     * @param event the event which triggered the method
     * @param message the message to display
     * @param rotateRight boolean value to check whether to rotate to the right or left
     */
    private void rotateClicked(MouseEvent event,String message,Boolean rotateRight) {

        if(rotateClickedListener != null) {
            rotateClickedListener.rotateClicked(message,rotateRight);
        }
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block.getValue());

        if(blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }

    /**
     * Set the listener to handle an event when a block is hovered by keyboard
     * @param currentKeyboardHoverBlock listener to add
     */
    public void setCurrentKeyboardHoverBlock(GameBlock currentKeyboardHoverBlock) {
        this.currentKeyboardHoverBlock = currentKeyboardHoverBlock;
    }

    /**
     * Change the value of isAimHoverDisplayed based on the parameter
     * @param aimHoverDisplayed the value to set
     */
    public void setAimHoverDisplayed(boolean aimHoverDisplayed) {
        isAimHoverDisplayed = aimHoverDisplayed;
    }

    /**
     * Removes the hover effect on the block
     */
    public void removeMouseHover() {
        if (currentMouseHoverBlock!=null && currentKeyboardHoverBlock!=currentMouseHoverBlock) {
            //Removes hover so that two hover effects aren't applied at the same time by keyboard and mouse
            currentMouseHoverBlock.removeHover();
        }

    }

    /**
     * Plays an animation for when a line of blocks are cleared
     * @param blocksToFade the set of block on which the fade animation should be applied to
     */
    public void  playFadeOutAnimation(Set<String> blocksToFade) {

        for (String blockToFade : blocksToFade) {
            String[] strings = blockToFade.replace("[", "").replace("]", "").split(", ");
            int[] intCoords = new int[strings.length];
            for (int i = 0; i < intCoords.length; i++) {
                intCoords[i] = Integer.parseInt(strings[i]);
            }
            getBlock(intCoords[0], intCoords[1]).fadeOut();
        }

    }

}
