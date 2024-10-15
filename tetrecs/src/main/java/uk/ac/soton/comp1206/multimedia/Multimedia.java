package uk.ac.soton.comp1206.multimedia;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;



/**
 * The Visual User Interface component representing a single block in the grid.
 * Extends Canvas and is responsible for drawing itself.
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class Multimedia {

    /**
     * The media which is played
     */
    private Media media;
    /**
     * The MediaPlayer which is used to play sound effects
     */
    private MediaPlayer soundPlayer;

    /**
     * The MediaPlayer which is used to play background music
     */
    private MediaPlayer musicPlayer;

    /**
     * Create a new Multimedia object
     */
    public Multimedia() {
    }

    /**
     * Plays background music
     * @param resource the file form which the media is created and played
     */
    public void playBackgroundMusic(String resource) {

        media = new Media(getClass().getResource(resource).toExternalForm());
        musicPlayer = new MediaPlayer(media);
        musicPlayer.setOnEndOfMedia(() -> musicPlayer.seek(javafx.util.Duration.ZERO));
        musicPlayer.play();
        // Background music is looped indefinitely

    }
    /**
     * Plays a sound effect
     * @param resource the file form which the media is created and played
     */
    public void playAudioFile(String resource) {
        media = new Media(getClass().getResource(resource).toExternalForm());
        soundPlayer = new MediaPlayer(media);
        soundPlayer.play();
    }
    /**
     * Stops the background music
     */
    public void stopMusic() {
        musicPlayer.stop();
    }

}
