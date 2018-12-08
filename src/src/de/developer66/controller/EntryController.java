package de.developer66.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import de.developer66.codearea.CustomCodeArea;
import de.developer66.helper.DBHelper;
import de.developer66.main.GifDecoder;
import de.developer66.main.MainView;
import de.developer66.util.EintragFiles;
import de.developer66.util.Entry;
import de.developer66.util.Kategorie;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.Pair;

public class EntryController {
	/********** Attribut Deklarationen START **********/
	// Title Textfield
	@FXML
	private TextField textfield_title;

	// Language Combobox
	@FXML
	private ComboBox<String> combobox_language;

	// Code Pane for CodeArea
	@FXML
	private BorderPane CodePane;

	// Code Describtion
	@FXML
	private TextArea textarea_describtion;

	// Loading pane with GIF laterr
	@FXML
	private BorderPane loadingPane;

	// Combobox für kategorie
	@FXML
	private ComboBox<ComboboxItem> combobox_kategorie;

	// Button Erstellen
	@FXML
	Button button_create;

	// Label für Fehlertext
	@FXML
	Label label_fehler;

	// Int for current ID
	int id = 0;

	// Files Table
	@FXML
	TableView<EintragFiles> tableview_files;
	// Files Table Columns
	@FXML
	private TableColumn<EintragFiles, String> tablecolumn_size;

	@FXML
	private TableColumn<EintragFiles, String> tablecolumn_filename;

	// boolean ob etwas verändert wurde
	boolean veraendert;

	// Enum für Loading Image
	enum LoadingImageType {
		Error, Success
	};

	// Variable für Title der in der MainView angezeigt wird
	String title = "Eintrag";
	// CodeArea
	CodeArea codeArea = new CustomCodeArea().getCodeArea();

	// DBHelper from MainView
	DBHelper dbhelper = MainView.getDBHelper();

	/********** Attribut Deklarationen ENDE **********/

