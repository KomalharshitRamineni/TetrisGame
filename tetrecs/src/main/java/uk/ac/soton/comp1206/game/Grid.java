package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }


    /**
     * gets the surrounding 3x3 blocks from the pint specified
     * @param x the x coordinate of the block midpoint
     * @param y the y coordinate of the block midpoint
     * @return a 3x3 3D array
     */
    public int [][][] getSurrounding3x3(int x, int y) {

        return new int[][][]{{{x-1,y+1}, {x,y+1}, {x+1,y+1}}
                , {{x-1,y}, {x,y}, {x+1,y}}
                , {{x-1,y-1}, {x,y-1}, {x+1,y-1}}};

    }
    /**
     * checks if a piece can be played at the given coordinates
     * @param x the x coordinate of the block midpoint
     * @param y the y coordinate of the block midpoint
     * @param piece the piece which is to be played at the coordinates
     * @return canPlay if the block can be played at the given coordinates
     */

    public boolean canPlayPiece(int x, int y, GamePiece piece) {
        boolean canPlay = true;

        //Checks if within the bounds of the grid first
        //Then checks if it overlaps with any existing pieces

        int[][][] getSurroundingBlocks = getSurrounding3x3(x,y);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (piece.getBlocks()[i][j] != 0) {
                    if (getSurroundingBlocks[i][j][0] > getCols() || getSurroundingBlocks[i][j][1] > getRows()) {
                        canPlay = false;
                    }

                    if (get(getSurroundingBlocks[i][j][0],getSurroundingBlocks[i][j][1])!=0) {
                        canPlay = false;
                    }
                }
            }

        }
        return canPlay;
    }


    /**
     * places a piece at a given coordinate
     * @param x the x coordinate of the block midpoint
     * @param y the y coordinate of the block midpoint
     * @param piece the piece which is to be played at the coordinates
     */

    public void playPiece(int x, int y, GamePiece piece) {

        int[][][] getSurroundingBlocks = getSurrounding3x3(x,y);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (piece.getBlocks()[i][j] != 0) {
                    set(getSurroundingBlocks[i][j][0],getSurroundingBlocks[i][j][1],piece.getBlocks()[i][j]);
                }
            }
        }
    }

    /**
     * checks if the given coordinates are on the grid
     * @param currentBlockAimedAt the current block which is aimed ate
     * @param x the x coordinate of the block midpoint
     * @param y the y coordinate of the block midpoint
     * @return valid, returns if the coords given are on the grid
     */

    public boolean checkIfCoordsOnGrid(int[] currentBlockAimedAt,int x, int y) {

        boolean valid = true;
        int xAfterChange = x + currentBlockAimedAt[0];
        int yAfterChange = y + currentBlockAimedAt[1];
        if ((xAfterChange<0 || xAfterChange > getRows()-1) || yAfterChange<0 || yAfterChange>getCols()-1) {
            valid = false;
        }
        return valid;
    }

    /**
     * Completely resets the grid by setting the value of each block to 0
     */
    public void resetGrid() {
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                set(i,j,0);
            }
        }
    }

    /**
     * Gets the current state of the grid
     * @return finalOutput, the state of the grid returned as a string
     */
    public String getGridState() {
        ArrayList<Integer> stateOfGrid = new ArrayList<>();
        for (int i = 0; i < getCols(); i++) {
            for (int j = 0; j < getRows(); j++) {
                stateOfGrid.add(get(i,j));
            }
        }
        String finalOutput = "";
        for (Integer state: stateOfGrid) {
            finalOutput = finalOutput + state.toString() + " ";
        }

        return finalOutput.trim();

    }
    /**
     * Sets the current state of the grid
     * @param state the state to which the grid should be updated to
     */
    public void setGridState(String state) {
        String[] states = state.split(" ");
        Iterator<String> values = Arrays.stream(states).iterator();
        for (int i = 0; i < getCols(); i++) {
            for (int j = 0; j < getRows(); j++) {
                set(i,j,(Integer.parseInt(values.next())));
            }
        }
    }

}
