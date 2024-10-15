package uk.ac.soton.comp1206.scene;

import javafx.animation.FillTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
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
import uk.ac.soton.comp1206.component.MultiplayerDisplayBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class MultiplayerScene extends ChallengeScene{

    private CommunicationsListener communicationsListener;
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private MultiplayerGame game;
    private String currentPlayerName;

    private Text chatArea;
    private TextField inputField;

    private VBox versusDisplay;
    private ArrayList<String> existingUsers;

    /**
     * Create a new Multi Player challenge scene
     * @param gameWindow the Game Window
     * @param currentPlayerName the name of the current player
     * @param existingUsers the users in the multiplayer game
     */
    public MultiplayerScene(GameWindow gameWindow,String currentPlayerName,ArrayList<String> existingUsers) {
        super(gameWindow);
        this.currentPlayerName = currentPlayerName;
        this.existingUsers = existingUsers;
    }

    /**
     * Build the Multi Player window
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

        var multiplayerExclusiveUI = new AnchorPane();
        challengePane.getChildren().add(multiplayerExclusiveUI);



        //A chat area is added to the screen
        var chatInputAndOutputDisplay = new HBox();

        inputField = new TextField();
        inputField.setPromptText("Type your message here...");
        inputField.setOnAction(event -> sendMessage());
        inputField.setVisible(false);

        chatArea = new Text();
        chatArea.getStyleClass().add("chatMessage");

        chatInputAndOutputDisplay.getChildren().addAll(inputField,chatArea);



        var info = new Text("Press T to access chat");
        info.getStyleClass().add("chatMessage");
        multiplayerExclusiveUI.getChildren().addAll(chatInputAndOutputDisplay,info);

        //Positioning of the chat area adjusted
        AnchorPane.setTopAnchor(chatInputAndOutputDisplay,550.0);
        AnchorPane.setLeftAnchor(chatInputAndOutputDisplay,150.0);
        AnchorPane.setTopAnchor(info,100.0);
        AnchorPane.setLeftAnchor(info,150.0);

        versusDisplay = new VBox();
        //A new VBox which contains the users who the current player is against,
        //along with their score and their boards.

        for(String user: existingUsers) {
            if (!Objects.equals(user, currentPlayerName)) {
                var userVbox = new VBox();
                var userInfo = new Text("  " + user + ":0");
                userInfo.getStyleClass().add("versusDisplay");
                userVbox.getChildren().add(userInfo);

                var userBoard = new MultiplayerDisplayBoard(5,5,60,60);
                userBoard.setPadding(new Insets(10,0,10,20));
                userVbox.getChildren().add(userBoard);

                versusDisplay.getChildren().add(userVbox);
            }

        }

        //Positioning of the versus display is adjusted
        multiplayerExclusiveUI.getChildren().add(versusDisplay);
        AnchorPane.setTopAnchor(versusDisplay,100.0);



        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);


        //A new game board is created for the game and set on the screen
        gameBoard = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(gameBoard);
        gameBoard.setPadding(new Insets(0,0,0,100));

        var pieceDisplayPane = new AnchorPane();


        //Two piece boards are created, one for the current piece and one for the next piece and they are displayed
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


        Text modeTitle = new Text("Multiplayer");
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
    @Override
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }


    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        game = new MultiplayerGame(5, 5,gameWindow.getCommunicator());
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
        setUpCommunicationsListener();
        //send an initial scores request
        gameWindow.getCommunicator().send("SCORES");


        //Handles keys pressed changing the current aim
        this.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                game.cancelTimer();
                if (timer!=null) {
                    timer.cancel();
                }
                returnToMenu();
            }

            if (event.getCode() == KeyCode.T) {
                displayChatInput();
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
        gameWindow.getCommunicator().send("DIE");
        multimedia.stopMusic();
        gameWindow.cleanup();
        gameWindow.startMenu();
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
     * Clears the relevant lines on the game board
     * @param  gameBlockcoordinates the set of game blocks which are to be cleared
     */
    @Override
    public void lineCleared(Set<String> gameBlockcoordinates) {
        gameBoard.playFadeOutAnimation(gameBlockcoordinates);
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
            logger.info("GAME ENDDED");
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
     * Processes messages that are received and displays them
     * @param string the message that was received
     */
    private void processIncomingMessages(String string) {
        string = string.substring(4);
        String sender = string.split(":")[0];
        String message = string.split(":")[1];
        chatArea.setText("   "+sender + ": " + message);

    }

    /**
     * Sets up communicator used to listen for incoming messages
     */
    public void setUpCommunicationsListener() {
        communicationsListener = communication -> {
            if (communication.startsWith("PIECE")) {
                game.updatePiecesQueue(communication);
            } if (communication.startsWith("MSG")) {
                multimedia.playAudioFile("/sounds/message.wav");
                processIncomingMessages(communication);
            } if (communication.startsWith("SCORES")) {
                Platform.runLater(() -> {
                    updateScores(communication);
                });
            } if (communication.startsWith("BOARD")) {
                Platform.runLater(() -> {
                    updateVersusBoards(communication);
                });
            }
        };

        gameWindow.getCommunicator().addListener(communicationsListener);
    }


    /**
     * Updates the game boards of the users playing against
     * @param communication the BOARD message received
     */
    private void updateVersusBoards(String communication) {

        communication = communication.substring(6);

        String boardOwner = communication.split(":")[0].trim();
        String boardState = communication.split(":")[1].trim();

        //Checks for the user in the versusDisplay and updates their board accordingly
        for(Node vBox: versusDisplay.getChildren()) {
            Node textNode = ((VBox) vBox).getChildren().get(0);

            String[] nodeInfo = ((Text) textNode).getText().split(":");
            if (Objects.equals(boardOwner, nodeInfo[0].trim())) {
                MultiplayerDisplayBoard multiplayerDisplayBoard = (MultiplayerDisplayBoard) ((VBox) vBox).getChildren().get(1);
                multiplayerDisplayBoard.setBoardDisplay(boardState);
            }
        }
    }


    /**
     * Updates the scores of the users playing against
     * @param scores the SCORES message received
     */
    private void updateScores(String scores) {

        scores = scores.substring(7);
        String[] infoForEachPlayer = scores.split("\n");

        //Loops through every user in the scores provided
        for (String info : infoForEachPlayer) {
            String[] playerInfo = info.split(":");


            game.updateScores(playerInfo[0] + ":" + playerInfo[1]);

            //Loops through every node in the versus display
            for(Node vBox: versusDisplay.getChildren()) {
                Node textNode = ((VBox) vBox).getChildren().get(0);
                //Text node contains the name of the user

                String[] nodeInfo = ((Text) textNode).getText().split(":");
                if (Objects.equals(playerInfo[0], nodeInfo[0].trim())) {
                    //If the user in the scores matches the user in the versus display, then it updates their score

                    //if dead, then apply the relevant styling to display their death
                    if (Objects.equals(playerInfo[2], "DEAD")) {

                        textNode.getStyleClass().clear();
                        textNode.getStyleClass().add("userDead");
                        ((Text) textNode).setStrikethrough(true);

                    } else {
                        ((Text) textNode).setText("  "+playerInfo[0] + ":" + playerInfo[1]);
                    }

                }

            }
        }
    }

    /**
     * Displays the input box to for the chat
     */
    private void displayChatInput() {
        inputField.setVisible(true);
    }

    /**
     * Sends the message entered by the user to the server
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            inputField.clear();
            gameWindow.getCommunicator().send("MSG "+message);
            inputField.setVisible(false);
        }
    }
}
