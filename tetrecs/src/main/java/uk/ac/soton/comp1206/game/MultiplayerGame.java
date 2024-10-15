package uk.ac.soton.comp1206.game;

import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.*;
/**
 * The MultiplayerGame class handles the logic for the multiplayer challenge and extends the Game class.
 */
public class MultiplayerGame extends Game{

    /**
     * The queue of pieces from which the pieces are taken from
     */
    private ArrayList<GamePiece> piecesQueue;
    /**
     * The communicator which is used to send messages
     */
    private Communicator communicator;
    /**
     * The list of current users in the game and their scores
     */
    private ArrayList<String> usersAndScores;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator the communicator to use when sending requests
     */
    public MultiplayerGame(int cols, int rows,Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
        piecesQueue = new ArrayList<>();
        usersAndScores = new ArrayList<>();

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

        //Send a request for pieces
        sendPiecesRequest();

        currentBlockAimedAt = new int[]{0, 0};
        aimChangedListener.aimChanged(currentBlockAimedAt,currentBlockAimedAt);

        startTimeLoop();
        gameLoopListener.gameLoop(timer,false,false);
        multimedia = new Multimedia();

        communicator.send("BOARD " + grid.getGridState());


    }


    /**
     * Update the users and scores arraylist with the score for the specified user
     * @param nameAndScore the name and score of the user whos score is to be updated
     */
    public void updateScores(String nameAndScore) {

        if (usersAndScores.isEmpty()) {
            usersAndScores.add(nameAndScore);
        } else {
            boolean updateNameAndScore = false;
            boolean nameAndScoreInList = false;
            int indexToReplace = 0;
            for (String userAndScore: usersAndScores) {
                if (Objects.equals(userAndScore.split(":")[0], nameAndScore.split(":")[0])) {
                    indexToReplace = usersAndScores.indexOf(userAndScore);
                    updateNameAndScore = true;
                    nameAndScoreInList = true;
                }

            }
            if (updateNameAndScore) {
                usersAndScores.set(indexToReplace,nameAndScore);
            }
            if (!nameAndScoreInList) {
                usersAndScores.add(nameAndScore);
            }
        }


    }

    /**
     * Returns the users and scores list
     * @return usersAndScores, a list of users and scores
     */
    public ArrayList<String> getUsersAndScores() {
        return usersAndScores;
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
     * Handle what should happen after a piece is played
     */
    public void afterPiece() {

        communicator.send("BOARD " + grid.getGridState());
        //Sends a BOARD message with the current state of the board

        timer.cancel();
        startTimeLoop();
        gameLoopListener.gameLoop(timer,false,false);


        clearLines();
        piecesQueue.remove(0);
        piecesQueue.remove(0);
        currentPiece = nextPiece;
        nextPiece = piecesQueue.get(0);
        nextPieceListener.nextPiece(currentPiece,nextPiece);
        checkIfNeedToUpdateQueue();

        logger.info("Your new piece is: " + currentPiece.getValue());

    }

    /**
     * updates the score for the user
     * @param lines the number of lines cleared
     * @param blocksCleared the number of blocks cleared
     */
    public void updateScore(int lines, int blocksCleared) {
        score.set(score.getValue() + (lines * 10 * blocksCleared * multiplier.getValue()));
        communicator.send("SCORE " + score.getValue());
    }

    /**
     * Handles the logic for the loop of the game
     */
    public void gameLoop() {

        multiplier.set(1);
        multimedia.playAudioFile("/sounds/lifelose.wav");

        if (lives.getValue()-1==-1) {
            //If lives run out, then the game ends
            communicator.send("DIE");

            communicator.clearListeners();
            timer.cancel();
            gameLoopListener.gameLoop(timer,true,true);

            piecesQueue.remove(0);
            piecesQueue.remove(0);
            currentPiece = nextPiece;
            nextPiece = piecesQueue.get(0);
            checkIfNeedToUpdateQueue();


            nextPieceListener.nextPiece(currentPiece,nextPiece);


        } else {
            piecesQueue.remove(0);
            piecesQueue.remove(0);
            currentPiece = nextPiece;
            nextPiece = piecesQueue.get(0);
            checkIfNeedToUpdateQueue();

            timer.cancel();
            gameLoopListener.gameLoop(timer,true,false);
            startTimeLoop();

            nextPieceListener.nextPiece(currentPiece,nextPiece);
            lives.set(lives.getValue()-1);
            communicator.send("LIVES " + lives.getValue());
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
     * Sends a request to the server for pieces
     */
    private void sendPiecesRequest() {
     for (int i = 0; i<10;i++) {
         communicator.send("PIECE");
        }
    }

    /**
     * Checks if the pieces queue needs to be updated and if so,
     * another pieces request is sent
     */
    private void checkIfNeedToUpdateQueue() {
        if (piecesQueue.size()<5) {
            sendPiecesRequest();
        }
    }

    /**
     * Updates the piece queue
     * @param pieceType the type of piece to be added to the queue
     */
    public void updatePiecesQueue(String pieceType) {
        pieceType = pieceType.substring(6);
        GamePiece gamePiece = GamePiece.createPiece(Integer.parseInt(pieceType));

        piecesQueue.add(gamePiece);

        if (piecesQueue.size()==2) {

            //On the first creation of the pieces queue, set the currentPiece and the nextPiece
            currentPiece = piecesQueue.get(0);
            nextPiece = piecesQueue.get(1);
            nextPieceListener.nextPiece(currentPiece,nextPiece);
        }
    }

}
