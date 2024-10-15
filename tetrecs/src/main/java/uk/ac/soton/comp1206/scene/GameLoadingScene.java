package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


/**
 * The scene which is displayed before the menu scene and when the application is first run
 */
public class GameLoadingScene extends BaseScene{


    /**
     * Create a new game loading scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public GameLoadingScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
    }

    /**
     * Build the loading screen layout
     */
    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var startPane = new StackPane();
        startPane.setMaxWidth(gameWindow.getWidth());
        startPane.setMaxHeight(gameWindow.getHeight());
        root.getChildren().add(startPane);


        Image ecsGamesImage = new Image(getClass().getResource("/images/ECSGames.png").toExternalForm());

        // Create an ImageView to display the image
        ImageView ecsGamesImageView = new ImageView(ecsGamesImage);
        ecsGamesImageView.setFitWidth(ecsGamesImage.getWidth()/2);
        ecsGamesImageView.setFitHeight(ecsGamesImage.getHeight()/2);


        //Fade animation to fade in the image
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(4.5), ecsGamesImageView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);

        //Fade animation to fade out the image
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), ecsGamesImageView);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);

        startPane.getChildren().add(ecsGamesImageView);


        fadeIn.setOnFinished(event -> {
            fadeOut.play();
        });
        fadeOut.setOnFinished(event -> {
            startPane.getChildren().remove(ecsGamesImageView);
            loadMenu();
        });
        fadeIn.play();
        multimedia.playAudioFile("/sounds/intro.mp3");


    }

    /**
     * Loads the menu scene
     */
    private void loadMenu() {
        gameWindow.startMenu();
    }
}
