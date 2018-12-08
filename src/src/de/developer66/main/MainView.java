package de.developer66.main;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import borderless.BorderlessScene;
import de.developer66.controller.MainViewController;
import de.developer66.helper.DBHelper;
import de.developer66.helper.UpdateHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

//MainView Class
public class MainView {

	static DBHelper dbHelper;

	public static void setDBHelper(DBHelper dbhelper) {
		dbHelper = dbhelper;
	}

	public static DBHelper getDBHelper() {
		return dbHelper;
	}

	static int jarVersion = 1;

	public static int getJarVersion() {
		return jarVersion;
	}

	// Konsturktor
	public MainView(Stage stage) {
		if (stage == null) {
			stage = new Stage();
		}

		// Ubuntu Font fix
		System.setProperty("prism.lcdtext", "false");
		System.setProperty("prism.text", "t2k");

		start(stage);
	}

	public MainView() {
	}

	double xOffset = 0;
	double yOffset = 0;

	// Controller
	static MainViewController mvc = null;

	public static MainViewController getMainViewConroller() {
		return mvc;
	}

	public static void setMainViewConroller(MainViewController controller) {
		mvc = controller;
	}

	// Start Methode
	public void start(Stage primaryStage) {
		try {
			// FXML loader
			FXMLLoader loader = new FXMLLoader();
			// Set path to .fxml
			loader.setLocation(getClass().getResource("/main/MainView.fxml"));
			// Set Stage size
			primaryStage.setHeight(800);
			primaryStage.setWidth(1200);
			// Add new Icon
			primaryStage.getIcons().add(new Image("icon.png"));

			// Set parent to loader
			Parent root = null;

			root = (Parent) loader.load();

			setMainViewConroller(loader.getController());

			root.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = primaryStage.getX() - event.getScreenX();
					yOffset = primaryStage.getY() - event.getScreenY();
				}
			});
			root.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					primaryStage.setX(event.getScreenX() + xOffset);
					primaryStage.setY(event.getScreenY() + yOffset);
				}
			});

			BorderlessScene scene = new BorderlessScene(primaryStage, root);
			scene.getStylesheets().add("/MetroStyle.css");

			primaryStage.initStyle(StageStyle.UNDECORATED);

			primaryStage.setScene(scene);
			primaryStage.show();

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					dbHelper.close();
					System.exit(0);
				}
			});

			MainViewController mvc = (MainViewController) loader.getController();

			Button maximize = mvc.getButtonMaximize();
			Button minmize = mvc.getButtoMinimize();
			Button close = mvc.getButtonClose();

			close.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					dbHelper.close();
					primaryStage.close();
					
					System.exit(0);
				}
			});
			minmize.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					primaryStage.setIconified(true);
				}
			});

			maximize.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (primaryStage.isMaximized()) {
						primaryStage.setMaximized(false);
					} else {
						primaryStage.setMaximized(true);
					}
				}
			});

			// Set ClickCount on MainVbox
			VBox windowVBox = getMainViewConroller().getVboxMain();
			windowVBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getClickCount() == 2) {
						if (primaryStage.isMaximized()) {
							primaryStage.setMaximized(false);
						} else {
							primaryStage.setMaximized(true);
						}
					}
				}
			});

			primaryStage.getScene().setOnDragDetected(new EventHandler<Event>() {
				@Override
				public void handle(Event event) {
					if (primaryStage.isMaximized()) {
						primaryStage.setMaximized(false);
					}
				}
			});

			// Timer Update Check
			Timer t_update = new Timer();

			t_update.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							new Thread(new Runnable() {
								@Override
								public void run() {
									new UpdateHelper(false);
								}
							}).start();
						}
					});
				}
			}, 10000, (long) Duration.hours(1).toMillis());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
