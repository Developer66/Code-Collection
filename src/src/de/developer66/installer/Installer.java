package de.developer66.installer;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import de.developer66.main.Login;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Installer extends Application {

	/*
	 * Infos zum Installer Aufbau Installer > Sprache > Database > User > Start
	 */

	/******* Attribut Deklarationen Start *******/

	// Speicher L�nderkennung bis es in der Datenbank gespeichert wurde
	public Locale locale;

	/******* Attribut Deklarationen Ende *******/

	// Konstruktor
	public Installer() {
		// New Stage
		Stage stage = new Stage();

		// Starte Applciation
		start(stage);
	}

	// Start Methode
	@Override
	public void start(Stage primaryStage) {
		// Willkommens Message :)
		System.out.println("Starte Installer.");

		// Rufe die Sprachauswahl auf
		new ChooseLanguage(primaryStage);

		// Show Stage
		primaryStage.show();

		// Set Stage Title
		primaryStage.setTitle("Installations Wizard");

		// Set Stage Icon
		primaryStage.getIcons().add(new Image("/icon.png"));
	}

	/*
	 * chooseLanguage Class
	 * 
	 * Legt die SystemSprache fest. Aktuell Deutsch oder Englisch mehr Sprachen
	 * folden sp�ter
	 * 
	 */
	public class ChooseLanguage {

		/******* Attribut Deklarationen Start *******/

		// Combobox f�r Sprachen
		@FXML
		ComboBox<String> combobox_sprache;

		/**** Labels ****/
		// Title
		@FXML
		Label label_title;
		// Main Text
		@FXML
		Label label_maintext;

		/**** Buttons ****/
		// CancelButton
		@FXML
		Button button_exit;
		// Button OK
		@FXML
		Button button_ok;

		/******* Attribut Deklarationen Ende *******/

		// Sprach Update Methode
		void updateLocale(Locale locale) {
			try {
				ResourceBundle resbun = ResourceBundle.getBundle(
						"bundle." + locale.getCountry().substring(locale.getCountry().length() - 2).toLowerCase(),
						locale);
				// Update GUI
				label_title.setText(resbun.getString("Label.title"));
				label_maintext.setText(resbun.getString("Label.mainSprachtext"));
				button_exit.setText(resbun.getString("Button.cancel"));
				button_ok.setText(resbun.getString("Button.continue"));
				// Erfolg Meldung
				System.out.println("\tGUI Updated");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		// Stage
		Stage stage;

		// Konstruktor
		ChooseLanguage(Stage stage) {
			// Set stage
			this.stage = stage;
			try {
				/******** Sprache ********/
				// Anfangs Sprache wird auf System Sprache gesetzt
				// bis User neue Ausw�hlt
				Locale systemLocale = Locale.getDefault();
				// Name = bundle package + L�nderkennung (Substring um nur die
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
				loader.setLocation(getClass().getResource("/installer/SpracheAuswahl.fxml"));
				// Set parent to loader
				Parent root = (Parent) loader.load();
				// Set Scene
				Scene scene = new Scene(root, 590, 330);
				// Stage Settings
				stage.setResizable(false);
				stage.setScene(scene);

				/****** Combobox *******/
				// Erzeugen von Combobox Eintr�gen
				combobox_sprache.getItems().addAll("Deutsch", "English");
				// ChangeListener f�r Combobox
				combobox_sprache.valueProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String lastState,
							String newState) {
						System.out.println("\tSprache wurde ge�ndert.");

						// New Locale
						Locale newlocale = null;
						// Switchanweisung um neue Sprache festzustellen
						switch (newState) {
						case "Deutsch":
							// Setze neue Sprache auf Deutsch
							newlocale = new Locale("de", "DE");
							break;
						case "English":
							// Setze neue Sprache auf English
							newlocale = new Locale("en", "EN");
							break;
						}

						// Setze public Local Variable auf neue Sprache um
						// sie in den anderen Fenstern anzuwenden
						locale = newlocale;

						// Update GUI zu neuer Sprache
						updateLocale(newlocale);
					}
				});
				// Selektieren des Eintrag der zur Systemsprache passt
				// Die Ersten 2 Zeichen (GRO�GESCHRIEBEN)
				switch (systemLocale.getCountry().substring(systemLocale.getCountry().length() - 2)) {
				case "DE":
					combobox_sprache.getSelectionModel().select(0);
					break;
				case "EN":
					combobox_sprache.getSelectionModel().select(1);
					break;
				default:
					combobox_sprache.getSelectionModel().select(0);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Button k�nnen jetzt auch �ber die Tastatur bedient werden
			button_ok.defaultButtonProperty().bind(button_ok.focusedProperty());
			button_exit.defaultButtonProperty().bind(button_exit.focusedProperty());
		}

		/****** Button OnAction Methoden Start *******/

		// Weiter Button
		@FXML
		void buttonContinue() {
			new ChooseDatabase(stage);
		}

		// Abbruch Button
		@FXML
		void buttonExit() {
			// Basic confirmation Alert
			Alert confirmation = new Alert(AlertType.CONFIRMATION);

			// Alert Sprache generieren
			if (locale.getCountry().toLowerCase().contains("de")) {
				confirmation.setTitle("Achtung");
				confirmation.setHeaderText("Wirklich beenden?");
				confirmation.setContentText("Sind Sie sich sicher?");
			} else if (locale.getCountry().toLowerCase().contains("en")) {
				confirmation.setTitle("Warning");
				confirmation.setHeaderText("Do you want to close?");
				confirmation.setContentText("Are you sure?");
			} else { // Falls die Sprache nicht Deutsch oder Englisch entspricht
						// wird Englisch ausgew�hlt
				confirmation.setTitle("Warning");
				confirmation.setHeaderText("Do you want to close?");
				confirmation.setContentText("Are you sure?");
			}

			//
			Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				// System wurde beendet
				System.out.println("Installer abgebrochen");
				System.exit(0);
			} else {
				// Installer wird fortgesetz
				System.out.println("Installer nicht abgebochen");
			}
		}

		/****** Button OnAction Methoden Ende *******/
	}

	/*
	 * chooseDatabase Class
	 * 
	 * Legt die Datenbank fest auf der Infos gespeichert werden Standartm��ig eine
	 * Lokale SQLite DB (Settings) kann jedoch auch auf PostgreSQL | MYSQL
	 * ausgelagert werden
	 * 
	 */
	class ChooseDatabase {

		/******* Attribut Deklarationen Start *******/

		// Weiter Button
		@FXML
		Button button_ok;
		@FXML
		Button button_back;

		// Radio Buttons
		@FXML
		RadioButton radioButton_newDatabase;

		// Label um Fortschritt anzuzeigen
		@FXML
		Label label_progress;
		// Zugeh�rige ProgressBar
		@FXML
		ProgressBar progressbar;

		// Stage
		Stage stage;

		/******* Attribut Deklarationen Ende *******/

		// Konstruktor
		public ChooseDatabase(Stage stage) {
			try {
				// Set stage in Attributdeklaration to stage
				this.stage = stage;

				// Language Properties
				ResourceBundle resbun = ResourceBundle.getBundle(
						"bundle." + locale.getCountry().substring(locale.getCountry().length() - 2).toLowerCase(),
						locale);

				// FXML loader
				FXMLLoader loader = new FXMLLoader();
				// Set Controller
				loader.setController(this);
				// Set path to .fxml
				loader.setLocation(getClass().getResource("/installer/DatenbankAuswahl.fxml"));
				// Set Sprach File
				loader.setResources(resbun);
				// Set parent to loader
				Parent root = (Parent) loader.load();
				// Set Scene
				Scene scene = new Scene(root, 600, 340);
				// Stage Settings
				stage.setResizable(false);
				stage.setScene(scene);

				button_ok.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						if (radioButton_newDatabase.isSelected()) {
							System.out.println("\nEs wird eine neue Datenbank angelegt.\n");

							// Update Progressbar & Label in FX-Thread
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									label_progress.setText("100%");
									progressbar.setProgress(1.0);
								}
							});

							// Weiter zum User
							new ChooseUser(stage);
						} else {
							// TODO Access other DB
						}
					}
				});

				// Button k�nnen jetzt auch �ber die Tastatur bedient werden
				button_ok.defaultButtonProperty().bind(button_ok.focusedProperty());
				button_back.defaultButtonProperty().bind(button_back.focusedProperty());

				// Set RadioButton Selected
				radioButton_newDatabase.setFocusTraversable(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@FXML
		void buttonBack() {
			new ChooseLanguage(stage);
		}
	}

	/*
	 * chooseUser Class
	 * 
	 * Legt einen User fest den man mit Password sch�tzen kann man kann jedoch uach
	 * ohne User forfahren... Au�erdem hat man die M�glichkeite einer
	 * Verschl�sselung hierzug wird am Anfang des Startes ein nicht gepeichertes
	 * Password abgefragt um die Informationen zuentschl�sseln
	 * 
	 */
	class ChooseUser {

		/******* Attribut Deklarationen Start *******/

		// Buttons
		@FXML
		Button button_ok;
		@FXML
		Button button_back;

		// Radio Buttons
		@FXML
		RadioButton radioButton_nouser;
		@FXML
		RadioButton radioButton_newUser;

		// Labels
		@FXML
		Label label_username;
		@FXML
		Label label_passwd;

		// Input Fields
		@FXML
		TextField textfield_username;
		@FXML
		PasswordField passwordfield_passwd;

		// ToggleGoup for RadioButtons
		ToggleGroup tg = new ToggleGroup();

		// Stage
		Stage stage;

		/******* Attribut Deklarationen Ende *******/

		// Konstruktor
		public ChooseUser(Stage stage) {
			try {
				// Set stage in Attributdeklaration to stage
				this.stage = stage;

				// Language Properties
				ResourceBundle resbun = ResourceBundle.getBundle(
						"bundle." + locale.getCountry().substring(locale.getCountry().length() - 2).toLowerCase(),
						locale);

				// FXML loader
				FXMLLoader loader = new FXMLLoader();
				// Set Controller
				loader.setController(this);
				// Set path to .fxml
				loader.setLocation(getClass().getResource("/installer/UserAuswahl.fxml"));
				// Set Sprach File
				loader.setResources(resbun);
				// Set parent to loader
				Parent root = (Parent) loader.load();
				// Set Scene
				Scene scene = new Scene(root, 600, 340);
				// Stage Settings
				stage.setResizable(false);
				stage.setScene(scene);

				// Set RadioButton to ToggleGroup
				radioButton_newUser.setToggleGroup(tg);
				radioButton_nouser.setToggleGroup(tg);

				// ToggleGroup ChangeListener
				tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
					@Override
					public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
						if (tg.getSelectedToggle() != null) {
							// Temp RadioButton umsp�ter den Text auzulesen
							RadioButton rabu = (RadioButton) new_toggle.getToggleGroup().getSelectedToggle();

							// Check welcher Button ausgew�hlt wurde
							if (rabu.getText().contains("1)")) {
								// Setze Disabled auf false
								label_passwd.setDisable(true);
								label_username.setDisable(true);
							} else if (rabu.getText().contains("2)")) {
								// Setze Disabled auf false
								label_passwd.setDisable(false);
								label_username.setDisable(false);
							}
						}

					}
				});

				// Button k�nnen jetzt auch �ber die Tastatur bedient werden
				button_ok.defaultButtonProperty().bind(button_ok.focusedProperty());
				button_back.defaultButtonProperty().bind(button_back.focusedProperty());

				// Selektiere Standartm��ig RadioButton
				radioButton_newUser.setFocusTraversable(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// FXML Methode Button Weiter
		@FXML
		void buttonContinue() {
			// Schreibe Daten in DB
			DBCreator cdb = new DBCreator();

			if (radioButton_nouser.isSelected()) {
				// Best�tigungs Meldung
				System.out.println("System hat keinen Password gesch�tzten User");

				// Erzeuge DB ohne passwd | Default User = admin
				cdb.createDatabase(null, null);

				// Generiere Setting Table | Parameterliste: Locale f�r Systemsprache
				cdb.createSettingsTable(locale);

				// Generiere Entries Table
				cdb.createEntriesTable();

				// Generiere Kategorie Table
				cdb.createCategoriesTable();

				// Create File Table
				cdb.createFileTable();

				// Schlie�e DBCreator
				cdb.close();

				// Closing stage
				stage.close();

				// Starte Login
				Login login = new Login();
				login.silentLogin("", "");
			} else {
				if (textfield_username.getText().length() == 0 && passwordfield_passwd.getText().length() == 0) {
					Alert warning = new Alert(AlertType.WARNING);
					warning.setContentText("Username | Password incorrect");
					warning.show();
				} else {
					stage.close();
					// Erzeuge DB mit passwd
					cdb.createDatabase(textfield_username.getText(), passwordfield_passwd.getText());

					// Generiere Setting Table | Parameterliste: Locale f�r Systemsprache
					cdb.createSettingsTable(locale);

					// Generiere Entries Table
					cdb.createEntriesTable();

					// Generiere Kategorie Table
					cdb.createCategoriesTable();

					// Create File Table
					cdb.createFileTable();

					// Schlie�e DBCreator
					cdb.close();

					// Close DBCreator
					cdb.close();

					// Closing Stage
					stage.close();

					// Starte Login
					Login login = new Login();
					login.silentLogin(textfield_username.getText(), passwordfield_passwd.getText());
				}
			}
		}

		// FXML Methode Button Weiter
		@FXML
		void buttonBack() {
			new ChooseDatabase(stage);
		}

	}
}
