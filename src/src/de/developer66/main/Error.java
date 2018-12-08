package de.developer66.main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
 * Ein Fenster mit der Errormeldung wir angezeigt
 * der Text in der TextArea wird mit einem
 * "Typing" Effekt angezeigt...
 */
public class Error extends Application {

	// Konstruktor mit Text als Parameter
	public Error(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// New Stage
				Stage stage = new Stage();

				// Setze Parametertext auf den Text
				StringMessage = message;

				// Starte Window in FX-Thread
				start(stage);
			}
		});
	}

	/********** Attribut Deklarationen START *********/

	// Message String mit Fehlertext
	String StringMessage = "Default Text";

	// Textarea
	@FXML
	private TextArea textarea_error;

	// Sorrytex >> Sorry das hätte nicht passieren dürfen
	@FXML
	private Label label_sorrytext;

	// Title >> Fehler aufgetretten
	@FXML
	private Label label_errorheader;

	/********** Attribut Deklarationen ENDE *********/

	@Override
	public void start(Stage primaryStage) {
		try {
			// FXML loader
			FXMLLoader loader = new FXMLLoader();
			// Set controller to this loader
			loader.setController(this);
			// Set path to .fxml
			loader.setLocation(getClass().getResource("/main/error.fxml"));
			// Set parent to loader
			Parent root = (Parent) loader.load();
			// New Scene
			Scene scene = new Scene(root, 600, 350);
			// Set Icon
			primaryStage.getIcons().add(new Image("error.png"));
			// Set title
			primaryStage.setTitle("Error");
			// Set always on top
			primaryStage.setAlwaysOnTop(true);
			// Set Scene
			primaryStage.setScene(scene);
			// Set Resize false
			primaryStage.setResizable(false);
			// Show Stage
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start "Typing" Effect
		final IntegerProperty i = new SimpleIntegerProperty(0);
		Timeline timeline = new Timeline();
		KeyFrame keyFrame = new KeyFrame(
				// Duration 50ms
				Duration.millis(50), event -> {
					if (i.get() > StringMessage.length()) {
						timeline.stop();
					} else {
						textarea_error.setText(StringMessage.substring(0, i.get()));
						i.set(i.get() + 1);
					}
				});
		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
		// Starte Timeline
		timeline.play();
	}
}
