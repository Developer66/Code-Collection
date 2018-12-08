package de.developer66.controller;

import java.awt.Desktop;
import java.net.URI;
import java.text.DecimalFormat;

import de.developer66.main.DBHelper;
import de.developer66.main.MainView;
import de.developer66.main.UpdateHelper;
import de.developer66.util.Toast;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/*
 * MainView FXML Controller
 */
public class MainViewController {

	/********** Attributdeklarationen START **********/

	// Default DB Helper from MainView
	DBHelper dbhelper = MainView.getDBHelper();;

	// MainVbox
	@FXML
	private VBox vboxMain;

	// MainView >> Scrollpane
	@FXML
	ScrollPane scrollpane;

	// TitlePane für Aktuelle Titel
	@FXML
	private TitledPane newEntry;

	// Loader des aktuellen FXMLs
	FXMLLoader currentLoader;

	// Label für aktuellen FXML Titel
	@FXML
	Label label_currentView;

	// Label für den Usernamen
	@FXML
	private Label label_username;

	// Label für Linken Status
	@FXML
	private Label label_leftstatus;

	// Label für Rechten Status
	@FXML
	private Label label_rightstatus;

	// Menu
	@FXML
	private Menu menu_settings;
	// Menuitem CheckForUpdates
	@FXML
	private MenuItem menuitem_checkforupdates;

	// BorderPane für Userimg
	@FXML
	BorderPane userimageview;

	/********** Attributdeklarationen ENDE **********/

	/*
	 * Crontroller für MainView wird initialisiert
	 */
	@FXML
	void initialize() {
		// Init Menu
		initMenus();
		// Show Intro
		showIntro();
		// LoadUserImage
		loadUserImage();
	}

