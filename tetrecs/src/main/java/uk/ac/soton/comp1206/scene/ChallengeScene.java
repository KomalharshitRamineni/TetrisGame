package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.*;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.Set;
import java.util.Timer;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, RotateClickedListener, AimChangedListener, LineClearedListener, GameLoopListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * The game object for this scene
     */
    protected Game game;

    /**
     * The PieceBoard for the current piece
     */
    protected PieceBoard currentPieceboard;
    /**
     * The PieceBoard for the next piece
     */
    protected PieceBoard nextPieceboard;

    /**
     * The GameBoard object for the game
     */
    protected GameBoard gameBoard;

    /**
     * The time bar that is set at the bottom of the screen to show the time left
     */
    protected Rectangle timerBar;
    /**
     * The transition used for the time bar
     */
    protected FillTransition fillTransition;
    /**
     * The transition used for the time bar
     */
    protected Transition widthTransition;
    /**
     * The pane used for the time bar
     */
    protected StackPane timeBarPane;
    /**
     * The timer for the current game
     */
    protected Timer timer;
    /**
     * The text which displays the current score
     */
    protected Text scoreText;
    /**
     * The text which displays the high score
     */
    private Text highScoreText;
    /**
     * The highscore;
     */
    private int highScore;



    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);


        //Add high score text to the screen
        highScore = getHighScore();
        highScoreText = new Text("Highscore: " + highScore);
        highScoreText.getStyleClass().add("level");

        //Adjust positioning for the high score
        var highScorePane = new AnchorPane();
        highScorePane.getChildren().add(highScoreText);
        challengePane.getChildren().add(highScorePane);
        AnchorPane.setTopAnchor(highScoreText,60.0);
        AnchorPane.setLeftAnchor(highScoreText,5.0);


        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //Create a new game board
        gameBoard = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(gameBoard);


        var pieceDisplayPane = new AnchorPane();


        //Create two piece boards one for the current piece, and one for the next piece
        currentPieceboard = new PieceBoard(3,3,gameWindow.getWidth()/6,gameWindow.getWidth()/6);
        Text currentPieceText = new Text("Current Piece:");
        currentPieceText.getStyleClass().add("pieceInfo");

        nextPieceboard = new PieceBoard(3,3,gameWindow.getWidth()/8,gameWindow.getWidth()/8);
        Text nextPieceText = new Text("Next Piece:");
        nextPieceText.getStyleClass().add("pieceInfo");

        pieceDisplayPane.getChildren().addAll(currentPieceboard,nextPieceboard,currentPieceText,nextPieceText);

        //Adds the relevant game info nodes to the pane

        Text lives = new Text();
        lives.textProperty().bind(game.getLives().asString("Lives: %d"));
        lives.getStyleClass().add("lives");
        scoreText = new Text();
        scoreText.textProperty().bind(game.getScore().asString("Score: %d"));
        scoreText.getStyleClass().add("score");


        Text multiplier = new Text();
        multiplier.textProperty().bind(game.getMultiplier().asString("Multiplier: %d"));
        multiplier.getStyleClass().add("multiplier");
        Text level = new Text();
        level.textProperty().bind(game.getLevel().asString("Level: %d"));
        level.getStyleClass().add("level");


        Text modeTitle = new Text("SinglePlayer");
        modeTitle.getStyleClass().add("title");

        var topPane = new StackPane();

        //Adjusts the positioning of the nodes on the screen
        topPane.getChildren().addAll(scoreText,modeTitle,lives);
        StackPane.setAlignment(scoreText, Pos.CENTER_LEFT);

        StackPane.setAlignment(lives,Pos.CENTER_RIGHT);
        topPane.setPadding(new Insets(5));

        mainPane.setTop(topPane);

        //Create a new Vbox for the relevant information and sets their positioning on the screen
        var levelInfo = new VBox(10);
        pieceDisplayPane.getChildren().add(levelInfo);
        levelInfo.getChildren().addAll(level,multiplier);

        double distanceOfCurrentPieceboardFromTop = ((double) gameWindow.getHeight() /4 + (currentPieceText.getFont().getSize()*3));
        double distanceOfNextPieceboardFromTop = (double) gameWindow.getHeight() /1.8 + (nextPieceText.getFont().getSize()*3);


        AnchorPane.setTopAnchor(currentPieceboard,distanceOfCurrentPieceboardFromTop );
        AnchorPane.setTopAnchor(nextPieceboard,distanceOfNextPieceboardFromTop);

        AnchorPane.setTopAnchor(currentPieceText,(distanceOfCurrentPieceboardFromTop - currentPieceText.getFont().getSize()*3));
        AnchorPane.setTopAnchor(nextPieceText,distanceOfNextPieceboardFromTop - nextPieceText.getFont().getSize()*3);

        AnchorPane.setRightAnchor(currentPieceboard,(double) gameWindow.getWidth()/10);

        AnchorPane.setLeftAnchor(nextPieceboard,(double) ((gameWindow.getWidth()/6) - (gameWindow.getWidth()/8))/2);
        AnchorPane.setLeftAnchor(nextPieceText,(double) ((gameWindow.getWidth()/6) - (gameWindow.getWidth()/8))/2);

        AnchorPane.setTopAnchor(levelInfo,(double) gameWindow.getHeight()/8);

        mainPane.setRight(pieceDisplayPane);



        //Creates and adds a time bar pane for the time bar
        timeBarPane = new StackPane();
        mainPane.setBottom(timeBarPane);



        //Handles the game board being clicked to place a block as well as to rotate it
        gameBoard.setOnBlockClick(this::blockClicked);
        gameBoard.setOnRotateClicked(this::rotateClicked);
        //Handles the piece board being clicked to rotate the block
        currentPieceboard.setOnRotateClicked(this::rotateClicked);

        multimedia.playBackgroundMusic("/music/game.wav");

    }


    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }


    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        game = new Game(5, 5);
        game.setNextPieceListener(this);
        game.setAimChangedListener(this);
        game.setLineClearedListener(this);
        game.setGameLoopListener(this);

    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();

        this.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                game.cancelTimer();
                if (timer!=null) {
                    timer.cancel();
                }
                returnToMenu();
            }

            //Handles keys pressed changing the current aim

            if (event.getCode() == KeyCode.UP) {
                game.changeCurrentAim(0,-1);
            }
            if (event.getCode() == KeyCode.LEFT) {
                game.changeCurrentAim(-1,0);
            }
            if (event.getCode() == KeyCode.RIGHT) {
                game.changeCurrentAim(1,0);
            }
            if (event.getCode() == KeyCode.DOWN) {
                game.changeCurrentAim(0,1);
            }
            if (event.getCode() == KeyCode.W) {
                game.changeCurrentAim(0,-1);
            }
            if (event.getCode() == KeyCode.A) {
                game.changeCurrentAim(-1,0);
            }
            if (event.getCode() == KeyCode.D) {
                game.changeCurrentAim(1,0);
            }
            if (event.getCode() == KeyCode.S) {
                game.changeCurrentAim(0,1);
            }


            //Handles keys pressed to place the piece at the aim
            if (event.getCode() == KeyCode.ENTER) {
                game.placePieceAtAim();

            }
            if (event.getCode() == KeyCode.X) {
                game.placePieceAtAim();

            }

            //Handles keys pressed to swap the current and next piece
            if (event.getCode() == KeyCode.SPACE) {
                game.swapCurrentPiece();
                currentPieceboard.displayPiece(game.getCurrentPiece());
                nextPieceboard.displayPiece(game.getNextPiece());
                logger.info("swapped");
            }
            if (event.getCode() == KeyCode.R) {
                game.swapCurrentPiece();
                currentPieceboard.displayPiece(game.getCurrentPiece());
                nextPieceboard.displayPiece(game.getNextPiece());
                logger.info("swapped");
            }


            //Handles keys pressed to rotate the current piece to the left and right
            if (event.getCode() == KeyCode.Q) {
                game.rotateCurrentPieceLeft();
                currentPieceboard.displayPiece(game.getCurrentPiece());
            }
            if (event.getCode() == KeyCode.E) {
                game.rotateCurrentPieceRight();
                currentPieceboard.displayPiece(game.getCurrentPiece());
            }
            if (event.getCode() == KeyCode.Z) {
                game.rotateCurrentPieceLeft();
                currentPieceboard.displayPiece(game.getCurrentPiece());
            }
            if (event.getCode() == KeyCode.C) {
                game.rotateCurrentPieceRight();
                currentPieceboard.displayPiece(game.getCurrentPiece());
            }

            if (event.getCode() == KeyCode.OPEN_BRACKET) {
                game.rotateCurrentPieceLeft();
                currentPieceboard.displayPiece(game.getCurrentPiece());
            }
            if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                game.rotateCurrentPieceRight();
                currentPieceboard.displayPiece(game.getCurrentPiece());
            }

        });
    }

    /**
     * Returns to the menu
     */
    protected void returnToMenu() {
        multimedia.stopMusic();
        gameWindow.cleanup();
        gameWindow.startMenu();
    }


    /**
     * Receives the updated current piece and next piece from the game and updates the piece boards
     * @param currentPiece the new current piece
     * @param nextPiece the new next piece
     */
    public void nextPiece(GamePiece currentPiece,GamePiece nextPiece) {
        updatePieceBoards(currentPiece,nextPiece);
    }

    /**
     * Updates the piece boards with the new current piece and next piece
     * @param currentPiece the new current piece
     * @param nextPiece the new next piece
     */
    public void updatePieceBoards(GamePiece currentPiece, GamePiece nextPiece) {
        currentPieceboard.displayPiece(currentPiece);
        nextPieceboard.displayPiece(nextPiece);

    }

    /**
     * Rotates the pieces when the piece has been rotated
     * @param message the message to display
     * @param rotateRight used to check whether to rotate to the right or left
     */
    public void rotateClicked(String message, Boolean rotateRight) {
        logger.info(message + " clicked and piece has been rotated!");
        if (rotateRight) {
            game.rotateCurrentPieceRight();
        } else {
            game.rotateCurrentPieceLeft();
        }

        currentPieceboard.displayPiece(game.getCurrentPiece());
    }

    /**
     * Changes the current aim on the game board
     * @param previousAim the block which was aimed at previously
     * @param currentAim the block which the aim is now set to
     */
    @Override
    public void aimChanged(int[] previousAim, int[] currentAim) {

        gameBoard.getBlock(previousAim[0],previousAim[1]).removeHover();
        gameBoard.getBlock(currentAim[0],currentAim[1]).setHover();

        gameBoard.setCurrentKeyboardHoverBlock(gameBoard.getBlock(currentAim[0],currentAim[1]));
        gameBoard.removeMouseHover();

    }

    /**
     * Clears the relevant lines on the game board
     * @param  gameBlockcoordinates the set of game blocks which are to be cleared
     */
    @Override
    public void lineCleared(Set<String> gameBlockcoordinates) {
        gameBoard.playFadeOutAnimation(gameBlockcoordinates);
        if (game.getScore().getValue() > highScore) {
            highScoreText.textProperty().bind(game.getScore().asString("Highscore: %d"));
        }
    }

    /**
     * The game loop for the game
     * @param timer the timer of the game
     * @param runningOnDifferentThread used to check if the timer is running on a different thread
     * @param endGame used to check whether to end the game or not
     */
    @Override
    public void gameLoop(Timer timer, boolean runningOnDifferentThread,boolean endGame) {
        this.timer = timer;

        if (!endGame) {
            game.getTimerDelay();
            setTimerBar(runningOnDifferentThread);
        } else {

            logger.info("GAME ENDED");
            multimedia.stopMusic();
            Platform.runLater(() -> {
                gameWindow.displayScores(game);
            });
        }

    }


    /**
     * Sets the timer bar at the bottom of the screen
     * @param runOnDifferentThread used to check which thread the current time bar is running on
     */
    public void setTimerBar(Boolean runOnDifferentThread) {

        stopAnimations();
        removeTimeBar(runOnDifferentThread);


        double initialWidth = gameWindow.getWidth(); // Initial width of the timer bar
        double animationDuration = game.getTimerDelay(); // Animation duration in milliseconds

        timerBar = new Rectangle(0, 0, initialWidth, 10);
        timerBar.setFill(Color.GREEN);

        // Create a FillTransition to animate the color change
        fillTransition = new FillTransition(Duration.millis(animationDuration), timerBar);
        fillTransition.setFromValue(Color.GREEN);
        fillTransition.setToValue(Color.RED);

        // Create a Transition to animate the width change
        widthTransition = new Transition() {
            {
                setCycleDuration(Duration.millis(animationDuration));
            }
            @Override
            protected void interpolate(double frac) {
                // Update the width of the timer bar

                timerBar.setWidth(initialWidth * (1 - frac));
                //timerBar.translateXProperty();

                // Update the fill color based on the interpolation ratio
                Color color = Color.GREEN.interpolate(Color.RED, frac);
                timerBar.setFill(color);
            }
        };

        fillTransition.play();
        widthTransition.play();


        Platform.runLater(() -> {

            timeBarPane.getChildren().add(timerBar);
            timeBarPane.setPrefSize(gameWindow.getWidth(),10);
        });

    }

    /**
     * Stops the animations for the time bar
     */
    private void stopAnimations() {
        if (fillTransition != null) {
            fillTransition.stop();
        }
        if (widthTransition != null) {
            widthTransition.stop();
        }
    }

    /**
     * Removes the time bar from the screen
     * @param runOnDifferentThread is used to check which thread the time bar is running on
     */
    private void removeTimeBar(boolean runOnDifferentThread) {
        if (timerBar != null && timeBarPane.getChildren().contains(timerBar)) {
            if (runOnDifferentThread) {
                Platform.runLater(() -> {
                    timeBarPane.getChildren().remove(timerBar);
                });
            } else {
                timeBarPane.getChildren().remove(timerBar);
            }

        }
    }

    /**
     * Gets the current high score saved in scores.txt to display
     * @return high score
     */
    private int getHighScore() {
        String fileName = "Scores.txt";

        int highScore = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Split the line by ":" to separate key and value
                String[] nameAndScore = line.split(":");

                int score = Integer.parseInt(nameAndScore[1].trim());
                //remove trailing spaces

                if (score > highScore) {
                    highScore = score;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return highScore;
    }

}

