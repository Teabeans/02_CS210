/* 
 * CS210 Assignment NULL (Not an assignment)
 * Tim Lum - twhlum@gmail.com
 * Student ID: ### - ### - ###
 * 2017.05.30 (YYYY.MM.DD) - Spring quarter
 *
 * This software is published under the GNU general public license
 * https://www.gnu.org/licenses/gpl.txt
 * https://en.wikipedia.org/wiki/GNU_General_Public_License
 *
 * This program simulates a Spirograph and is meant to provide examples of basic JavaFX functionalities
 */

import java.awt.*; // Abstract Window Toolkit, for graphics tools
import java.util.*; // For random and scanner class
import java.text.Format;
import java.text.DecimalFormat;
import java.text.NumberFormat;
//import java.time.Duration;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration; 
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Spirograph extends Application {

	static final int PANEL_WIDTH = 800; // Maximum value set by user video card, could be higher or lower, oddity of JavaFX. Recommend 800 max
	static final int PANEL_HEIGHT = 600; // Maximum value set by user video card, could be higher or lower, oddity of JavaFX. Recommend 600 max

// Speed and time variables
	static double time = 0.0; // Used to store the current "time" (based off hardcoded 60 fps. Time increments to 1/60th of the current frame count)
	static final double FRAMERATE = 60; // frames per second (TODO: Tether framerate to actual refresh rate of animation cycle)
	static int    frame = 0; // Stores the current frame (used to calculate overall "time")
	static double planetaryAngularVelocity = 1; // Adjust speed here, angular velocity around the circle in radians per second

// Size and scale variables
	static double annularRadius = 275;			// Recommend 275 for default scale
	static int    annularToothCount = 60; 		// Number of teeth on the large ring, treated as static by program execution, but may be subject to change in the future.
	static int    planetaryToothCount = 20; 	// Default start number of teeth on the planetary gear
	static double planetaryRadius = (annularRadius/annularToothCount)*planetaryToothCount; // Calculates radius (to be used in equations) based on tooth count


// Spirograph mark variables
	static final double MAX_BRUSH_DIAMETER = 10; // Maximum diameter that brush can reach (at 100% velocity)
	static double pen1Radius = 50; // Distance from center of planetary gear to pen location
	static double pen2Radius = pen1Radius*2;
	static double pen3Radius = pen1Radius*3;
	static boolean draw1 = false; // If false, no pen exists (draw1, uniquely, is overidden by the checkbox settings at program initialization)
	static boolean draw2 = false; // If false, no pen exists
	static boolean draw3 = false; // If false, no pen exists
// This defines the color variable "spiroColor"
	static Color spiroColor = Color.rgb(255, 122, 0); // R, G, B, default to orange, but later overriden by velocityColor.
	static double velocity = 		0; // Distance between last pen mark and current pen mark
	static double velocityMax = 	0; // Holds the maximum observed velocity since last refresh
	static double previousPenX =	0; // Used to store location of pen's X value on the last frame
	static double previousPenY =	0; // Used to store location of pen's Y value on the last frame
	static double deltaX = 			0; // Difference between current pen's X position and last frame's X position
	static double deltaXMax = 		0; // Highest observed value of deltaX since last refresh
	static double deltaY = 			0; // Difference between current pen's Y position and last frame's Y position
	static double deltaYMax = 		0; // Highest observed value of deltaX since last refresh

// Used to detect what keys are pressed
	static HashSet<String> currentlyActiveKeys; // For storing what keys are being pressed. A set is required to avoid duplication.

// Set background RGB values here (0-255 inclusive corresponds to 0 to 100% respectively. Exceeding 255 will throw an exception)
	static int bgRed = 		15;
	static int bgGreen = 	15;
	static int bgBlue = 	15;
// This defines the color variable "backgroundColor" based on the above inputs
	static Color backgroundColor = Color.rgb(bgRed, bgGreen, bgBlue); // R, G, B

// Set the major and minor axes intervals here. Sizes are in pixels.
	static int axesMajorInterval = 100;
	static int axesMinorInterval = 10;

// Set axes RGB values here (0-255 inclusive corresponds to 0 to 100% respectively. Exceeding 255 will throw an exception)
	static int originAxesRed = 255;
	static int originAxesGreen = 255;
	static int originAxesBlue = 255;

// Set axes transparency values for origin, major, and minor subdivisions here (0 to 100 inclusive corresponds to percent opacity
// NOT percent transparency. Going out of range will throw an exception)
	static int axesOriginOpacity = 50;
	static int axesMajorOpacity = 25;
	static int axesMinorOpacity = 5;

// Calculates and defines the origin axes actual color based on background color, line color, and line transparency.
	static Color originAxes = Color.rgb(
// background color moves toward foreground color by fraction of opacity
// (0% opacity = no movement towards foreground, 100% opacity means complete movement towards foreground)
		(bgRed + ((originAxesRed-bgRed)*(axesOriginOpacity))/100),
		(bgGreen + ((originAxesGreen-bgGreen)*(axesOriginOpacity))/100),
		(bgBlue + ((originAxesBlue-bgBlue)*(axesOriginOpacity))/100)
		);

// Calculates and defines the major axes subdivision actual color based on background color, line color, and line transparency.
	static Color majorAxes = Color.rgb(
// background color moves toward foreground color by fraction of opacity
// (0% opacity = no movement towards foreground, 100% opacity means complete movement towards foreground)
		(bgRed + ((originAxesRed-bgRed)*(axesMajorOpacity))/100),
		(bgGreen + ((originAxesGreen-bgGreen)*(axesMajorOpacity))/100),
		(bgBlue + ((originAxesBlue-bgBlue)*(axesMajorOpacity))/100)
		);

// Calculates and defines the minor axes actual color based on background color, line color, and line transparency.
	static Color minorAxes = Color.rgb(
// background color moves toward foreground color by fraction of opacity
// (0% opacity = no movement towards foreground, 100% opacity means complete movement towards foreground)
		(bgRed + ((originAxesRed-bgRed)*(axesMinorOpacity))/100),
		(bgGreen + ((originAxesGreen-bgGreen)*(axesMinorOpacity))/100),
		(bgBlue + ((originAxesBlue-bgBlue)*(axesMinorOpacity))/100)
		);


	public static void main(String[] args) 
	{
		launch(args);
	}

	@Override
	public void start(Stage theStage) 
	{
		theStage.setTitle( "Spirograph" );

		Group root = new Group();
		Scene theScene = new Scene( root );
		theStage.setScene( theScene );

		Group UI = new Group();
		
		Canvas background = new Canvas( PANEL_WIDTH, PANEL_HEIGHT );
		Canvas canvas = new Canvas( PANEL_WIDTH, PANEL_HEIGHT );
		Canvas spirograph = new Canvas( PANEL_WIDTH, PANEL_HEIGHT );
		Canvas HUD = new Canvas( PANEL_WIDTH, PANEL_HEIGHT );

		root.getChildren().add( background );
		root.getChildren().add( spirograph );
		root.getChildren().add( canvas );
		root.getChildren().add( UI );
		UI.getChildren().add( HUD );

		GraphicsContext gcbg = background.getGraphicsContext2D();
		GraphicsContext gcspiro = spirograph.getGraphicsContext2D();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		GraphicsContext gcHUD = HUD.getGraphicsContext2D();

		Image bgImage = new Image( "Assets/BackgroundImage.jpg" );

// Draw background elements
		drawBackground(gcbg, bgImage);
		drawYAxis(gcbg);
		drawXAxis(gcbg);
		drawCircleCentered(gcbg, cartX(0), cartY(0), (int)annularRadius, Color.GRAY); // Draws the Spirograph outline
		drawBigTeeth(gcbg, annularToothCount);
//		drawVelocityLine(gcHUD);



		root.getChildren().add( sliderPlanetaryTeeth(gcspiro) );
		root.getChildren().add( sliderPen1Radius(gcspiro) );
		UI.getChildren().add( checkbox1(gcspiro) );
		UI.getChildren().add( checkbox2(gcspiro) );
		UI.getChildren().add( checkbox3(gcspiro) );

		prepareActionHandlers(theScene);





// Draw refreshing elements FRAMERATE per second
		new AnimationTimer() {
			public void handle(long currentNanoTime) {
// Timer background refresh
				gc.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
				frame++;
				time = frame/FRAMERATE; 
// 2(pi) seconds per annularToothCount
				double angleOfRotation = (time * planetaryAngularVelocity);

// Calculates the location of the pen 1, 2, 3 points
				double radiusDelta = annularRadius-planetaryRadius;
				double pen1X = cartX(0) + (radiusDelta*Math.cos(angleOfRotation)) + pen1Radius*(Math.cos((radiusDelta/planetaryRadius)*angleOfRotation));
				double pen1Y = cartY(0) + (radiusDelta*Math.sin(angleOfRotation)) - pen1Radius*(Math.sin((radiusDelta/planetaryRadius)*angleOfRotation));
				double pen2X = cartX(0) + (radiusDelta*Math.cos(angleOfRotation)) + pen2Radius*(Math.cos((radiusDelta/planetaryRadius)*angleOfRotation));
				double pen2Y = cartY(0) + (radiusDelta*Math.sin(angleOfRotation)) - pen2Radius*(Math.sin((radiusDelta/planetaryRadius)*angleOfRotation));
				double pen3X = cartX(0) + (radiusDelta*Math.cos(angleOfRotation)) + pen3Radius*(Math.cos((radiusDelta/planetaryRadius)*angleOfRotation));
				double pen3Y = cartY(0) + (radiusDelta*Math.sin(angleOfRotation)) - pen3Radius*(Math.sin((radiusDelta/planetaryRadius)*angleOfRotation));

// Set the velocity and prior point variables
				deltaX = Math.abs(pen1X - previousPenX);
				if (deltaX > deltaXMax) {
					deltaXMax = deltaX;
				}
				double currentDeltaXPercent = calculateDeltaXPercent();
				
				deltaY = Math.abs(pen1Y - previousPenY);
				if (deltaY > deltaYMax) {
					deltaYMax = deltaY;
				}
				double currentDeltaYPercent = calculateDeltaYPercent();
				
				velocity = Math.sqrt((deltaX * deltaX)+(deltaY*deltaY));
				if (velocity > velocityMax) {
					velocityMax = velocity;
				}
				double currentVelocityPercent = calculateVelocityPercent();
				
				Color velocityPenColor = calculateVelocityColor(currentDeltaXPercent, currentDeltaYPercent, currentVelocityPercent);
				
// Set the previous pen coordinates to prepare for application in the next calculation
				previousPenX = pen1X;
				previousPenY = pen1Y;

// Position of small circle running its circuit in the big circle
				double smallCircX = cartX(Math.cos(angleOfRotation)*(annularRadius-planetaryRadius));
				double smallCircY = cartY(-Math.sin(angleOfRotation)*(annularRadius-planetaryRadius));
				drawCircleCentered(gc, smallCircX, smallCircY, planetaryRadius, Color.GRAY);
				drawCircleCentered(gc, smallCircX, smallCircY, 5, Color.GRAY);

// Pen radius line (vector)
				gc.strokeLine(smallCircX, smallCircY, pen1X, pen1Y);
				drawSmallTeeth(gc, smallCircX, smallCircY, angleOfRotation);
				double thetaOfPenVector = -Math.atan2((smallCircY-pen1Y), (smallCircX-pen1X)); // Figures out the angle of the pen vector

// Spirograph pen mark
				if (draw1 == true) {
					fillCircleCentered(gcspiro, pen1X, pen1Y, (currentVelocityPercent*MAX_BRUSH_DIAMETER));
					drawCircleCentered(gcspiro, pen1X, pen1Y, (currentVelocityPercent*MAX_BRUSH_DIAMETER)-1, velocityPenColor); //writes to gcspiro, so it does not get cleared, can be cleared independent of everything else
				}
// TODO: Current velocity percent is different if pen 2 and pen 3 are activated. Need different percents calculated for each brush.
				if (draw2 == true) {
					fillCircleCentered(gcspiro, pen2X, pen2Y, (currentVelocityPercent*MAX_BRUSH_DIAMETER));
					drawCircleCentered(gcspiro, pen2X, pen2Y, (currentVelocityPercent*MAX_BRUSH_DIAMETER)-1, velocityPenColor); //writes to gcspiro, so it does not get cleared, can be cleared independent of everything else
				}
				if (draw3 == true) {
					fillCircleCentered(gcspiro, pen3X, pen3Y, (currentVelocityPercent*MAX_BRUSH_DIAMETER));
					drawCircleCentered(gcspiro, pen3X, pen3Y, (currentVelocityPercent*MAX_BRUSH_DIAMETER)-1, velocityPenColor); //writes to gcspiro, so it does not get cleared, can be cleared independent of everything else
				}

				drawDebugText(gc, smallCircX, smallCircY, pen1X, pen1Y, angleOfRotation, thetaOfPenVector);

				gc.setLineWidth(1);
				gc.setStroke(Color.WHITE);

// Clear the canvas on spacebar press
				clearOnButtonPress(gcspiro);
			}
		}.start();
		theStage.show();
	} // Closing start



// Method to adjust a color based on velocities
	static Color calculateVelocityColor(double deltaXPercent, double deltaYPercent, double velocityPercent) {
		int R = (int)(deltaXPercent*255); // Red for faster horizontal movement
		int G = 0;
		int B = (int)(deltaYPercent*255); // Blue for faster vertical movement
		Color velocityColor = Color.rgb(R, G, B);
		return (velocityColor);
	}

	static double calculateDeltaXPercent() {
		return(deltaX/deltaXMax);
	}

	static double calculateDeltaYPercent() {
		return(deltaY/deltaYMax); // Note: Do not need to find minimum deltaY since it has to be 0.
	}

	static double calculateVelocityPercent() {
		return((velocity) / (velocityMax));
	}

	static void drawSmallTeeth(GraphicsContext gc, double smallCircX, double smallCircY, double angleOfRotation) {
		gc.setLineWidth(1);
		gc.setStroke(Color.WHITE);
		double radiusDelta = annularRadius - planetaryRadius;
		for (int i = 0 ; i < planetaryToothCount ; i++) {
			double radiansPerTooth = (2*Math.PI)/planetaryToothCount; // Calculates how many radians each tooth occupies on the small gear
			double pen1X = cartX(0) + (radiusDelta*Math.cos(angleOfRotation))					// Moves the small gear around inside the big gear
						  + planetaryRadius*(Math.cos((radiusDelta/planetaryRadius)*angleOfRotation	// Adds the rotational translation around the center of the small gear
						  + (radiansPerTooth*i)));												// Displaces each tooth on the small gear by the arc per tooth times the iteration
			double pen1Y = cartY(0) + (radiusDelta*Math.sin(angleOfRotation))					// Moves the small gear around inside the big gear
						  - planetaryRadius*(Math.sin((radiusDelta/planetaryRadius)*angleOfRotation	// Adds the rotational translation around the center of the small gear
						  + (radiansPerTooth*i)));												// Displaces each tooth on the small gear by the arc per tooth times the iteration
			drawCircleCentered(gc, pen1X, pen1Y, 3, Color.GRAY);
		}
	}

	static void drawDebugText(GraphicsContext gc, double smallCircX, double smallCircY, double pen1X, double pen1Y, double angleBig, double thetaOfPenVector) {
		String coordinatePattern = "+###,000.000; -#";
		String angularPattern = "0.000";
		String temporalPattern = "###,000.0";
		DecimalFormat coordinateFormat = new DecimalFormat(coordinatePattern);
		DecimalFormat angularFormat = new DecimalFormat(angularPattern);
		DecimalFormat temporalFormat = new DecimalFormat(temporalPattern);

		Font theFont = Font.font( "Arial", FontWeight.BOLD, 12 );
		gc.setFont( theFont );
		gc.setFill( Color.WHITE );
		gc.fillText( ("Time (frames / 60): " + temporalFormat.format(time)),					10, 20);
		gc.fillText( ("Angular speed (rad/sec): " + planetaryAngularVelocity),						10, 35); // Speed
		gc.fillText( ("Annular tooth count: " + annularToothCount),									10, 50); // Teeth big (Radius)
		gc.fillText( ("Planetary tooth count: " + planetaryToothCount),							10, 65);// Teeth small (Radius)
		gc.fillText( ("Gear X: " + coordinateFormat.format(smallCircX-(PANEL_WIDTH/2))),		10, 80); // PenX
		gc.fillText( ("Gear Y: " + coordinateFormat.format(-smallCircY+(PANEL_HEIGHT/2))),		10, 95); // PenY
		gc.fillText( ("Pen X: " + coordinateFormat.format(pen1X-PANEL_WIDTH/2)),					10, 110); // Pen's X position
		gc.fillText( ("Pen Y: " + coordinateFormat.format(-pen1Y+(PANEL_HEIGHT/2))),				10, 125); // Pen's Y position
		gc.fillText( ("Angle Big Gear: " + angularFormat.format(angleBig%(2*Math.PI))),			10, 140); // Position of the large gear
		gc.fillText( ("Angle Small Gear: " + angularFormat.format((thetaOfPenVector+Math.PI))),	10, 155);
	}

// Draws a clearing rectangle across gcSpiro on a key press event
	private static void clearOnButtonPress(GraphicsContext gcspiro) {
		if (currentlyActiveKeys.contains("C")) {
			gcspiro.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
			deltaXMax = 0;
			deltaYMax = 0;
			velocityMax = 0;
		}
		else {
		}
	}

	private static void prepareActionHandlers(Scene theScene) {
		currentlyActiveKeys = new HashSet<String>(); // use a set so duplicates are not possible
		theScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				currentlyActiveKeys.add(event.getCode().toString());
			}
		});
		theScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				currentlyActiveKeys.remove(event.getCode().toString());
			}
		});
	}
	
	static void clearCanvas(GraphicsContext gcspiro) {
//	On spacebar press
//	clear rect
		gcspiro.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
	}
	
	
	static CheckBox checkbox1(GraphicsContext gcspiro) {
		CheckBox checkBox1 = new CheckBox("Pen 1");
		checkBox1.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				draw1 = !draw1; // Toggles the boolean value of draw1.
				gcspiro.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
			}
		});
		checkBox1.setSelected(true);
		checkBox1.setLayoutX(25);
		checkBox1.setLayoutY(500);
		return(checkBox1);
	}
	
	static CheckBox checkbox2(GraphicsContext gcspiro) {
		CheckBox checkBox2 = new CheckBox("Pen 2");
		checkBox2.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				draw2 = !draw2; // Toggles the boolean value of draw1.
				gcspiro.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
			}
		});
		checkBox2.setSelected(false);
		checkBox2.setLayoutX(25);
		checkBox2.setLayoutY(525);
		return(checkBox2);
	}
	
	static CheckBox checkbox3(GraphicsContext gcspiro) {
		CheckBox checkBox3 = new CheckBox("Pen 3");
		checkBox3.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				draw3 = !draw3; // Toggles the boolean value of draw1.
				gcspiro.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
			}
		});
		checkBox3.setSelected(false);
		checkBox3.setLayoutX(25);
		checkBox3.setLayoutY(550);
		return(checkBox3);
	}
	
	
	static Slider sliderPlanetaryTeeth(GraphicsContext gc) {
		Slider sliderPlanetaryTeeth = new Slider(10, 60, 20); // (Min, Max, Default)
		sliderPlanetaryTeeth.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				planetaryToothCount = new_val.intValue();
				planetaryRadius = (annularRadius/annularToothCount)*planetaryToothCount; // Updates
				gc.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
				deltaXMax = 0;
				deltaYMax = 0;
				velocityMax = 0;
			}
		});
		sliderPlanetaryTeeth.setOrientation(javafx.geometry.Orientation.VERTICAL);
		sliderPlanetaryTeeth.setLayoutX(PANEL_WIDTH-50);

		sliderPlanetaryTeeth.setLayoutY(50);
		sliderPlanetaryTeeth.setSnapToTicks(true);
		sliderPlanetaryTeeth.setShowTickLabels(true);
		sliderPlanetaryTeeth.setShowTickMarks(true);
		sliderPlanetaryTeeth.setMajorTickUnit(10);
		sliderPlanetaryTeeth.setMinorTickCount(2);
		sliderPlanetaryTeeth.setBlockIncrement(20);
		sliderPlanetaryTeeth.setMinHeight(PANEL_HEIGHT-100);
		return(sliderPlanetaryTeeth);
	}
	
	static Slider sliderPen1Radius(GraphicsContext gc) {
		Slider sliderPen1Radius = new Slider(0, 150, 40); // (Min, Max, Default)
		sliderPen1Radius.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				pen1Radius = new_val.intValue();
				pen2Radius = pen1Radius*2;
				pen3Radius = pen1Radius*3;
//				planetaryRadius = (annularRadius/annularToothCount)*planetaryToothCount; // Updates
				gc.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT); // x, y, w, h
				deltaXMax = 0;
				deltaYMax = 0;
				velocityMax = 0;
			}
		});
		sliderPen1Radius.setOrientation(javafx.geometry.Orientation.VERTICAL);
		sliderPen1Radius.setLayoutX(PANEL_WIDTH-100);

		sliderPen1Radius.setLayoutY(50);
		sliderPen1Radius.setSnapToTicks(true);
		sliderPen1Radius.setShowTickLabels(true);
		sliderPen1Radius.setShowTickMarks(true);
		sliderPen1Radius.setMajorTickUnit(10);
		sliderPen1Radius.setMinorTickCount(1);
		sliderPen1Radius.setBlockIncrement(20);
		sliderPen1Radius.setMinHeight(PANEL_HEIGHT-100);
		return(sliderPen1Radius);
	}


	static void drawBackground (GraphicsContext gc, Image bgImage) {
		gc.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		gc.drawImage( bgImage, 0, 0 );
		gc.setGlobalAlpha(0.98); // Sets the "brush" alpha value
		gc.setFill(backgroundColor);
		gc.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		gc.setGlobalAlpha(1.0); // Resets the "brush" alpha value
	}

