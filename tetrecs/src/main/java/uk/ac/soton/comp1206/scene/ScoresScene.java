package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;

/**
 * The scores scene for the game. Displayed after either a single player game or a multiplayer game
 */
public class ScoresScene extends BaseScene{

    /**
     * The game to get the scores from
     */
    private Game game;
    /**
     * A list for the local scores
     */
    private SimpleListProperty<Pair<String,Integer>> localScores;
    /**
     * A list for the remote scores
     */
    private SimpleListProperty<Pair<String,Integer>> remoteScores;
    /**
     * The score list component for the local scores
     */
    private ScoresList localScoresListComponent;
    /**
     * The score list component for the remote scores
     */
    private ScoresList remoteScoresListComponent;
    /**
     * The communications listener to receive information from
     */
    private CommunicationsListener communicationsListener;
    /**
     * The pane onto where the scores are set
     */
    private BorderPane mainPane;
    /**
     * The user who set a new highscore;
     */
    private String newHighScoreUser;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     * @param game the game for which the scores are displayed
     */
    public ScoresScene(GameWindow gameWindow,Game game) {
        super(gameWindow);
        this.game = game;

    }

    /**
     * Initialise the scores scene
     */
    @Override
    public void initialise() {
        this.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {

                returnToMenu();
            }
        });
    }

    /**
     * Build the scores scene layout
     */
    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.getStyleClass().add("challenge-background");
        root.getChildren().add(scoresPane);


        mainPane = new BorderPane();
        scoresPane.getChildren().add(mainPane);

        setUpCommunicator();
        createScoreLists();
        loadScores();

        if (!(game instanceof MultiplayerGame)) {
            if ((game.getScore().getValue() > localScoresListComponent.orderByScore(localScores).get(0).getValue())) {


                //If the current game is not a multiplayer and a new high score is set, then a new high score message is displayed


                Text title = new Text("New Highscore!");
                title.getStyleClass().add("title");
                TextField textField = new TextField();
                textField.setPromptText("Enter name");

                Button submitName = new Button("Submit");

                var enterNameHbox = new HBox();
                enterNameHbox.getChildren().addAll(textField,submitName);

                var headingPane = new StackPane();
                mainPane.setTop(headingPane);
                headingPane.getChildren().add(title);
                StackPane.setAlignment(title,Pos.CENTER);

                var anchorPane = new AnchorPane();
                mainPane.setCenter(anchorPane);
                anchorPane.getChildren().add(enterNameHbox);
                AnchorPane.setLeftAnchor(enterNameHbox,300.0);
                AnchorPane.setTopAnchor(enterNameHbox,200.0);


                textField.setOnKeyPressed(event -> {
                    //The name entered is stored and the user and score is saved to a file
                    if (event.getCode() == KeyCode.ENTER) {
                        String userInput = textField.getText();
                        localScores.add(new Pair<>(userInput, game.getScore().getValue()));
                        writeScores();
                        newHighScoreUser = userInput;

                        requestOnlineScore();

                    }
                });

                submitName.setOnAction(event -> {
                    //The name entered is stored and the user and score is saved to a file
                    String userInput = textField.getText();
                    localScores.add(new Pair<>(userInput, game.getScore().getValue()));
                    writeScores();
                    writeOnlineScores(userInput,game.getScore().getValue());
                    newHighScoreUser = userInput;

                    requestOnlineScore();

                });

            } else {
                //If the user did not set a new high score, then display as normal
                requestOnlineScore();

            }
        } else {
            //If instance is a multiplayer game then the local scores will contain the scores for the game,
            //and the online scores will dislpay
            requestOnlineScore();
        }

        multimedia.playBackgroundMusic("/music/menu.mp3");

    }


    /**
     * Displays the scores
     */
    private void displayScores() {

        //Removes any existing scores set before adding new
        mainPane.getChildren().removeAll(mainPane.getChildren());
        var imageStackPane = new StackPane();


        Image tetrECSImage = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());

        // Create an ImageView to display the image
        ImageView tetrECSImageView = new ImageView(tetrECSImage);
        tetrECSImageView.setFitWidth(tetrECSImage.getWidth()/8);
        tetrECSImageView.setFitHeight(tetrECSImage.getHeight()/8);
        tetrECSImageView.setPreserveRatio(true);


        //Adjust positioning of the image and title
        var gameInfoPane = new AnchorPane();
        Text gameOver = new Text("GAME OVER!");
        gameOver.getStyleClass().add("title");
        gameInfoPane.getChildren().add(gameOver);
        imageStackPane.getChildren().add(gameInfoPane);
        AnchorPane.setTopAnchor(gameOver,170.0);
        AnchorPane.setLeftAnchor(gameOver,250.0);


        Text currentScores = new Text("Local Scores:");
        if (game instanceof MultiplayerGame) {
            currentScores = new Text("Lobby Scores:");
            //The local scores are set as the multiplayer scores instad of reading from file
        }

        //Set heading for local/multiplayer scores
        currentScores.getStyleClass().add("scoreHeading");
        gameInfoPane.getChildren().add(currentScores);
        AnchorPane.setTopAnchor(currentScores,220.0);
        AnchorPane.setLeftAnchor(currentScores,90.0);

        //Set heading for online scores
        Text onlineScoresHeading = new Text("Online Scores:");
        onlineScoresHeading.getStyleClass().add("scoreHeading");
        gameInfoPane.getChildren().add(onlineScoresHeading);
        AnchorPane.setTopAnchor(onlineScoresHeading,220.0);
        AnchorPane.setLeftAnchor(onlineScoresHeading,570.0);



        //Adjust positioning of image
        mainPane.setTop(imageStackPane);
        imageStackPane.getChildren().add(tetrECSImageView);
        StackPane.setAlignment(imageStackPane,Pos.TOP_CENTER);

        //Vbox for the local/multiplayer scores
        VBox localScores = localScoresListComponent.getScoreVbox();
        mainPane.setLeft(localScores);
        localScores.setPadding(new Insets(10,0,0,100));

        //Cuts of the number of scores if greater than 10
        remoteScores = (SimpleListProperty<Pair<String, Integer>>) remoteScoresListComponent.orderByScore(remoteScores);
        while (remoteScores.size()>10) {
            remoteScores.remove(10);
        }

        //Vbox for the online scores
        VBox onlineScores = remoteScoresListComponent.getScoreVbox();
        mainPane.setRight(onlineScores);
        onlineScores.setPadding(new Insets(10,100,0,0));
    }

    /**
     * Creates the score lists which hold the scores
     */
    private void createScoreLists() {

        ArrayList<Pair<String, Integer>> localScoresList = new ArrayList<>();

        // Create an observable list from the ArrayList
        ObservableList<Pair<String, Integer>> observableLocalScoresList = FXCollections.observableArrayList(localScoresList);

        // Create a SimpleListProperty as a wrapper around the observable list
        localScores = new SimpleListProperty<>(observableLocalScoresList);

        localScoresListComponent = new ScoresList();
        localScoresListComponent.bind(localScores);


        ArrayList<Pair<String, Integer>> remoteScoresList = new ArrayList<>();

        // Create an observable list from the ArrayList
        ObservableList<Pair<String, Integer>> observableRemoteScoresList = FXCollections.observableArrayList(remoteScoresList);

        // Create a SimpleListProperty as a wrapper around the observable list
        remoteScores = new SimpleListProperty<>(observableRemoteScoresList);

        remoteScoresListComponent = new ScoresList();
        remoteScoresListComponent.bind(remoteScores);


    }

    /**
     * Returns to the menu
     */
    private void returnToMenu() {
        multimedia.stopMusic();
        gameWindow.cleanup();
        gameWindow.startMenu();
    }

    /**
     * Loads the local/multiplayer scores for the game
     */
    private void loadScores() {


        if (game instanceof MultiplayerGame) {
            //If the game object is an instance of the multiplayer game, then scores are retrieved from it
            for (String usernameAndScore: ((MultiplayerGame) game).getUsersAndScores()) {
                String name = usernameAndScore.split(":")[0];
                int score = Integer.parseInt(usernameAndScore.split(":")[1]);
                localScores.add(new Pair<>(name,score));
            }

        } else {
            //If the game object is an instance of the game, then scores are retrieved from the Scores.txt file
            String fileName = "Scores.txt";

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // Split the line by ":" to separate key and value
                    String[] nameAndScore = line.split(":");

                    String name = nameAndScore[0].trim();
                    int score = Integer.parseInt(nameAndScore[1].trim());
                    //remove trailing spaces

                    if (localScores.size() < 9) {
                        localScores.add(new Pair<>(name,score));
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Writes a new high score into a file
     */
    private void writeScores() {

        String fileName = "Scores.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Pair<String, Integer> pair : localScoresListComponent.orderByScore(localScores)) {
                writer.write(pair.getKey() + ":" + pair.getValue()); // Writing the pair in format "String:Integer"
                writer.newLine(); // Adding newline after each pair
            }
            System.out.println("Data has been written to the file successfully!");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }


    /**
     * Loads the online scores for the game
     */
    private void loadOnlineScores(String communication) {

        communication = communication.substring(9);

        for(String nameAndScore :communication.split("\n")) {
            String name = nameAndScore.split(":")[0].trim();
            Integer score = Integer.valueOf(nameAndScore.split(":")[1].trim());

            //Only takes top 10 from the online scores list
            if (remoteScores.size() < 10) {
                remoteScores.add(new Pair<>(name,score));
            }
        }

        if (newHighScoreUser!=null) {
            remoteScores.add(new Pair<>(newHighScoreUser,game.getScore().getValue()));
        }


        //Displays both Online scores and local/multiplayer scores
        Platform.runLater(this::displayScores);

    }

    /**
     * Sets up communicator used to listen for incoming messages
     */
    private void setUpCommunicator() {
        communicationsListener = communication -> {

            if (communication.startsWith("HISCORES")) {
                loadOnlineScores(communication);
            }
        };
        this.gameWindow.getCommunicator().addListener(communicationsListener);
    }

    /**
     * Sends a HIGH SCORES request to the server via the communicator
     */
    private void requestOnlineScore() {
        this.gameWindow.getCommunicator().send("HISCORES DEFAULT");
    }

    /**
     * Submits the user's score to the server using the communicator
     */
    private void writeOnlineScores(String userName, int newScore) {

        this.gameWindow.getCommunicator().send("HISCORE <" + userName + ">:" + newScore);

    }



}
