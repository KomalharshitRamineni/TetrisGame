package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * Extends Canvas and is responsible for drawing itself.
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {

            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE

    };

    /**
     * GameBoard is the board this block belongs to
     */
    private final GameBoard gameBoard;

    /**
     * Used to check if there should be a circle displayed on the block
     */
    private Boolean displayCenterCircle;

    /**
     *  The width of the canvas to render
     */
    private final double width;

    /**
     *  The height of the canvas to render
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */

    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        displayCenterCircle = false;
        //Set the initial value of displayCenterCircle to false

        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);


    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.rgb(180, 78, 217,0.2));
        gc.fillRect(0,0, width, height);


        //Border
        gc.setStroke(Color.rgb(144, 0, 255,1));
        gc.strokeRect(0,0,width,height);

    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Color colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        double borderThickness = width/6;

        Color topColour = getOffsetColour(colour,false);
        Color leftColour = getOffsetColour(colour,true);
        Color rightColour = getOffsetColour(leftColour,true);
        Color bottomColour = getOffsetColour(rightColour,true);


        //Draws 4 triangles on each side of the canvas to achieve desired block design

        gc.setStroke(topColour);
        gc.strokePolygon(new double[]{0, width/2, width}, new double[]{0, height/2, 0}, 3);
        gc.setFill(topColour);
        gc.fillPolygon(new double[]{0, width/2, width}, new double[]{0, height/2, 0}, 3);

        gc.setStroke(leftColour);
        gc.strokePolygon(new double[]{0, width/2, 0}, new double[]{0, height/2, height}, 3);
        gc.setFill(leftColour);
        gc.fillPolygon(new double[]{0, width/2, 0}, new double[]{0, height/2, height}, 3);

        gc.setStroke(rightColour);
        gc.strokePolygon(new double[]{width, width/2, width}, new double[]{0, height/2, height}, 3);
        gc.setFill(rightColour);
        gc.fillPolygon(new double[]{width, width/2, width}, new double[]{0, height/2, height}, 3);

        gc.setStroke(bottomColour);
        gc.strokePolygon(new double[]{0, width/2, width}, new double[]{height, height/2, height}, 3);
        gc.setFill(bottomColour);
        gc.fillPolygon(new double[]{0, width/2, width}, new double[]{height, height/2, height}, 3);


        gc.setFill(colour);
        gc.fillRect(borderThickness, borderThickness, width-borderThickness*2, height-borderThickness*2);


        if (displayCenterCircle) {
            paintCircle();
        }

        //Border
        gc.setStroke(Color.rgb(144, 0, 255,1));
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing its colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Calculate a new colour based on the colour passed into it.
     * @param colour initial colour to calculate new colour with.
     * @param shade the boolean value determines how the new colour is changed, it either gets shaded or tinted
     * @return the new colour calculated
     */

    private Color getOffsetColour(Color colour,Boolean shade) {

        //The shade and tint factor determine how much lighter or darker the colour is change to
        double shadeFactor = 0.3;
        double tintFactor = 0.5;

        double currentR = colour.getRed() * 255;
        double currentG = colour.getGreen() * 255;
        double currentB = colour.getBlue() * 255;

        double newR;
        double newG;
        double newB;


        if (shade) {

            newR = currentR * (1 - shadeFactor);
            newG = currentG * (1 - shadeFactor);
            newB = currentB * (1 - shadeFactor);

        } else {

            newR = currentR + (255 - currentR) * tintFactor;
            newG = currentG + (255 - currentG) * tintFactor;
            newB = currentB + (255 - currentB) * tintFactor;

        }

        return Color.rgb((int) newR, (int) newG, (int) newB);

    }

    /**
     * Paints a circle on the canvas on top of the existing drawings
     */
    private void paintCircle() {

        var gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(255, 255, 255, 0.7));

        // Draw a translucent circle
        double centerX = width/2;
        double centerY = height/2;
        double radius = width/5;
        gc.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }
    /**
     * Sets the boolean attribute displayCenterCircle to true.
     */

    public void setCenterCircle() {
        displayCenterCircle = true;
    }

    /**
     * Draws on top of the existing canvas to achieve a hover effect.
     */
    public void setHover() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(255, 255, 255, 0.7));
        gc.fillRect(0,0,width,height);

    }

    /**
     * Removes the hover on the block by re-painting.
     */
    public void removeHover() {

        Color colourOfBlock = COLOURS[getValue()];
        if (getValue() == 0) {
            paintEmpty();
        } else {
            paintColor(colourOfBlock);
        }
    }

    /**
     * Used to apply an animation to the blocks which is played when they are cleared.
     */
    public void fadeOut() {

        var gc = getGraphicsContext2D();
        new AnimationTimer() {
            long startTime = -1;
            final long duration = 500_000_000L; // 0.5 secs
            @Override
            public void handle(long now) {
                if (startTime < 0) {
                    startTime = now;
                }

                // Calculate time elapsed since animation started
                long elapsedTime = now - startTime;

                // Calculate the progress of fading (0.0 to 1.0)
                double progress = (double) elapsedTime / duration;

                // Ensure that progress remains within the valid range [0.0, 1.0]
                progress = Math.max(0.0, Math.min(1.0, progress));

                // Clear the canvas and paint it empty
                gc.clearRect(0, 0, width, height);
                paintEmpty();

                // Draw a green rectangle with gradually decreasing opacity
                gc.setFill(Color.rgb(0, 255, 0, 1.0 - progress)); // Fade out gradually
                gc.fillRect(0, 0, width, height);

                // Check if the animation has finished and stop if so
                if (elapsedTime >= duration) {
                    stop();
                }
            }
        }.start();
    }
}