	/******************* Show new Views *******************/
	/*
	 * Show Intro in MainView
	 */
	public void showIntro() {
		/*
		 * Falls in der DB steht das WelcomeScreen übersprungen werden soll, wir dieser
		 * nun übersprungen und es wird gleich das Dashboard angezeigt
		 */
		if (dbhelper.getSetting("skip_welcome").equals("false")) {
			// Lade FXML
			Task<Parent> task = loadFXML("/main/Welcome.fxml");
			// Set On Succeded
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent arg0) {
					// Get Current Controller from loader
					WelcomeController wc = currentLoader.getController();

					// Get Continue Button from Controller
					Button buttonContinue = wc.getContinueButton();
					// Get Checkbox
					CheckBox checkbox = wc.getCheckbox();

					// Setze on Action für continue Button
					buttonContinue.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							// Überprüfe ob checkbox selected ist
							if (checkbox.isSelected()) { // Checkbox ist selected
								// Update Setting in DB
								dbhelper.changeSetting("skip_welcome", "true");
								// Show Dashboard
								showDashboard();
							} else {
								// Show Dashboard
								showDashboard();
							}
						}
					});

					// Set Scrollpane to new FXML
					scrollpane.setContent(task.getValue());

					// Set Title
					label_currentView.setText(wc.getTitle());
				}
			});
		} else {
			showDashboard();
		}

	}

	/*
	 * Dashboard wird initialisiert und anschließend angezeigt...
	 */
	@FXML
	public void showDashboard() {
		// Load DashboardFXML
		Task<Parent> task = loadFXML("/main/Dashboard.fxml");
		// Set on Succeeded
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				// Get Current FXML Loader
				DashboardController dc = currentLoader.getController();

				// Set Tooltip for PieChart in Dashboard
				dc.getDoughnutChart().getData().stream().forEach(data -> {
					// DecimalFomat um max 4 Stellen darzustellen
					DecimalFormat formatter = new DecimalFormat();
					// Set max Stellen auf 4
					formatter.setMaximumFractionDigits(2);

					// Neuer Tooltip mit Text:
					// (100/GesamtAnzahlVonEinträgen)*PieChartDaten
					Tooltip tooltip = new Tooltip(
							formatter.format((100.0 / dbhelper.getEntriesRowCount()) * data.getPieValue()) + "%");
					// Setze Tooltip auf das PieChart
					Tooltip.install(data.getNode(), tooltip);
					// Vergrößere Schrift
					tooltip.setStyle("-fx-font-size: 50px");
				});

				// Setze Tooltip for LineChart in Dashboard
				for (Series<String, Number> s : dc.getLineChart().getData()) {
					// Durchschaue alle Daten
					for (Data<String, Number> d : s.getData()) {
						// Installiere Tooltip
						Tooltip tooltip = new Tooltip(d.getXValue().toString() + "\nAnzahl: " + d.getYValue());
						Tooltip.install(d.getNode(), tooltip);
						tooltip.setStyle("-fx-font-size: 50px");

						// Adding class on hover
						d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

						// Removing class on exit
						d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
					}
				}

				// Set Scrollpane to new FXML
				scrollpane.setContent(task.getValue());
				// Set Title
				label_currentView.setText(dc.getTitle());
			}
		});
	}

	/*
	 * Kategorie Verwaltung wird aufegrufen und initialisiert
	 */
	@FXML
	public void showKategorie() {
		// Load yFXML
		Task<Parent> task = loadFXML("/main/Kategorie.fxml");
		// Set on Succeeded
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				// Get Current FXML Loader
				KategorieController kc = currentLoader.getController();

				// Set Scrollpane to new FXML
				scrollpane.setContent(task.getValue());
				// Set Title
				label_currentView.setText(kc.getTitle());
			}
		});
	}

	/*
	 * Methode zum aufrufen der Eintragsverwaltung
	 */
	@FXML
	public void showVerwaltung() {
		// Load yFXML
		Task<Parent> task = loadFXML("/main/Verwaltung.fxml");
		// Set on Succeeded
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				// Get Current FXML Loader
				VerwaltungController vc = currentLoader.getController();

				// Set Scrollpane to new FXML
				scrollpane.setContent(task.getValue());
				// Set Title
				label_currentView.setText(vc.getTitle());
			}
		});
	}

	/*
	 * Menubar wird inizialisiert
	 */
	public void initMenus() {
		// Label für Text mit Text-Farbe Weiß
		Label label = new Label();
		// Set Text
		label.setText("⚙  Settings");
		// Set Color > White
		label.setTextFill(Paint.valueOf("White"));
		// Set Graphic Label to Menu
		menu_settings.setGraphic(label);

		// Username String
		String username = dbhelper.getUsername().substring(0, 1).toUpperCase() + dbhelper.getUsername().substring(1);
		// Set Label for Username
		label_username.setText(username);

		// Set Left Status
		label_leftstatus.setText("Database connected");
		// Set Textcolor to Green
		label_leftstatus.setTextFill(Paint.valueOf("#006600"));

		// Set Right Status
		label_rightstatus.setText("Version: " + MainView.getJarVersion());

		/********** Action Listener **********/

		// Menuitems für Update Check
		menuitem_checkforupdates.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Starte UpdateHelper
				new UpdateHelper(true);
			}
		});
	}

	/*
	 * Methode zum Laden des "Neuer Eintrag" FXML
	 */
	@FXML
	void showEntry() {
		// Load yFXML
		Task<Parent> task = loadFXML("/main/Entry.fxml");
		// Set on Succeeded
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent arg0) {
				// Get Current FXML Loader
				EntryController ec = (EntryController) currentLoader.getController();

				// Set Scrollpane to new FXML
				scrollpane.setContent(task.getValue());
				// Set Title
				label_currentView.setText(ec.getTitle());
			}
		});
	}

	/*
	 * FXML Methode für ShareButton
	 * 
	 * INFO: - Funktioniert Aktuell nur auf Windows.. - TODO Linux | Mac Support
	 */
	@FXML
	void share() {
		try {
			String message = "Hey,%20look%20at%20CodeCollection%20by%20Developer66%20on%20Github:%20https://github.com/Developer66/Code-Collection";
			Desktop.getDesktop().mail(new URI("mailto:YourContact?subject=CodeCollection&body=" + message));
		} catch (Exception e) {
			Toast.makeText("Dieses System unterstützt warscheinlich keine mailto", 2000, 500, 500);
			System.out.println("Dieses System unterstützt warscheinlich keine mailto...");
		}
	}

	/******************* User Image *******************/
	/*
	 * Methode zum Laden eines Userimages
	 */
	void loadUserImage() {
		/********** Set User Image **********/
		// New Circleview
		Circle circleview = new Circle(50);
		// Set Border Color BLACK
		circleview.setStroke(Color.BLACK);
		// Set Border Width to 2
		circleview.setStrokeWidth(2);
		// Set Default IMG

		// ImageView mit Person.png
		Image imageview = new Image("/menuicons/person.png", false);

		// Set ImageView to Circleview
		circleview.setFill(new ImagePattern(imageview));
		// Add CircleView to Borderpane
		userimageview.setCenter(circleview);
	}

	/******************* Window Buttons *******************/
	// Menu Buttons (+ , - , x)
	@FXML
	private Button button_close;

	@FXML
	private Button button_maximize;

	@FXML
	private Button button_minimize;

	/************** Getter & Setter **************/
	// GETTER Close Button
	public Button getButtonClose() {
		return button_close;
	}

	// GETTER Butotn Maximize
	public Button getButtonMaximize() {
		return button_maximize;
	}

	// GETTER Button Minimize
	public Button getButtoMinimize() {
		return button_minimize;
	}

	/**
	 * @return the vboxMain
	 */
	public VBox getVboxMain() {
		return vboxMain;
	}

	/**
	 * SETTER für den Akutellen Loader dies wird benötigt falls man den Controller
	 * 
	 * @param FXMLLoader
	 */
	void setLoader(FXMLLoader loader) {
		// Setze Loader
		currentLoader = loader;
	}

	/******************* Window Buttons *******************/
	/*
	 * Set Buttons background on Mouse Hover to #e0e0e0
	 */
	@FXML
	void windowButtonOnEntered(MouseEvent me) {
		Button button = (Button) me.getSource();
		// Überprüfe ob der Button der schließ-Button ist
		// Wenn ja wird der Background statt auf #e0e0e0
		// aus red gesetzt
		if (button.getText().contains("×")) {
			button.setStyle("-fx-background-color: red; -fx-background-radius: 0;");
			button.setTextFill(Paint.valueOf("White"));
		} else {
			button.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 0;");
			button.setTextFill(Paint.valueOf("Black"));
		}
	}

	/*
	 * Set Buttons background transparent again
	 */
	@FXML
	void windowButtonOnExited(MouseEvent me) {
		Button button = (Button) me.getSource();
		button.setStyle("-fx-background-color: transparent;");
		button.setTextFill(Paint.valueOf("#e0e0e0"));
	}

	/*******************
	 * NavigaationDrawer Menuitem Mouse handle
	 *******************/
	/*
	 * Menuitem das mit der Maus überfahren wird bekommt eine andere
	 * Hintergrundfarbe
	 */
	@FXML
	void DrawerMenuItemMouseEntered(MouseEvent me) {
		// Get Button
		Button button = (Button) me.getSource();
		// Set background > #eeeeee
		button.setStyle("-fx-background-color: #eeeeee");
	}

	/*
	 * Menuitem das mit der Maus verlassen wird bekommt eine andere Hintergrundfarbe
	 */
	@FXML
	void DrawerMenuItemMouseExited(MouseEvent me) {
		// Get Button
		Button button = (Button) me.getSource();
		// Set background > #fff
		button.setStyle("-fx-background-color: #fff");
	}

	/*
	 * Loader for custom FXML on ScrollPane
	 * 
	 * Übergabe eines Path z.B.: /main/Welcome.fxml der anschließend geladen wird
	 * und auf der Main ScrollPane angezeigt wird...
	 */
	Task<Parent> loadFXML(String path) {
		// Loader Task
		Task<Parent> createMainScene = new Task<Parent>() {
			@Override
			public Parent call() {
				// Update Message
				updateMessage("Loading...");

				// Pane with FXML
				Pane pane = null;

				try {
					// FXML loader
					FXMLLoader loader = new FXMLLoader();

					// Set path to PARAMETERLISTE.fxml
					loader.setLocation(getClass().getResource(path));

					// Set load fxml to Pane
					pane = (Pane) loader.load();

					pane.getStylesheets().add("/MetroButtonStyle.css");

					setLoader(loader);
				} catch (Exception e) {
					System.err.println("Error. Grund: " + e.getMessage());
				}

				// Return Pane
				return pane;
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

		// Set Curren View to "Loading"
		label_currentView.setText("Loading...");

		// Layout
		VBox root = new VBox(statusLabel, pBar);
		// SetFill Width TRUE
		root.setFillWidth(true);
		// Center Items
		root.setAlignment(Pos.CENTER);
		// Set Stylesheet
		root.getStylesheets().add("MetroStyle.css");
		// Set Background to #e0e0e0
		root.setStyle("-fx-background-color: #e0e0e0");

		// Set Layout to ScrollPane
		scrollpane.setContent(root);

		// Display loaded FXML onSucceed
		createMainScene.setOnSucceeded(e -> {
			scrollpane.setContent(createMainScene.getValue());
		});

		// Start Thread
		new Thread(createMainScene).start();

		return createMainScene;
	}
}