// Function to draw x axis and associated horizontal gridlines
	static void drawXAxis(GraphicsContext gc) {
// Draw major horizontal lines using the majorAxes color
		for (int i = 0 ; i <= PANEL_WIDTH/axesMajorInterval ; i++) {
			gc.setStroke(majorAxes);
			drawHorizontalLine(gc, cartY(i*axesMajorInterval));
			drawHorizontalLine(gc, cartY(-i*axesMajorInterval));

// Draw minor horizontal lines using the minorAxes color
			for (int j = 0 ; j <= (axesMajorInterval/axesMinorInterval) ; j++) {
				gc.setStroke(minorAxes);
				drawHorizontalLine(gc, (i*axesMajorInterval)+(j*axesMinorInterval));
			}
		
// Draws the axes labels along the Y axis.
			gc.setStroke(originAxes);
			Font theFont = Font.font( "Arial", FontWeight.BOLD, 12 );
			gc.setFont( theFont );
			gc.setFill( Color.WHITE );
			gc.fillText(""+((i*axesMajorInterval)-PANEL_WIDTH/2), (i*axesMajorInterval)+5, (PANEL_HEIGHT/2)-5); // (String, x, y)
		}
// Draw the origin line at height = 0
			gc.setStroke(originAxes);
			drawHorizontalLine(gc, 0);
	} // Closing drawXAxis

