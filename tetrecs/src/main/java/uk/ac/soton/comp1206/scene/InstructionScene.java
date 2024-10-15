package uk.ac.soton.comp1206.scene;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The instructions scene of the game, explains how to play the game
 */
public class InstructionScene extends BaseScene{
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    /**
     * Initialise this scene. Called after creation
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
     * Build the instructions layout
     */
    @Override
    public void build() {


        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());


        var startPane = new StackPane();
        startPane.setMaxWidth(gameWindow.getWidth());
        startPane.setMaxHeight(gameWindow.getHeight());
        startPane.getStyleClass().add("menu-background");
        root.getChildren().add(startPane);


        Image instructionsImage = new Image(getClass().getResource("/images/Instructions.png").toExternalForm());

        ImageView instructionsImageView = new ImageView(instructionsImage);

        instructionsImageView.setFitWidth(instructionsImage.getWidth()/2.5);
        instructionsImageView.setFitHeight(instructionsImage.getHeight()/2.5);

        BorderPane borderPane = new BorderPane();

        Text heading = new Text("Instructions");
        heading.getStyleClass().add("instructionTitle");

        Text backButton = new Text("Back [ESC]");
        backButton.getStyleClass().add("menuItem");
        backButton.setOnMouseClicked(this::returnToMenu);


        var topPane = new StackPane();
        topPane.getChildren().addAll(heading,backButton);
        StackPane.setAlignment(backButton, Pos.CENTER_LEFT); //add button

        topPane.setPadding(new Insets(5));

        borderPane.setCenter(instructionsImageView);
        borderPane.setTop(topPane);



        //Dynamically generates all the piece types and adds them to the grid pane

        GridPane displayPieces = new GridPane();

        int currentGridWidth = 0;
        int currentGridRow = 0;
        int currentGridCol = 0;

        for (int i = 0; i < 15; i++) {

            PieceBoard pieceToDisplay = new PieceBoard(3,3, (double) gameWindow.getWidth()/10,(double) gameWindow.getWidth()/10);
            currentGridWidth += gameWindow.getWidth()/10;

            if (i == 0) {
                pieceToDisplay.setPadding(new Insets(0,0,0, (double) gameWindow.getWidth() /10));
                currentGridWidth += 5;
            }

            if (currentGridWidth > gameWindow.getWidth()-gameWindow.getWidth()/10) { //leaves a block space width either side
                currentGridCol +=1;
                currentGridRow=0;
                currentGridWidth = 0;

                pieceToDisplay.setPadding(new Insets(0,0,0, (double) gameWindow.getWidth() /10));
                currentGridWidth += 5;
            }
            displayPieces.add(pieceToDisplay,currentGridRow,currentGridCol);

            pieceToDisplay.displayPiece(GamePiece.createPiece(i));

            currentGridRow+=1;
        }


        borderPane.setBottom(displayPieces);
        startPane.getChildren().add(borderPane);
        multimedia.playBackgroundMusic("/music/menu.mp3");

    }

    /**
     * returns to the menu screen
     */
    private void returnToMenu() {
        multimedia.stopMusic();
        gameWindow.startMenu();
        multimedia.playAudioFile("/sounds/transition.wav");
    }
    /**
     * returns to the menu screen
     * @param event the event which triggered the method
     */
    private void returnToMenu(MouseEvent event) {
        multimedia.stopMusic();
        gameWindow.startMenu();
        multimedia.playAudioFile("/sounds/transition.wav");
    }
}
