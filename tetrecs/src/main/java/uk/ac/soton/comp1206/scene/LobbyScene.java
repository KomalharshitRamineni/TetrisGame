package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


/**
 * The lobby for the game, use to join or host channels from which games can be started from
 */
public class LobbyScene extends BaseScene{

    /**
     * The Timer used to request channels
     */
    private Timer timer;
    /**
     * An array list of the current channels
     */
    private ArrayList<String> channels;
    /**
     * The chat area displayed to view messages
     */
    private TextArea chatArea;
    /**
     * The input field for sending messages
     */
    private TextField inputField;
    /**
     * The pane on which all the chat relevant nodes are added to
     */
    private AnchorPane chatPane;
    /**
     * The name of the current channel the user is in
     */
    private String currentChannelName;

    /**
     * The communications listener used to receive messages from the server
     */
    private CommunicationsListener communicationsListener;

    /**
     * The Vbox where the channels are displayed on
     */
    private VBox channelsVBox;

    /**
     * The Vbox where the option to host a channel is displayed on
     */
    private VBox hostVBox;

    /**
     * used to check if the current user is the host of a lobby
     */
    private boolean isHost;

    /**
     * The Hbox used to display the current users in a lobby
     */
    private HBox currentUsersVBox;

    /**
     * The name of the current user
     */
    private String currentPlayerName;
    /**
     * An array list containing the users who were in the lobby when the game starts
     */
    private ArrayList<String> usersWhoStartedGame;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
    }


    /**
     * Initialise the lobby
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
     * returns to the menu screen
     */
    public void returnToMenu() {
        gameWindow.getCommunicator().send("PART");
        multimedia.stopMusic();
        timer.cancel();
        gameWindow.cleanup();
        gameWindow.startMenu();
    }


    /**
     * Build the Lobby layout
     */
    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new AnchorPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);


        //Creates a pane and VBox to display the current users in a channel
        var currentUsers = new AnchorPane();
        currentUsersVBox = new HBox();

        lobbyPane.getChildren().add(currentUsers);

        //Adjusts the positioning of the Vbox
        currentUsers.getChildren().add(currentUsersVBox);
        AnchorPane.setTopAnchor(currentUsersVBox,70.0);
        AnchorPane.setLeftAnchor(currentUsersVBox,250.0);

        //Creates a pane for chat related nodes
        chatPane = new AnchorPane();
        lobbyPane.getChildren().add(chatPane);

        channelsVBox = new VBox();
        hostVBox = new VBox(10);

        //Vbox to display headings, the host option and a Vbox to display available channels
        var channelsInfoVbox = new VBox(10);

        var multiplayerHeading = new Text("Multiplayer");
        multiplayerHeading.getStyleClass().add("multiplayerHeading");
        var joinAChannelHeading = new Text("Join a channel:");
        joinAChannelHeading.getStyleClass().add("multiplayerJoin");

        channelsInfoVbox.getChildren().addAll(multiplayerHeading,hostVBox,joinAChannelHeading,channelsVBox);
        
        Text hostGame = new Text("Host a game");
        hostGame.getStyleClass().add("menuItem");
        hostGame.setOnMouseClicked(event -> {
            revealTextField(hostVBox);
        });

        hostVBox.getChildren().add(hostGame);


        //Creates a channels info pane to hold the channelsInfoVbox and adjusts its positioning
        var channelsInfoPane = new AnchorPane();
        channelsInfoPane.getChildren().add(channelsInfoVbox);
        lobbyPane.getChildren().add(channelsInfoPane);

        AnchorPane.setTopAnchor(channelsInfoPane,10.0);
        AnchorPane.setLeftAnchor(channelsInfoPane,10.0);

        isHost = false;
        setUpCommunicationsListener();
        repeatChanelRequest();


        multimedia = new Multimedia();
        multimedia.playBackgroundMusic("/music/menu.mp3");

    }


    /**
     * Reveals the input filed to allow the user to enter the name of a channel to host
     */
    private void revealTextField(VBox hostVBox) {

        hostVBox.getChildren().removeIf(node -> node instanceof TextField);

        TextField textField = new TextField();
        textField.setPromptText("Enter channel name");

        hostVBox.getChildren().add(textField);
        textField.requestFocus();

        textField.setOnAction(event -> {

            //A channel is created with the given name
            createNewChanel(textField.getText());
            sendRequestToChanel();
            hostVBox.getChildren().removeIf(node -> node instanceof TextField);
        });

    }
    /**
     * Displays the chat area for the channel
     */
    private void displayChatArea() {


        //Displays the name of the current channel and adjusts its positioning
        if (currentChannelName!=null) {
            Text channelName = new Text(currentChannelName);
            channelName.getStyleClass().add("multiplayerJoin");
            chatPane.getChildren().add(channelName);
            AnchorPane.setTopAnchor(channelName,40.0);
            AnchorPane.setLeftAnchor(channelName,250.0);
        }

        //Creates a new text area for the chat
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.getStyleClass().add("customTextArea");


        var chatVBox = new VBox();
        chatVBox.setPrefSize(500,600);


        chatArea.appendText("Type /NICK nickname to change your name\n");
        chatArea.getStyleClass().add("TextField");
        chatArea.setPrefHeight(400);


        //Input field for typing messages
        inputField = new TextField();
        inputField.setPromptText("Enter message");
        inputField.setOnAction(event -> sendMessage());

        //Creates a new HBox for the input field to type a message and the send button
        var inputFieldAndSendHBox = new HBox();
        inputFieldAndSendHBox.getChildren().add(inputField);
        inputField.setPrefSize(450,10);

        chatVBox.getChildren().addAll(chatArea,inputFieldAndSendHBox);


        //Adjusts portioning for the chatVBox
        chatPane.getChildren().add(chatVBox);
        chatVBox.setPadding(new Insets(100,0,0,0));
        AnchorPane.setLeftAnchor(chatVBox,250.0);


        //Button used to send messages
        Button sendButton = new Button("Send");
        sendButton.setOnAction(event -> sendMessage());
        inputFieldAndSendHBox.getChildren().add(sendButton);
        sendButton.setPrefSize(50,10);


        //Button used to leave the channel
        Button leaveChannelButton = new Button("Leave game");
        leaveChannelButton.setOnAction(event -> leaveChannel());
        chatPane.getChildren().add(leaveChannelButton);
        //Position of the button adjusted
        AnchorPane.setTopAnchor(leaveChannelButton,470.0);
        AnchorPane.setLeftAnchor(leaveChannelButton,255.0);


        if (isHost) {
            //If the user is host, then another button to host the game is displayed
            Button startGameButton = new Button("Start game");
            startGameButton.setOnAction(event -> sendStartRequest());
            chatPane.getChildren().add(startGameButton);
            AnchorPane.setTopAnchor(startGameButton,470.0);
            AnchorPane.setLeftAnchor(startGameButton,670.0);
        }

    }

    /**
     * Sends a request to start the game
     */
    private void sendStartRequest() {
        gameWindow.getCommunicator().send("START");
    }

    /**
     * Starts the game by loading the multiplayer scene
     */
    private void startGame() {
        timer.cancel();
        multimedia.stopMusic();
        gameWindow.loadMultiplayerScene(currentPlayerName,usersWhoStartedGame);
    }

    /**
     * Removes the chat from the display
     */
    private void clearChatArea() {
        chatPane.getChildren().remove(0,chatPane.getChildren().size());
    }

    /**
     * Leaves the current channel
     */
    private void leaveChannel() {
        currentChannelName = null;
        currentUsersVBox.getChildren().clear();
        gameWindow.getCommunicator().send("PART");
        isHost = false;
        clearChatArea();
        sendRequestToChanel();

    }


    /**
     * Sends a message to the channel
     */
    private void sendMessage() {

        String message = inputField.getText().trim();

        if (message.startsWith("/NICK")) {
            message = message.substring(5);
            setNewNickName(message);
        }

        if (!message.isEmpty()) {
            inputField.clear();
            gameWindow.getCommunicator().send("MSG "+message);
        }
    }

    /**
     * Sends a request to change the user's nickname
     * @param nickName the name to change the user's name to
     */
    private void setNewNickName(String nickName) {
        gameWindow.getCommunicator().send("NICK " + nickName);
    }

    /**
     * Processes the incoming messages and adds to the chat area
     * @param string the message received
     */
    private void processIncomingMessages(String string) {
        string = string.substring(4);
        String sender = string.split(":")[0];
        String message = string.split(":")[1];
        chatArea.appendText(sender + ": " + message + "\n");
    }

    /**
     * Updates the display of the current available channels
     */
    private void updateChannelDisplay() {

        var channelsBox = new VBox();
        //First removes all channels currently displayed
        channelsVBox.getChildren().remove(0, channelsVBox.getChildren().size());
        channelsVBox.getChildren().add(channelsBox);

        //Then loops through the list of channels and adds the channels to the display
        if (channels!=null) {
            for (String chanelName:channels) {
                Text text = new Text(chanelName);
                text.getStyleClass().add("menuItem");
                text.setOnMouseClicked(event -> {
                    joinChanel(chanelName);

                });
                channelsBox.getChildren().add(text);
            }

        }
    }

    /**
     * Sends a request to create a new channel
     */
    private void createNewChanel(String channelName) {
        gameWindow.getCommunicator().send("CREATE <"+channelName+">");
    }

    /**
     * Sends a request to join a channel
     */
    private void joinChanel(String chanelName) {
        gameWindow.getCommunicator().send("JOIN " + chanelName);

    }

    /**
     * Sends a request to get the list of channels
     */
    private void sendRequestToChanel() {
        gameWindow.getCommunicator().send("LIST");
    }


    /**
     * Sets up the communications listener to process incoming messages from the server
     */
    private void setUpCommunicationsListener() {
        communicationsListener = communication -> {

            if (communication.startsWith("CHANNELS")) {
                int iterations=0;
                channels = new ArrayList<String >();
                for(String channelNames :communication.split("\n")) {
                    if (iterations==0) {
                        channelNames = channelNames.substring(9);
                    }
                    channels.add(channelNames);
                    iterations ++;
                }
            } if (communication.startsWith("MSG")) {
                multimedia.playAudioFile("/sounds/message.wav");
                processIncomingMessages(communication);
                //update

            } if (communication.startsWith("HOST")) {
                isHost = true;
                Platform.runLater(this::displayChatArea);

            } if (communication.startsWith("JOIN")) {
                currentChannelName = "Current channel: " + communication.substring(5);
                isHost = false;
                //
                multimedia.playAudioFile("/sounds/message.wav");
                Platform.runLater(this::displayChatArea);
            } if (communication.startsWith("USERS")) {
                setUsers(communication);
                Platform.runLater(() -> {
                    displayUsersInChanel(communication);
                });

            } if (communication.startsWith("START")) {
                Platform.runLater(this::startGame);
            } if (communication.startsWith("NICK")) {
                setCurrentName(communication);
            } if (communication.startsWith("ERROR")) {
                Platform.runLater(() -> {
                    displayError(communication);
                });
            }

        };

        gameWindow.getCommunicator().addListener(communicationsListener);
    }

    /**
     * Updates the users, who are currently in the channel
     * @param string the users
     */
    private void setUsers(String string) {
        string = string.substring(6);
        usersWhoStartedGame = new ArrayList<>();
        usersWhoStartedGame.addAll(Arrays.asList(string.split("\n")));
    }

    /**
     * Sets the current name of the user
     * @param string name of user
     */
    private void setCurrentName(String string) {
        currentPlayerName = string.substring(5);
    }

    /**
     * Displays an error message when an error is received
     * @param error message of the error
     */
    private void displayError(String error) {
        String errorMessage = error.substring(6);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("ERROR");
        alert.setContentText(errorMessage);

        alert.show();
    }


    /**
     * Displays the users that are currently in the channel
     * @param users users int the channel
     */
    private void displayUsersInChanel(String users) {
        //First clears the display for the users
        currentUsersVBox.getChildren().clear();
        users = users.substring(6);


        Text usersLabel = new Text("Users: ");
        usersLabel.getStyleClass().add("usersInChannel");
        currentUsersVBox.getChildren().add(usersLabel);

        //Then loops through the users and adds them to the display
        for(String user: users.split("\n")) {
            Text newUser = new Text(user);
            newUser.getStyleClass().add("usersInChannel");
            currentUsersVBox.getChildren().add(newUser);
        }

    }


    /**
     * Sets a timer which continuously sends a LIST request to the server to update the channels
     */
    private void repeatChanelRequest() {

        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                sendRequestToChanel();

                Platform.runLater(() -> {
                    updateChannelDisplay();
                });

            }
        };

        // Schedule the task to run every 2.5 seconds
        timer.schedule(task, 0, 2500);
    }


}