	// Initialize FXML
	@FXML
	public void initialize() {
		// Initialize CodeArea with max Width and Height
		codeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// Add CodeArea to Pane
		CodePane.setCenter(new VirtualizedScrollPane<>(codeArea));

		// Populate Categorie Combobox with values
		populateCategorieCombobox();

		// Set Label fehler Visible False
		label_fehler.setVisible(false);

		// Set On Drag & Drop on FileTable
		tableview_files.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasFiles()) {
					for (File file : db.getFiles()) {
						uploadFile(file);
					}
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});
		// Set DragOver
		tableview_files.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					event.consume();
				}
			}
		});
		// Allow Musliple Select in FileTable
		tableview_files.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	// Hande Button Click in FXML
	@FXML
	void handleSaveButton() {
		boolean succeed = false;

		// If ID is 0 Create Entry
		if (id == 0) {
			// Create Entry
			succeed = createEntry();
		} else { // else Save Entry
			// Save the Entry
			succeed = saveEntry();
		}

		// Wenn kein Fehler vorhanden ist wir der Eintrag geladen
		if (succeed == true) {
			// Lade Eintrag mit ID
			loadEntry();
		}
	}

	/**
	 * Methode zum Überschreiben eines existierenden Eintrags
	 * 
	 * @return true bei alles geklappt oder false bei Fehler
	 */
	private boolean saveEntry() {
		/*
		 * Rufe Methode zum Überschreiben des Eintrags aus dem DBHelper auf als return
		 * Wert ist der Fehlertext
		 * 
		 * Übergebe: - ID - Titel - Kategorie ID - Langage ID - Beschreibung - Code
		 * 
		 * // TODO Langage Combobox
		 */

		String fehlertext = dbhelper.saveEntry(new Entry(id, null, null, textfield_title.getText(),
				combobox_kategorie.getSelectionModel().getSelectedItem().getId(), "", 0, textarea_describtion.getText(),
				codeArea.getText(), 0));

		// Wenn kein Fehler vorhanden
		if (fehlertext == null) {
			// Setze Fehlertext auf Invisible
			label_fehler.setVisible(false);
			// Show Success Gif
			showLoadingGif(LoadingImageType.Success);

			return true;
		} else { // Fehler vorhanden
			// Sete Visible des Fehlerlabels auf true
			label_fehler.setVisible(true);
			// Show Error gif
			showLoadingGif(LoadingImageType.Error);
			// Setze Fehlertext
			label_fehler.setText("Fehler: " + fehlertext);

			return false;
		}
	}

	/**
	 * Create Methode Informationen werden geschrieben und währenddessen wird das
	 * Loading GIF angezeigt
	 * 
	 * @return true bei alles geklappt oder false bei Fehler
	 */
	private boolean createEntry() {
		// Show Loading GIF
		showLoadingGif(LoadingImageType.Success);

		/*
		 * Rufe im DBHelper die Methode zum erstellen eines neuen Eintrags auf. Return
		 * wert ist ein Pair, darin enthalten sind die neue ID und ein Fehlertext
		 * 
		 * Übergebe: - Titel - Kategorie ID - Language ID - Beschreibung - Code
		 * 
		 * //TODO Language
		 */
		Pair<Integer, String> returnvalue = dbhelper.createNewEntry(new Entry(0, null, null, textfield_title.getText(),
				combobox_kategorie.getSelectionModel().getSelectedItem().getId(), "", 0, textarea_describtion.getText(),
				codeArea.getText(), 0));

		// Wenn kein Fehler vorhanden ist wird lokale ID auf bekommene ID gesetzt
		if (returnvalue.getValue() == null) {
			// Sete ID
			id = returnvalue.getKey();
			// Setzte Label nicht sichtbar
			label_fehler.setVisible(false);

			return true;
		} else { // Fehler aufgetretten
			// Setze Fehlerlabel sichtbar
			label_fehler.setVisible(true);
			// Setze Fehlertext
			label_fehler.setText("Fehler: " + returnvalue.getValue());
			// Show Error Gif
			showLoadingGif(LoadingImageType.Error);

			return false;
		}
	}

	/*
	 * Load Entry Methode
	 */
	public void loadEntry() {
		// Bekomme entry mit lokaler ID
		Entry entry = dbhelper.getEntry(id);

		// Set Title to Entry Title
		textfield_title.setText(entry.getTitle());
		// Set Describiton to Entry Describiton
		textarea_describtion.setText(entry.getBeschreibung());
		// Clear CodeArea
		codeArea.clear();
		// Insert Text from Entry in Pos 0
		codeArea.insertText(0, entry.getCode());
		// Setze Kategoire
		combobox_kategorie.getSelectionModel()
				.select(new ComboboxItem(entry.getKategorie(), dbhelper.getCategorieName(entry.getKategorie())));
		// TODO Language
		// Change Button Text
		button_create.setText("Speichern");

		// Lade Files wenn ID != 0
		if (id != 0) {
			loadFileTable();
		}
	}

	/*
	 * Load Entry Methode mit übergabe der ID, um den Eintrag aus der Verwaltung
	 * zuladen.
	 */
	void loadEntry(int id) {
		// Setze lokale ID Variable von Parameterliste
		this.id = id;
		// Starte noramle LoadEntry Methode
		loadEntry();
	}

	/*
	 * Populate the categorie Combobox
	 */
	void populateCategorieCombobox() {
		// Bekomme eine Listen mit allen Kategorien vom DBHelper
		ArrayList<Kategorie> categorieNames = dbhelper.getFullCategorieInformations();

		// Gehe Liste durch
		for (Kategorie kategorie : categorieNames) {
			// Setze Items auf Combobox
			combobox_kategorie.getItems().add(new ComboboxItem(kategorie.getId(), kategorie.getName()));
		}
		String topKategorie = dbhelper.getTopCategorie();
		if (topKategorie.length() != 0) {
			// Default Select Top Categorie
			combobox_kategorie.getSelectionModel().select(new ComboboxItem(0, dbhelper.getTopCategorie()));
		} else {
			combobox_kategorie.getSelectionModel().select(0);
		}
	}

	/*
	 * Populate File Table
	 */
	void loadFileTable() {
		System.out.println("Lade Dateien...");

		// SetCellValueFactory
		tablecolumn_filename.setCellValueFactory(new PropertyValueFactory("name"));
		tablecolumn_size.setCellValueFactory(new PropertyValueFactory("formattedSize"));

		// Clear Table
		tableview_files.getItems().clear();

		// Lade Files und setze sie auf den Table
		tableview_files.setItems(dbhelper.getFiles(id));
	}

	/********** CONTEXTMENU FILETABLE **********/
	@FXML
	void contextmenu_open(ActionEvent event) {
		// Only allow if ID != 0
		if (id != 0 && tableview_files.getSelectionModel().getSelectedItem() != null) {
			// Check for Selected Row Count
			if (tableview_files.getSelectionModel().getSelectedItems().size() < 2) {
				// Lade Selected File id
				int selectedFileId = tableview_files.getSelectionModel().getSelectedItem().getId();

				// Speicher Verzeichniss abfragen
				// Lade File
				File file = dbhelper.getFile(selectedFileId, true);

				if (file != null) {
					try {
						// Try to get Desktop
						Desktop desktop = Desktop.getDesktop();
						// Try to open File
						desktop.open(file);
					} catch (Exception e) {
						new Error(e.getMessage());
					}
				}
			} else {
				// Get Custom Directory
				DirectoryChooser directoryChooser = new DirectoryChooser();
				// Set Default direcory
				directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				// Get direcory
				File directory = directoryChooser.showDialog(null);

				// Check if user selected a direcotry
				if (directory != null) {
					directory = new File(directory.getAbsoluteFile() + File.separator);

					// Check for MultiSelect
					for (EintragFiles item : tableview_files.getSelectionModel().getSelectedItems()) {
						// Lade Selected File id
						int selectedFileId = item.getId();

						// Speicher Verzeichniss abfragen
						// Lade File
						File file = dbhelper.writeFileToCustomDirecory(directory, selectedFileId);

						if (file != null) {
							try {
								// Try to get Desktop
								Desktop desktop = Desktop.getDesktop();
								// Try to open File
								desktop.open(directory);
							} catch (Exception e) {
								new Error(e.getMessage());
							}
						}
					}
				}
			}
		}
	}

	@FXML
	void contextmenu_new(ActionEvent event) {
		// Only allow if ID != 0
		if (id != 0) {
			// Open File Dialog
			FileChooser fileChooser = new FileChooser();
			// Get Selected File
			File selectedFile = fileChooser.showOpenDialog(null);

			// If File was selected...
			if (selectedFile != null) {
				// Upload File
				uploadFile(selectedFile);
				// Refresh FileTable
				loadFileTable();
			}
		}
	}

	@FXML
	void contextmenu_change(ActionEvent event) {

	}

	@FXML
	void contextmenu_delete(ActionEvent event) {
		if (id != 0 && tableview_files.getSelectionModel().getSelectedItem() != null) {
			// Check for MultiSelect
			// Check for SelectionItems Count
			if (tableview_files.getSelectionModel().getSelectedItems().size() < 2) {
				// Confirmation Alert
				Alert alert = new Alert(AlertType.CONFIRMATION);
				// Set Content Text
				alert.setContentText("Delete??");

				// Optinal for Result
				Optional<ButtonType> optinal = alert.showAndWait();
				// Handle ButtonType OK
				if (optinal.get() == ButtonType.OK) {
					// Get SelectedFileId
					int selectedFileId = tableview_files.getSelectionModel().getSelectedItem().getId();

					// Delete File
					dbhelper.deleteFile(selectedFileId);
				}
			} else {
				// Confirmation Alert
				Alert alert = new Alert(AlertType.CONFIRMATION);
				// Set Content Text
				alert.setContentText(
						"Delete " + tableview_files.getSelectionModel().getSelectedItems().size() + " items??");

				// Optinal for Result
				Optional<ButtonType> optinal = alert.showAndWait();
				// Handle ButtonType OK
				if (optinal.get() == ButtonType.OK) {
					for (EintragFiles item : tableview_files.getSelectionModel().getSelectedItems()) {

						// Get SelectedFileId
						int selectedFileId = item.getId();

						// Delete File
						dbhelper.deleteFile(selectedFileId);
					}
				}
			}
			// Refresh File Table
			loadFileTable();
		}
	}

	/*********** FILE UPLOAD ***********/
	void uploadFile(File file) {
		if (file != null && file.length() <= 15000000) {
			// Upload File
			dbhelper.uploadFile(file, file.length(), id);

			// Load fileTable
			loadFileTable();
		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("File size too large\nSize: " + dbhelper.getFileSize(file.length()));
			alert.show();
		}
	}

	/*********** GETTER & SETTER ***********/
	// Returns Title for MainView
	public String getTitle() {
		return title;
	}

	/*********** GIF CLASSES ***********/
	// Show Loading Screen with AnimatedGif
	Animation showLoadingGif(LoadingImageType type) {
		// ImagePath
		String src = "";
		// Duration
		Duration duration;
		// Boolean isError

		// Look which Type is requested
		switch (type) {
		case Error:
			src = "/error.gif";
			duration = Duration.seconds(3);
			break;
		case Success:
			duration = Duration.seconds(2);
			src = "/check.gif";
			break;
		default:
			duration = Duration.seconds(2);
			src = "/check.gif";
			break;
		}

		// Set new GIF > /check.gif | PlayTime 2000 ms
		Animation ani = new AnimatedGif(getClass().getResource(src).toExternalForm(), duration.toMillis());
		// Set CycleCount to 1 to show GIF only one time
		ani.setCycleCount(1);
		// Start Gif
		ani.play();

		// OnFinish blende Loadingscreen aus
		ani.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Hide LoadingPane
				loadingPane.setVisible(false);
			}
		});

		// Set Pane Visible
		loadingPane.setVisible(true);
		// New ImageView with aniView
		ImageView imageview = ani.getView();
		// Add imageview to loadingPAne
		loadingPane.setCenter(imageview);

		// Set MAX Width & Height
		loadingPane.setMaxWidth(loadingPane.getWidth());
		loadingPane.setMaxHeight(loadingPane.getHeight());

		// Return ani
		return ani;
	}

	/*********** Combobox Item Class **********/
	class ComboboxItem {
		// Name
		private String name;
		// Id (DB ID)
		private int id;

		// Konstruktor | Parameter ID, Name
		public ComboboxItem(int id, String name) {
			// Sete ID
			this.id = id;
			// Setze Name
			this.name = name;
		}

		// Overrifde to String Methode
		@Override
		public String toString() {
			return name;
		}

		// Get ID Methode
		public int getId() {
			return id;
		}
	}

	/********** GIF ANIMATION **********/
	class AnimatedGif extends Animation {
		public AnimatedGif(String filename, double durationMs) {
			GifDecoder d = new GifDecoder();
			d.read(filename);

			Image[] sequence = new Image[d.getFrameCount()];
			for (int i = 0; i < d.getFrameCount(); i++) {

				WritableImage wimg = null;
				BufferedImage bimg = d.getFrame(i);
				sequence[i] = SwingFXUtils.toFXImage(bimg, wimg);
			}

			super.init(sequence, durationMs);
		}

	}

	class Animation extends Transition {

		private ImageView imageView;
		private int count;

		private int lastIndex;

		private Image[] sequence;

		private Animation() {
		}

		public Animation(Image[] sequence, double durationMs) {
			init(sequence, durationMs);
		}

		private void init(Image[] sequence, double durationMs) {
			this.imageView = new ImageView(sequence[0]);
			this.sequence = sequence;
			this.count = sequence.length;

			setCycleCount(1);
			setCycleDuration(Duration.millis(durationMs));
			setInterpolator(Interpolator.LINEAR);
		}

		@Override
		protected void interpolate(double k) {

			final int index = Math.min((int) Math.floor(k * count), count - 1);
			if (index != lastIndex) {
				imageView.setImage(sequence[index]);
				lastIndex = index;
			}

		}

		public ImageView getView() {
			return imageView;
		}

	}
}