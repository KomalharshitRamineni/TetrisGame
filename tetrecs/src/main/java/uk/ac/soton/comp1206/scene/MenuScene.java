package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);


        var borderPane = new BorderPane();

        var anchorPane = new AnchorPane();

        Image tetrECSTitleImage = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());

        // Create an ImageView to display the image
        // Set the positioning for the image
        ImageView tetrECSTitleImageView = new ImageView(tetrECSTitleImage);
        tetrECSTitleImageView.setFitWidth(tetrECSTitleImage.getWidth()/8);
        tetrECSTitleImageView.setFitHeight(tetrECSTitleImage.getHeight()/8);
        tetrECSTitleImageView.setPreserveRatio(true);
        anchorPane.getChildren().add(tetrECSTitleImageView);
        AnchorPane.setLeftAnchor(tetrECSTitleImageView,116.0);
        AnchorPane.setTopAnchor(tetrECSTitleImageView,150.0);


        menuPane.getChildren().addAll(anchorPane,borderPane);


        double durationInSeconds = 8.0; // Duration of one complete rotation (in seconds)

        // Create a RotateTransition for rotating the image around its center
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(durationInSeconds), tetrECSTitleImageView);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);

        // Play the rotation animation
        rotateTransition.play();



        //Create buttons for the menu

        var singleplayerButton = new Text("Singleplayer");
        singleplayerButton.setOnMouseClicked(this::startGame);
        singleplayerButton.getStyleClass().add("menuItem");

        var multiplayerButton = new Text("Multiplayer");
        multiplayerButton.getStyleClass().add("menuItem");
        multiplayerButton.setOnMouseClicked(this::loadLobby);

        var instructionsButton = new Text("Instructions");
        instructionsButton.getStyleClass().add("menuItem");
        instructionsButton.setOnMouseClicked(this::displayInstructions);

        var exitButton = new Text("Exit");
        exitButton.setOnMouseClicked(this::exitGame);
        exitButton.getStyleClass().add("menuItem");

        VBox menuButtons = new VBox(10);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.setPadding(new Insets(150,0,0,0));
        menuButtons.getChildren().addAll(singleplayerButton,multiplayerButton,instructionsButton,exitButton);


        borderPane.setCenter(menuButtons);

        //Play menu music
        multimedia.playBackgroundMusic("/music/menu.mp3");


    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

        String fileName = "Scores.txt";

        if (!Files.exists(Paths.get(fileName))) {
            // Create the file if it doesn't exist
            try {
                Files.createFile(Paths.get(fileName));
                logger.info("File created: " + fileName);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                    //Preload some scores if the file is empty
                    writer.write("Bob:900");
                    writer.newLine();
                    writer.write("Jeff:300");
                    writer.newLine();
                    writer.write("Steve:200");
                    writer.newLine();

                    logger.info("Default scores written to the file");
                } catch (IOException e) {
                    logger.error("Error writing to file: " + e.getMessage());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        this.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.exitGame();
                gameWindow.getCommunicator().send("END");
            }
        });


    }

    /**
     * Handle when the Start Game button is pressed
     * @param event MouseEvent
     */
    private void startGame(MouseEvent event) {
        gameWindow.startChallenge();
        multimedia.stopMusic();
        multimedia.playAudioFile("/sounds/transition.wav");
    }

    /**
     * Handle when the Instructions button is pressed
     * @param event MouseEvent
     */
    private void displayInstructions(MouseEvent event) {
        gameWindow.displayInstructions();
        multimedia.stopMusic();
        multimedia.playAudioFile("/sounds/transition.wav");
    }

    /**
     * Handle when the Multiplayer button is pressed
     * @param event MouseEvent
     */
    private void loadLobby(MouseEvent event) {
        gameWindow.displayLobby();
        multimedia.stopMusic();
        multimedia.playAudioFile("/sounds/transition.wav");
    }

    /**
     * Handle when the Exit button is pressed
     * @param event MouseEvent
     */
    private void exitGame(MouseEvent event) {
        multimedia.stopMusic();
        gameWindow.exitGame();
    }

}