// Function to draw y axis and associated vertical gridlines
	static void drawYAxis(GraphicsContext gc) {
// Draw major vertical lines using the majorAxes color
		for (int i = 0 ; i <= (PANEL_HEIGHT/axesMajorInterval)+1 ; i++) {
			gc.setStroke(majorAxes);
			drawVerticalLine(gc, cartX(i*axesMajorInterval)); // Draws vert lines to right of origin
			drawVerticalLine(gc, cartX(-i*axesMajorInterval)); // Draws vert lines to left of origin
// Draw minor vertical lines using the minorAxes color
			for (int j = 0 ; j <= (axesMajorInterval/axesMinorInterval) ; j++) {
				gc.setStroke(minorAxes);
				drawVerticalLine(gc, (i*axesMajorInterval)+(j*axesMinorInterval));
			}
// Draw axes labels
			Font theFont = Font.font( "Arial", FontWeight.BOLD, 12 );
			gc.setFont( theFont );
			gc.setFill( Color.WHITE );
			gc.fillText(""+(-i*axesMajorInterval+(PANEL_HEIGHT/2)), (PANEL_WIDTH/2)+5, (i*axesMajorInterval)-5); // (String, X, Y)
		}
// Draw the origin line at height = 0
		gc.setStroke(originAxes);
		drawVerticalLine(gc, 0);
	}

