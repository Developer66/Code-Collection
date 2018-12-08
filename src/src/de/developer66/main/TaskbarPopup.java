package de.developer66.main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/*
 * Diese Klasse generiert ein Taskbar Popup
 */
public class TaskbarPopup extends Application {

	/********** Attributdeklarationen START **********/

	// Label für Current Date
	@FXML
	Label date;

	// Label für MessageText
	@FXML
	Label message;

	// X Button
	@FXML
	Button closeButton;

	// Show Hyperlink
	@FXML
	private Hyperlink hyperlink_show;

	// Download Hyperlink
	@FXML
	private Hyperlink hyperlink_download;

	// Timeline für Transition
	Timeline main = new Timeline();

	/********** Attributdeklarationen ENDE **********/

	/*
	 * Konstruktor
	 * 
	 * Parameterliste: - Message Text - Boolean für visible Show Hyperlink -
	 * Eventhandler für Show Hyperlink - Boolean für visible Download Hyperlink -
	 * Eventhandler für Download Hyperlink
	 */
	public TaskbarPopup(String message, boolean showHyperlink_anzeigen, EventHandler<ActionEvent> anzeigenActionEvent,
			boolean showHyperlink_downloaden, EventHandler<ActionEvent> downloadenActionEvent) {
		// New Stage
		Stage stage = new Stage();
		// Starte Stage
		start(stage);

		// Setze Text auf Message Label
		this.message.setText(message);

		// Setze Hyperlink visible oder nicht
		hyperlink_show.setVisible(showHyperlink_anzeigen);
		// Setze Custom Eventhandler
		hyperlink_show.setOnAction(anzeigenActionEvent);

		// Setze Hyperlink visible oder nicht
		hyperlink_download.setVisible(showHyperlink_downloaden);
		// Setze Custom Eventhandler
		hyperlink_download.setOnAction(downloadenActionEvent);
	}

	// Start Methode
	@Override
	public void start(Stage primaryStage) {
		try {
			// FXML loader
			FXMLLoader loader = new FXMLLoader();
			// Set controller to this loader
			loader.setController(this);
			// Set path to .fxml
			loader.setLocation(getClass().getResource("/main/TaskbarPopup.fxml"));

			// Setzte Sprache
			Locale systemLocale = Locale.getDefault();
			// Name = bundle package + Länderkennung (Substring um nur die
			// ersten 2 anzuzeigen)
			String resourcename = "bundle."
					+ systemLocale.getCountry().substring(systemLocale.getCountry().length() - 2).toLowerCase();
			// Resource Bundle
			ResourceBundle resbun = ResourceBundle.getBundle(resourcename, systemLocale);
			loader.setResources(resbun);

			// Set parent to loader
			Parent root = (Parent) loader.load();
			// New Scene
			Scene scene = new Scene(root, 400, 150);
			// Set Scene
			primaryStage.setScene(scene);
			// Set Stage Risizable false
			primaryStage.setResizable(false);
			// Set undecorated
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			// Set Icon
			primaryStage.getIcons().add(new Image("icon.png"));
			// Set Fill
			scene.setFill(Color.TRANSPARENT);
			// Set Always on Top > true
			primaryStage.setAlwaysOnTop(true);

			// Get Date für Date Label
			Date date = new Date();
			// Format Date
			SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
			// Set date to Date Label
			this.date.setText(dt.format(date));

			// Set ActionEvent for Close Button
			closeButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					primaryStage.close();
				}
			});

			// Set Scene Transition Mouse Entered
			scene.addEventHandler(MouseEvent.MOUSE_ENTERED, evt -> {
				Timeline timeline = new Timeline();
				KeyFrame key = new KeyFrame(Duration.millis(200),
						new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 0.7));
				timeline.getKeyFrames().add(key);
				timeline.play();

				main.pause();
			});
			// Set Scene Transition Mouse Entered
			scene.addEventHandler(MouseEvent.MOUSE_EXITED, evt -> {
				Timeline timeline = new Timeline();
				KeyFrame key = new KeyFrame(Duration.millis(200),
						new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 1));
				timeline.getKeyFrames().add(key);
				timeline.play();

				main.playFromStart();
			});

			// Get Display Maße
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
			Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();

			// Get X & Y für Stage um sie rechts unten anzuzeigen
			double X = rect.getMaxX() - 400;
			double Y = rect.getMaxY() - 180;

			// Setze X & Y
			primaryStage.setX(X);
			primaryStage.setY(Y);

			// Show Stage
			primaryStage.show();

			// Set Timeline um Stage nach 5 Sec auszublenden
			main.getKeyFrames().add(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					// Wenn 5 Sec um dann PrimaryStage schließen
					primaryStage.close();
				}
			}));
			// Starte Timeline
			main.play();
		} catch (Exception e) {
			new Error(e.getMessage());
		}
	}
}
