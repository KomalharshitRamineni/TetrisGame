package uk.ac.soton.comp1206.component;


import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * The Visual User Interface component which holds either local scores or multiplayer scores,
 * along with online scores
 */
public class ScoresList {

    /**
     * The list of scores which is initialised
     */
    private final ListProperty<Pair<String, Integer>> scoreList =  new SimpleListProperty<>();

    /**
     * Creates a ScoresList object and calls the updateList method
     */
    public ScoresList() {
        scoreList.addListener(this::updateList);
    }

    /**
     * When the scores have been received and are to be updated,
     * @param observableValue what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateList(ObservableValue<? extends ObservableList<Pair<String, Integer>>> observableValue, ObservableList<Pair<String, Integer>> oldValue, ObservableList<Pair<String, Integer>> newValue) {

    }

    /**
     * Bind the value of this score entry to another property. Used to link the visual score display to its corresponding score entry.
     * @param input property to bind the value to
     */
    public void bind(ObservableListValue<Pair<String, Integer>> input) {
        scoreList.bind(input);
    }

    /**
     * generates a VBox from its scoreList which contains text nodes of which have the user and their score
     * @return scoreVbox the VBox containing the scores as text nodes which are ordered
     */
    public VBox getScoreVbox() {

        VBox scoreVbox = new VBox();
        List<Pair<String,Integer>> orderedScoreList = orderByScore(scoreList);
        // The list is ordered from the highest score to lowest

        for (Pair<String, Integer> stringIntegerPair : orderedScoreList) {
            // For each score entry in the ordered score list, a text node is created and added to the scoreVbox
            Text nameAndScore = new Text(stringIntegerPair.getKey() + ": " + stringIntegerPair.getValue().toString());
            nameAndScore.getStyleClass().add("score");
            nameAndScore.setOpacity(0);
            scoreVbox.getChildren().add(nameAndScore);
        }

        for (int i = 0; i < scoreVbox.getChildren().size(); i++) {
            reveal(scoreVbox.getChildren().get(i), i * 300);
            // 300 milliseconds delay between each animation
        }

        return scoreVbox;
    }



    /**
     * Orders an array containing Pairs by the second element from highest to lowest
     * @param arrayToSort the array to be sorted
     * @return arrayToSort the sorted array list
     */


    public List<Pair<String,Integer>> orderByScore(List<Pair<String, Integer>> arrayToSort) {

        ArrayList<Pair<String,Integer>> sortedScores = new ArrayList<>();

        while (!arrayToSort.isEmpty()) {
            Pair<String,Integer> highestPair = arrayToSort.get(0);
            for(Pair<String,Integer> pair : arrayToSort) {
                if (pair.getValue() > highestPair.getValue()) {
                    highestPair = pair;
                }
            }
            sortedScores.add(highestPair);
            arrayToSort.remove(highestPair);
        }

        arrayToSort.addAll(sortedScores);

        return arrayToSort;
    }


    /**
     * An animation is applied to a node which slowly reveals its content
     * @param node the node to which the animation is applied on
     * @param  delay the length of the delay for the animation to play
     */
    public void reveal(Node node,int delay){
        FadeTransition ft = new FadeTransition(Duration.millis(1000), node);
        // Fade in duration: 1000 milliseconds
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(delay));
        // Apply delay before starting the animation
        ft.play();

    }
}
