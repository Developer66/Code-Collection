package de.developer66.main;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/*
 * Update Helper
 * 
 * Der Update Helper wird jeden Start aufgerufen
 * und schaut ob auf Github ein Update verfügbar
 * ist. Wenn ja wird ein Popup unten rechts angezeigt.
 */
public class UpdateHelper extends Application {

	/********** Attribut Deklarationen START **********/

	// DBHelper from MainView
	DBHelper dbhelper = MainView.getDBHelper();

	// Boolean für showNotAvailableAlert... Falls kein Update Verfügbar
	// ist kann man so ein Alert anzeigen lassen
	boolean showNotAvailableAlert = false;

	/********** Attribut Deklarationen ENDE **********/

	// Konstruktor
	public UpdateHelper(boolean showNotAvailableAlert) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// Erzeuge neue Stage
				Stage stage = new Stage();

				// Setze Parameter Variable auf Atributdeklarations Variable
				UpdateHelper.this.showNotAvailableAlert = showNotAvailableAlert;

				// Starte UpdateHelper
				try {
					start(stage);
				} catch (Exception e) {
					new Error(e.getMessage());
				}
			}
		});
	}

	// Start Methode
	@Override
	public void start(Stage primaryStage) throws Exception {
		// Erzeuge Thread um Aktvitäten in der MainView nicht zublocken
		new Thread() {
			@Override
			public void run() {
				// Get Json from Github Repository
				try {
					JSONObject json = readJsonFromUrl(
							"https://raw.githubusercontent.com/Developer66/Code-Collection/master/version.json");

					// Ausgeben in der Konsole der Json Version
					System.out.println(
							"Jar Version: " + MainView.getJarVersion() + " | Json Version: " + json.get("version"));

					// Check ob Versionen der Json der des Jar entspricht
					if (MainView.getJarVersion() == Integer.parseInt((String) json.get("version"))) {
						System.out.println("Kein Update Verfügbar");

						// Wenn gewünscht wird jetzt ein Alert angezeigt bei dem
						// dem User mitgeteilt wird, das es kein Update gibt
						if (showNotAvailableAlert == true) {
							// JavaFX Thread
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									// Alert "Kein Update Verfügbar"
									Alert alert = new Alert(AlertType.INFORMATION);
									alert.setContentText("Kein Update verfügbar");
									alert.show();
								}
							});
						}
					} else {
						System.out.println("Update Verfügbar");

						// JavaFX Thread
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								// EventHandler für den Hyperlink für die Anzeige des Changelogs im Internet
								EventHandler<ActionEvent> anzeigen = new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent event) {
										// Open Changelog im Browser
										openChangelog();
									}
								};
								// EventHandler für den Hyperlink für den Download
								EventHandler<ActionEvent> downloaden = new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent event) {
										// Download mit primaryStageals übergabe für den FilePicker
										download(primaryStage);
									}
								};

								/*
								 * Zeige das TaskbarPopup an.
								 * 
								 * Text: Update! Version: ** > ** AnzeigenHyperlink: true; DownloadenHyperlink:
								 * true
								 */
								new TaskbarPopup("Update! Version: " + MainView.getJarVersion() + " > "
										+ json.getString("version"), true, anzeigen, true, downloaden);
							}
						});
					}
				} catch (JSONException | IOException e) {
					System.out.println("Update fehlgeschlagen. Offline??");
				}
			}
		}.start();
	}

	/*
	 * Methode zum öffenen des Changelogs im Browser
	 */
	void openChangelog() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		// Versuche Changelog im Browser zuöffnen
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(URI
						.create("https://raw.githubusercontent.com/Developer66/Code-Collection/master/changelog.txt"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// Alert falls System anzeigen im Browser nicht unterstützt
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText(
					"Open in browser is not supported.\nPlease visit: https://raw.githubusercontent.com/Developer66/Code-Collection/master/changelog.txt");
			alert.show();
		}
	}

	/*
	 * Methode zum Download des neusten Jars
	 */
	public static <ProgressDialog> void download(Stage stage) {
		// New File Chooser
		FileChooser fc = new FileChooser();
		// Set Extensions
		fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Jar", "*.jar"),
				new FileChooser.ExtensionFilter("All", "*.*"));
		// Set default Filename to "CodeCollection.jar"
		fc.setInitialFileName("CodeCollection.jar");
		// Set User Directory to UserHome
		fc.setInitialDirectory(new File(System.getProperty("user.home")));

		// Get Localpath from User input
		File localPath = fc.showSaveDialog(stage);

		// If User has Selected File
		if (localPath != null) {
			// Task
			Task<Boolean> createMainScene = new Task<Boolean>() {
				@Override
				public Boolean call() {
					// Update Message
					updateMessage("Downloading...");

					BufferedInputStream in = null;
					FileOutputStream out = null;

					try {
						// Jar url
						URL url = new URL(
								"https://github.com/Developer66/Code-Collection/blob/master/CodeCollection.jar");
						URLConnection conn = url.openConnection();
						int size = conn.getContentLength();

						if (size < 0) {
							System.out.println("Could not get the file size");
							return false;
						} else {
							System.out.println("File size: " + size);

							in = new BufferedInputStream(url.openStream());
							out = new FileOutputStream(localPath);
							byte data[] = new byte[1024];
							int count;
							double sumCount = 0.0;

							while ((count = in.read(data, 0, 1024)) != -1) {
								out.write(data, 0, count);

								sumCount += count;
								if (size > 0) {
									updateMessage("Downloading... " + ((int) (sumCount / size * 100.0)) + "%");
									updateProgress((sumCount / size * 100.0), 100);
								}
							}
						}
					} catch (Exception e1) {
						new Error(e1.getMessage());
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e2) {
								new Error(e2.getMessage());
							}
						}
						if (out != null) {
							try {
								out.close();
							} catch (IOException e3) {
								new Error(e3.getMessage());
							}
						}
					}

					// Return Null
					return true;
				}
			};

			// ProgressBar
			ProgressBar pBar = new ProgressBar();
			// Load Value from Task
			pBar.progressProperty().bind(createMainScene.progressProperty());
			// Set PrefWidth
			pBar.setPrefWidth(250);
			// New Loading Label
			Label statusLabel = new Label();
			// Get Text
			statusLabel.textProperty().bind(createMainScene.messageProperty());
			// Set Style to Label
			statusLabel.setStyle("-fx-font: 24 arial;");

			// Layout
			VBox root = new VBox(statusLabel, pBar);

			// SetFill Width TRUE
			root.setFillWidth(true);
			// Center Items
			root.setAlignment(Pos.CENTER);

			// New Dialog
			Dialog<Void> dialog = new Dialog<>();
			// Set View to root
			dialog.getDialogPane().setContent(root);
			// Ad Cancel Button
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
			// Show Dialog
			dialog.show();

			// Display loaded FXML onSucceed
			createMainScene.setOnSucceeded(e -> {
				try {
					if (createMainScene.get() == false) {
						// Download Fehlgeschlagen
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setContentText(
								"Error occured... Could't download File.\nPlease visit:\n\thttps://developer66.github.io/Code-Collection/");
						alert.show();

						dialog.close();
					} else {
						System.out.println("Download Complete");
						dialog.close();

						/*
						 * Popup mit Anleitung zum Fortfahren nach dem Download
						 */
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setContentText(
								"Der Download ist abgeschlossen.\nSchließen Sie das aktuelle CodeCollection und löschen "
										+ "Sie die alte Datei. Nun benennen Sie die neue Datei in \"CodeCollection\" um und starten Sie es erneut.\n"
										+ "Das Update wird automatisch abgeschlossen...");
						alert.show();
					}
				} catch (Exception e1) {
					new Error(e1.getMessage());
				}
			});

			// Start Thread
			new Thread(createMainScene).start();
		} else {
			System.out.println("Update canceled");
		}
	}

	/*
	 * Methoden für Json aulsese
	 */
	String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
}