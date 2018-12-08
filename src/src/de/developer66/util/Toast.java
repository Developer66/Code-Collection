package de.developer66.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/*
 * Toast Klasse
 */
public class Toast {
	public static void makeText(String toastMsg, int toastDelay, int fadeInDelay, int fadeOutDelay) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage toastStage = new Stage();
				toastStage.setResizable(false);
				toastStage.initStyle(StageStyle.TRANSPARENT);

				toastStage.centerOnScreen();

				Text text = new Text(toastMsg);
				text.setFont(Font.font("Verdana", 25));
				text.setFill(Color.WHITE);

				StackPane root = new StackPane(text);
				root.setStyle(
						"-fx-background-radius: 20; -fx-background-color: rgba(101, 101, 101, 0.9); -fx-padding: 20px;");
				root.setOpacity(1);

				Scene scene = new Scene(root);
				scene.setFill(Color.TRANSPARENT);
				toastStage.setScene(scene);
				toastStage.show();

				Timeline fadeInTimeline = new Timeline();
				KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay),
						new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
				fadeInTimeline.getKeyFrames().add(fadeInKey1);
				fadeInTimeline.setOnFinished((ae) -> {
					new Thread(() -> {
						try {
							Thread.sleep(toastDelay);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Timeline fadeOutTimeline = new Timeline();
						KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay),
								new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
						fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
						fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
						fadeOutTimeline.play();
					}).start();
				});
				fadeInTimeline.play();
			}
		});
	}
}