package uk.ac.soton.comp1206.game;


import javafx.beans.property.IntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.event.AimChangedListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.multimedia.Multimedia;

import java.util.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    /**
     * Logger used to log information
     */
    protected static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * A listener which is triggered when the next piece is generated
     */
    protected NextPieceListener nextPieceListener;
    /**
     * A listener which is triggered changes are made to the game loop
     */
    protected GameLoopListener gameLoopListener;
    /**
     * A listener which is triggered when changes are made to the aim
     */
    protected AimChangedListener aimChangedListener;
    /**
     * A listener which is triggered when a line of blocks are cleared
     */
    protected LineClearedListener lineClearedListener;
    /**
     * The current gamePiece that is to be played
     */
    protected GamePiece currentPiece;
    /**
     * The next gamePiece that is to be played after the current one
     */
    protected GamePiece nextPiece;

    /**
     * The score for the game
     */
    protected SimpleIntegerProperty score;
    /**
     * The current level for the game
     */

    protected SimpleIntegerProperty level;
    /**
     * The number of lives for the game
     */

    protected SimpleIntegerProperty lives;
    /**
     * The multiplier for the game
     */
    protected SimpleIntegerProperty multiplier;
    /**
     * The block which is currently being aimed at by keyboard
     */
    protected int[] currentBlockAimedAt;

    /**
     * The Multimedia object used to play sounds and music
     */
    protected Multimedia multimedia;

    /**
     * The Timer object to keep track of the game loop
     */
    protected Timer timer;
    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        lives = new SimpleIntegerProperty(3);
        score = new SimpleIntegerProperty(0);
        level = new SimpleIntegerProperty(0);
        multiplier = new SimpleIntegerProperty(1);

    }

    /**
     * returns the current level
     * @return level
     */
    public IntegerProperty getLevel() {
        return level;
    }

    /**
     * returns the current number of lives
     * @return lives
     */
    public IntegerProperty getLives() {
        return lives;
    }

    /**
     * returns the current score
     * @return score
     */
    public IntegerProperty getScore() {
        return score;
    }

    /**
     * returns the current multiplier
     * @return multiplier
     */
    public IntegerProperty getMultiplier() {
        return multiplier;
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {

        logger.info("Initialising game");

        //Current and next piece are generated

        currentPiece = generatePiece();
        nextPiece = generatePiece();
        nextPieceListener.nextPiece(currentPiece,nextPiece);
        logger.info("current piece = " + currentPiece.toString());

        //Initial aim set
        currentBlockAimedAt = new int[]{0, 0};
        aimChangedListener.aimChanged(currentBlockAimedAt,currentBlockAimedAt);

        //Game loop started
        startTimeLoop();
        gameLoopListener.gameLoop(timer,false,false);
        multimedia = new Multimedia();

    }


    /**
     * Generates a random piece
     * @return GamePiece that was generated
     */
    public GamePiece generatePiece() {

        Random random = new Random();
        int pieceType = random.nextInt(0,15);
        return GamePiece.createPiece(pieceType);

    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */

    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("new block aim: {},{}",x,y );

        if (grid.canPlayPiece(x,y,currentPiece)) {
            grid.playPiece(x,y,currentPiece);
            multimedia.playAudioFile("/sounds/place.wav");

            afterPiece();
        } else {
            multimedia.playAudioFile("/sounds/fail.wav");
        }

    }

    /**
     * Handle what should happen when a block is placed at the current aim
     */
    public void placePieceAtAim() {
        int x = currentBlockAimedAt[0];
        int y = currentBlockAimedAt[1];
        if (grid.canPlayPiece(x,y,currentPiece)) {
            grid.playPiece(x,y,currentPiece);
            multimedia.playAudioFile("/sounds/place.wav");
            afterPiece();
        } else {
            multimedia.playAudioFile("/sounds/fail.wav");
        }
    }

    /**
     * Change the the current aim
     * @param x the x coordinate of the new aim
     * @param y the y coordinate of the new aim
     */
    public void changeCurrentAim(int x, int y) {
        //Checks if can change the aim further before changing it
        if (grid.checkIfCoordsOnGrid(currentBlockAimedAt,x,y)) {

            int[] previousAim = currentBlockAimedAt.clone();

            currentBlockAimedAt[0] = currentBlockAimedAt[0] + x;
            currentBlockAimedAt[1] = currentBlockAimedAt[1] + y;

            aimChangedListener.aimChanged(previousAim,currentBlockAimedAt);

        }

        logger.info("new block aim: {},{}",currentBlockAimedAt[0],currentBlockAimedAt[1] );
    }

    /**
     * Handle what should happen after a piece is played
     */
    public void afterPiece() {

        timer.cancel();
        startTimeLoop();
        gameLoopListener.gameLoop(timer,false,false);

        clearLines();
        currentPiece = nextPiece;
        nextPiece = generatePiece();
        nextPieceListener.nextPiece(currentPiece,nextPiece);
        logger.info("Your new piece is: " + currentPiece.getValue());

    }


    /**
     * Check the board for any lines that need to be cleared and clear them
     */
    public void clearLines() {

        HashSet<String> blocksToClear = new HashSet<>();
        int clearedLines = 0;

        //A hashset is created and then two loops are run to add any blocks that are to be cleared to the hashset

        for (int i = 0; i < grid.getRows(); i++) {
            int[][] currentColumn = new int[getRows()][2];
            for (int j = 0; j < grid.getCols(); j++) {
                currentColumn[j][0] = i;
                currentColumn[j][1] = j;
            }
            if (shouldLineBeCleared(currentColumn)) {
                clearedLines += 1;

                for (int[] ints : currentColumn) {
                    if (!blocksToClear.contains(Arrays.toString(new int[]{ints[0], ints[1]}))) {
                        blocksToClear.add(Arrays.toString(new int[]{ints[0], ints[1]}));
                    }
                }
            }
        }

        for (int i = 0; i < grid.getCols(); i++) {
            int[][] currentRow = new int[getCols()][2];
            for (int j = 0; j < grid.getRows(); j++) {
                currentRow[j][0] = j;
                currentRow[j][1] = i;
            }
            if (shouldLineBeCleared(currentRow)) {
                clearedLines += 1;
                for (int k = 0; k < currentRow.length; k++) {
                    if (!blocksToClear.contains(Arrays.toString(new int[]{currentRow[k][0], currentRow[k][1]}))) {
                        blocksToClear.add(Arrays.toString(new int[]{currentRow[k][0], currentRow[k][1]}));
                    }
                }
            }
        }


        Iterator<String> blocksToClearIterator = blocksToClear.iterator();
        if (!blocksToClear.isEmpty()) {
            updateScore(clearedLines,blocksToClear.size());
            multiplier.set(multiplier.getValue() + 1);
            level.set((int) Math.floor((double) score.getValue() /1000));
            //updates level
            //send blocks to clear animation
            lineClearedListener.lineCleared(blocksToClear);

        } else {
            multiplier.set(1);
        }
        while (blocksToClearIterator.hasNext()) {
            clearBlocks(blocksToClearIterator.next());

        }

    }

    /**
     * Updates the score
     * @param lines the number of lines that were cleared
     * @param blocksCleared the number of blocks that were cleared
     */
    public void updateScore(int lines, int blocksCleared) {

        score.set(score.getValue() + (lines * 10 * blocksCleared * multiplier.getValue()));
    }

    /**
     * checks if a line of blocks should be cleared
     * @param line an array of lines which are to be checked if they should be cleared or note
     * @return completeLine a boolean value indicating whether the line should be cleared or not.
     */
    public boolean shouldLineBeCleared(int[][] line) {
        //Check if every coordinated given contains a block
        boolean completeLine = true;

        for (int i = 0; i < line.length; i++) {
            if (grid.get(line[i][0],line[i][1]) == 0 ) {
                completeLine = false;
            }
        }
        return completeLine;
    }

    /**
     * Clears the blocks passed in as the parameter
     * @param blockCoords the coordinates of blocks to be cleared
     */
    public void  clearBlocks(String blockCoords) {

        String[] strings = blockCoords.replace("[", "").replace("]", "").split(", ");
        int intCoords[] = new int[strings.length];
        for (int i = 0; i < intCoords.length; i++) {
            intCoords[i] = Integer.parseInt(strings[i]);
        }

        grid.set(intCoords[0],intCoords[1],0);
        multimedia.playAudioFile("/sounds/clear.wav");

    }


    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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
     * Set the NextPieceListener when passed in through the parameter as the NextPieceListener for the class
     * @param listener NextPieceListener to set
     */
    public void setNextPieceListener(NextPieceListener listener) {
        nextPieceListener = listener;
    }

    /**
     * Set the GameLoopListener when passed in through the parameter as the GameLoopListener for the class
     * @param listener GameLoopListener to set
     */
    public void setGameLoopListener(GameLoopListener listener) {gameLoopListener = listener;}
    /**
     * Set the AimChangedListener when passed in through the parameter as the AimChangedListener for the class
     * @param listener AimChangedListener to set
     */
    public void setAimChangedListener(AimChangedListener listener) {
        aimChangedListener = listener;
    }
    /**
     * Set the LineClearedListener when passed in through the parameter as the LineClearedListener for the class
     * @param listener LineClearedListener to set
     */
    public void setLineClearedListener(LineClearedListener listener) {
        lineClearedListener = listener;
    }

    /**
     * Rotates the current piece to the right
     */
    public void rotateCurrentPieceRight() {
        currentPiece.rotateRight();
        multimedia.playAudioFile("/sounds/rotate.wav");
    }

    /**
     * Rotates the current piece to the left
     */
    public void rotateCurrentPieceLeft() {
        currentPiece.rotateLeft();
        multimedia.playAudioFile("/sounds/rotate.wav");
    }

    /**
     * Swaps the current piece and the next piece
     */
    public void swapCurrentPiece() {
        GamePiece currentPieceTemp = currentPiece;

        currentPiece = nextPiece;
        nextPiece = currentPieceTemp;

        multimedia.playAudioFile("/sounds/rotate.wav");
    }

    /**
     * Gets the current GamePiece
     * @return currentPiece the current piece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }
    /**
     * Gets the next GamePiece
     * @return nextPiece the next piece
     */
    public GamePiece getNextPiece() {
        return nextPiece;
    }


    /**
     * Gets the timerDelay based on the current level
     * @return timerDelay is how much time the user has to place a piece
     */
    public long getTimerDelay() {
        long maxDelay = 2500;
        long change = 12000 - (500L * level.getValue());
        if (change <= maxDelay) {
            return maxDelay;
        } else {
            return change;
        }
    }

    /**
     * Handles the logic for the loop of the game
     */
    public void gameLoop() {
        multiplier.set(1);
        multimedia.playAudioFile("/sounds/lifelose.wav");


        if (lives.getValue()-1==-1) {
            //if the user runs out of lives then the game should end
            timer.cancel();
            gameLoopListener.gameLoop(timer,true,true);
            currentPiece = nextPiece;
            nextPiece = generatePiece();

            nextPieceListener.nextPiece(currentPiece,nextPiece);


        } else {
            currentPiece = nextPiece;

            nextPiece = this.generatePiece();

            timer.cancel();
            gameLoopListener.gameLoop(timer,true,false);
            startTimeLoop();

            nextPieceListener.nextPiece(currentPiece,nextPiece);
            lives.set(lives.getValue()-1);
        }

    }


    /**
     * Starts a time loop which keeps on repeating changing the time for it to run at by calling the getTimerDelay method
     */
    public void startTimeLoop() {
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                gameLoop();
            }
        };

        timer.schedule(task, getTimerDelay(), getTimerDelay());
    }

    /**
     * Cancels the time loop
     */
    public void cancelTimer() {
        timer.cancel();
    }

}