// Function that draws a horizontal line from one end of the canvas to the other.
	static void drawHorizontalLine(GraphicsContext gc, double yCoord) {
		gc.setLineWidth(1);
		gc.strokeLine(0, yCoord, PANEL_WIDTH, yCoord);
	}

// Function that draws a vertical line from one end of the canvas to the other.
	static void drawVerticalLine(GraphicsContext gc, double xCoord) {
		gc.setLineWidth(1);
		gc.strokeLine(xCoord, 0, xCoord, PANEL_HEIGHT);
	}

// Function that draws the Spirograph outline
	static void drawBigTeeth(GraphicsContext gc, int annularToothCount) {
		gc.setLineWidth(1);
		gc.setStroke(Color.WHITE);
		for (int i = 0 ; i < annularToothCount ; i++) {
			drawCircleCentered(gc, cartX(annularRadius*-Math.cos(i*(2*Math.PI)/annularToothCount)), cartY(annularRadius*Math.sin(i*(2*Math.PI)/annularToothCount)), 3.0, Color.GRAY );
		}
	}
	
	static void fillCircleCentered(GraphicsContext gc, double x, double y, double radius) {
		gc.setLineWidth(2);
		gc.setStroke(backgroundColor);
		gc.strokeOval((x - radius), (y - radius), (2 * radius), (2 * radius));
	}

	static void drawCircleCentered(GraphicsContext gc, double x, double y, double radius, Color color) {
		gc.setLineWidth(1);
		gc.setStroke(color);
		gc.strokeOval((x - radius), (y - radius), (2 * radius), (2 * radius));
	}

	static double cartX(double x) { // if 0, return 1/2 panel width
		return(PANEL_WIDTH/2 + x);
	}

	static double cartY(double y) { // if 0, return 1/2 panel height, positive counts UP.
		return((PANEL_HEIGHT/2) - y);
	}

} // Closing class