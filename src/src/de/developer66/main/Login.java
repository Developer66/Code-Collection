package de.developer66.main;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

import de.developer66.helper.DBHelper;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/*
 * Login Class
 * 
 * Prüft ob DB ein Passwd hat wenn ja,
 * werden Username und PAsswd vom
 * User abefragt. Wenn kein Passwordschutzt
 * festgelegtist wird die Mainansicht gestartet
 */
public class Login extends Application {

	/******* Attribut Deklarationen Start *******/

	// Connection zur DB
	Connection c = null;
	// Statement
	Statement stmt = null;

	/////// Login GUIs /////////

	// Textfield
	@FXML
	TextField textfield_benutzername;
	// PasswordField
	@FXML
	PasswordField textfield_passwort;

	// Button
	@FXML
	Button button_einloggen;

	// Error Label
	@FXML
	Label label_error_text;

	// Stage
	Stage stage;

	/******* Attribut Deklarationen Ende *******/

	// Konstruktor
	public Login() {
	}

	@Override
	public void start(Stage primaryStage) {
		/*
		 * Checke ob DB Passwort hat
		 * 
		 * Methode: Try datenbank mit default Anmeldedaten zustarten wenn success weiter
		 * wenn nicht Benutzerabrafge starte
		 */

		// Set primaryStage to Atributdeklaration stage
		stage = primaryStage;

		// Checke ob default Values funktionen
		try {
			// JDBC Treiber
			Class.forName("org.h2.Driver");

			// DBPath (Homepath of jarfile)
			File dbfile = new File(new DBHelper().getDBPath());

			// Baue Verbindung auf
			c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), "admin", "");

			// Erfolgreich Meldung
			System.out.println("Datenbank hat keinen PasswdSchutz\n");

			MainView.setDBHelper(new DBHelper("", ""));

			// Close Connection
			c.close();

			// Starte MainWindow
			startMainWindow();
		} catch (Exception e) {
			/*
			 * Datenbank hat Passwordschutz Jetzt wird Login Fenster angezeigt
			 */

			// True or False
			boolean isLocked = false;
			// Check if DB is locked
			try {
				RandomAccessFile fos = null;
				try {
					File file = new File(new DBHelper().getDBPath() + ".mv.db");
					if (file.exists()) {
						fos = new RandomAccessFile(file, "rw");
					}
					isLocked = true;
				} finally {
					if (fos != null) {
						fos.close();
					}
				}
			} catch (Exception e1) {
				e.printStackTrace();
			}

			if (isLocked == true) { // Prüfe ob DB file gelockted ist
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("CodeCollection");
				alert.setContentText("DB is locked. Maybe already in use?");
				alert.showAndWait();

				// Warten und anschließendes Löschen der Trace Datei
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				String dbpath = new DBHelper().getDBPath();
				File dbfile = new File(dbpath + ".trace.db");
				dbfile.delete();

				// Beende System
				System.exit(0);
			} else {
				try {
					/******** Sprache ********/
					// Anfangs Sprache wird auf System Sprache gesetzt
					// bis Sprache aus DB geladen werden kann
					Locale systemLocale = Locale.getDefault();
					// Name = bundle package + Länderkennung (Substring um nur die
					// ersten 2 anzuzeigen)
					String resourcename = "bundle."
							+ systemLocale.getCountry().substring(systemLocale.getCountry().length() - 2).toLowerCase();
					// Resource Bundle
					ResourceBundle resbun = ResourceBundle.getBundle(resourcename, systemLocale);

					/****** FXML File *******/
					// FXML loader
					FXMLLoader loader = new FXMLLoader();
					// Set Controller
					loader.setController(this);
					// Set Resource
					loader.setResources(resbun);
					// Set path to .fxml
					loader.setLocation(getClass().getResource("/main/Login.fxml"));
					// Set parent to loader
					Parent root = (Parent) loader.load();
					// Set Scene
					Scene scene = new Scene(root, 450, 330);
					// Stage Settings
					primaryStage.setResizable(false);
					primaryStage.setScene(scene);

					// Button können jetzt auch über die Tastatur bedient werden
					button_einloggen.defaultButtonProperty().bind(button_einloggen.focusedProperty());

					// Button OnClickListener
					button_einloggen.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							try {
								// JDBC Treiber
								Class.forName("org.h2.Driver");

								// DBPath (Homepath of jarfile)
								File dbfile = new File(new DBHelper().getDBPath());

								// Baue Verbindung auf
								c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(),
										textfield_benutzername.getText(), textfield_passwort.getText());

								// Erfolgreich Meldung
								System.out.println("DB erflogreich geöffnet\n");

								// Set DBHelper in MainView
								MainView.setDBHelper(
										new DBHelper(textfield_benutzername.getText(), textfield_passwort.getText()));

								// Starte MainWindow
								startMainWindow();

								c.close();
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("Passwort oder Username ist falsch...");
								label_error_text.setVisible(true);
							}
						}
					});

					// Set Password and Username onEnter
					EventHandler<KeyEvent> eh = new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent event) {
							// Checke ob Enter gedrüct wurde
							if (event.getCode() == KeyCode.ENTER) {
								button_einloggen.fire();
							}
						}
					};

					// Set Eventhendler to Fields
					textfield_benutzername.setOnKeyPressed(eh);
					textfield_passwort.setOnKeyPressed(eh);

					// Set Focus on Username
					textfield_benutzername.setFocusTraversable(true);

					// Show Stage
					primaryStage.show();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	// Wird nach der Installation aufgrufen um Nutzer Infos nicht 2x eingeben
	// zulassen
	public void silentLogin(String username, String passwd) {
		try {
			// DBPath (Homepath of jarfile)
			File dbfile = new File(new DBHelper().getDBPath());

			// JDBC Treiber
			Class.forName("org.h2.Driver");
			// Stelle Verbindung her
			if (username.length() == 0 || passwd.length() == 0) {
				c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), "admin", "");
				// Set DBHelper in MainView
				MainView.setDBHelper(new DBHelper("", ""));
			} else {
				c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), username, passwd);
				// Set DBHelper in MainView
				MainView.setDBHelper(new DBHelper(textfield_benutzername.getText(), textfield_passwort.getText()));
			}

			// Erfolgreich Meldung
			System.out.println("DB erflogreich geöffnet\n");

			// Starte MainWindow
			startMainWindow();
		} catch (Exception e) {
			System.out.println("Passwort oder Username ist falsch...");
		}
	}

	// Loading Main Window
	public void startMainWindow() {
		if (MainView.getDBHelper().getDBVersion() != null) {
			// Get Update Task wenn Update bevorsteht
			// Der Task wird automatisch bei jedem Start aufgerufen
			// Falls ein Update vorliegt unterzieht er das Update
			// Wenn nicht wird die MainView gestartet
			MainView.getDBHelper().onUpdate(stage);
		} else {
			MainView.getDBHelper().changeSetting("version_nr", "" + MainView.getJarVersion());
			MainView.getDBHelper().onUpdate(stage);
		}
	}
}
