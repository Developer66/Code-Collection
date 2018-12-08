package de.developer66.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;

import de.developer66.main.DBHelper;
import de.developer66.main.Kategorie;
import de.developer66.main.MainView;
import de.developer66.util.Toast;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Pair;

public class KategorieController {
	/********** Attribut Deklarationen START **********/

	// Main Table
	@FXML
	private TableView<Kategorie> mainTable;
	// Column für KategorieNamen
	@FXML
	private TableColumn<String, String> tableColumn_name;
	// Columns für Beschreibung
	@FXML
	private TableColumn<String, String> tableColumn_describtion;

	// Label für den Namen
	@FXML
	private Label label_name;
	// Label für Beschreibung
	@FXML
	private Label label_describtion;
	// Label für Erstellungsdatum
	@FXML
	private Label label_created;
	// Label für Verändertdatum
	@FXML
	private Label label_modified;

	// Button add new
	@FXML
	private Button button_add;
	// Button Edit
	@FXML
	private Button button_edit;
	// Button Delete
	@FXML
	private Button button_delete;

	// DBHelper from Mainview
	DBHelper dbhelper = MainView.getDBHelper();

	// String Title
	String title = "Kategorie";

	// Liste mit Alle Kategorien
	ArrayList<Kategorie> list;

	/********** Attribut Deklarationen ENDE **********/

	// Get Title Methode
	public String getTitle() {
		return title;
	}

	// Controller wird initialisiert
	@FXML
	void initialize() {
		initTable();
	}

	// Labels werden aktualisiert
	void initLabels() {
		if (mainTable.getSelectionModel().getSelectedItem() != null) {
			// Dateformatter
			DateFormat df = new SimpleDateFormat("dd-MM-yyyy");

			// Label Name
			label_name.setText(mainTable.getSelectionModel().getSelectedItem().getName());
			// Label Describtion
			label_describtion.setText(mainTable.getSelectionModel().getSelectedItem().getDescribtion());
			// Label created
			label_created.setText(df.format(mainTable.getSelectionModel().getSelectedItem().getErstellt()));
			// Label Verändert
			label_modified.setText(df.format(mainTable.getSelectionModel().getSelectedItem().getVeraendert()));
		}
	}

	// Der Table wird mit den Kategorien gefüllt
	void initTable() {
		// Lade alle Kategorie Infos in die Liste
		list = dbhelper.getFullCategorieInformations();
		// Remove first Item = "Default" Kategorie
		list.remove(0);

		// Clear Items
		mainTable.getItems().clear();
		// Set CellFactorPropertys
		tableColumn_name.setCellValueFactory(new PropertyValueFactory<String, String>("name"));
		tableColumn_describtion.setCellValueFactory(new PropertyValueFactory<String, String>("describtion"));
		// Set Items to Table
		for (Kategorie kategorie : list) {
			mainTable.getItems().add(kategorie);
		}

		mainTable.setFixedCellSize(25);

		// Set OnClick on Item
		mainTable.setRowFactory(tv -> {
			// Get Table Row
			TableRow<Kategorie> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				// Check if Row != null and ClickCount == 1
				if (event.getClickCount() == 1 && !row.isEmpty()) {
					// Init Labels
					initLabels();
				} else if (event.getClickCount() == 2 && !row.isEmpty()) {
					editCategorie();
				}
			});
			return row;
		});
	}

	/******************* Add|Edit|Delete Button *******************/

	/*
	 * New Categorie Button
	 */
	@FXML
	void createCategorie() {
		// Erzeuge Custom Dialog mit leeren Input Feldern
		Optional<Pair<String, String>> result = getCustomDialog("", "").showAndWait();
		// Bekomme ergenisse
		result.ifPresent(pair -> {
			String message = dbhelper.createNewCategorie(pair.getKey(), pair.getValue());
			if (message.length() != 0) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setContentText("Kategorie existiert bereits");
				alert.show();
			} else {
				initTable();
			}
		});
	}

	/*
	 * Edit Categorie Button
	 */
	@FXML
	void editCategorie() {
		if (mainTable.getSelectionModel().getSelectedItem() != null) {
			Kategorie kategorie = mainTable.getSelectionModel().getSelectedItem();
			// Erzeuge Custom Dialog mit leeren Input Feldern
			Optional<Pair<String, String>> result = getCustomDialog(kategorie.getName(), kategorie.getDescribtion())
					.showAndWait();
			// Bekomme ergenisse
			result.ifPresent(pair -> {
				dbhelper.editCategorie(mainTable.getSelectionModel().getSelectedItem().getId(), pair.getKey(),
						pair.getValue());
				initTable();
			});
		} else {
			Toast.makeText("Bitte Eintrag auswählen", 2000, 500, 500);
		}
	}

	/*
	 * Delete Categorie Button
	 */
	@FXML
	void deleteCategorie() {
		if (mainTable.getSelectionModel().getSelectedItem() != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText("Wirklich löschen?");
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == ButtonType.OK) {
				dbhelper.deleteCategorie(mainTable.getSelectionModel().getSelectedItem().getId());
				initTable();
			}
		} else {
			Toast.makeText("Bitte Eintrag auswählen", 2000, 500, 500);
		}
	}

	/*
	 * Diese Methode erzeugt die Custom Alert Meldung diese wird in der Bearbeiten
	 * und Erzeugen Methode für die Kategorie benötigt
	 */
	private Dialog<Pair<String, String>> getCustomDialog(String name, String describtion) {
		// Neuer Dialog
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		// Setze Titel
		dialog.setTitle("Kategorie");
		// Set the button types
		ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		// Add Button
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
		// Get the Stage.
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		// Add a custom icon.
		stage.getIcons().add(new Image("icon.png"));
		// New GridPane
		GridPane gridPane = new GridPane();
		// Set Gaps
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		// Set PAdding
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		// New Label Name
		Label label_name = new Label("Name: ");
		// Set Alignment
		label_name.setAlignment(Pos.TOP_RIGHT);

		// New Label Beschreibung
		Label label_describtion = new Label("Beschreibung: ");
		// Set Alignment
		label_describtion.setAlignment(Pos.TOP_RIGHT);

		// Set textfield for name
		TextField textfield = new TextField(name);
		// Set PromptText
		textfield.setPromptText("Name");

		// Set textarea for describtion
		TextArea textArea = new TextArea(describtion);
		// Set PromptText
		textArea.setPromptText("Beschreibung");
		// Set Wrap > true
		textArea.setWrapText(true);
		// Set Height
		textArea.setPrefHeight(150);
		// Set Width
		textArea.setPrefWidth(300);

		// Add items to gridPane on right pos
		gridPane.add(label_name, 0, 0);
		gridPane.add(label_describtion, 0, 1);
		gridPane.add(textfield, 1, 0);
		gridPane.add(textArea, 1, 1);

		// Set gridPane to dialog
		dialog.getDialogPane().setContent(gridPane);

		// Request focus on the name field by default
		Platform.runLater(() -> textfield.requestFocus());

		// Convert the result to a name-describtion-pair when the ok button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return new Pair<>(textfield.getText(), textArea.getText());
			}
			return null;
		});

		// Return Dialog
		return dialog;
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
}