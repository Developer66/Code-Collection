package de.developer66.controller;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.google.jhsheets.filtered.FilteredTableView;
import org.google.jhsheets.filtered.operators.DateOperator;
import org.google.jhsheets.filtered.operators.IFilterOperator;
import org.google.jhsheets.filtered.operators.NumberOperator;
import org.google.jhsheets.filtered.operators.StringOperator;
import org.google.jhsheets.filtered.tablecolumn.ColumnFilterEvent;
import org.google.jhsheets.filtered.tablecolumn.FilterableDateTableColumn;
import org.google.jhsheets.filtered.tablecolumn.FilterableIntegerTableColumn;
import org.google.jhsheets.filtered.tablecolumn.FilterableStringTableColumn;

import de.developer66.helper.DBHelper;
import de.developer66.main.Error;
import de.developer66.main.MainView;
import de.developer66.util.Entry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VerwaltungController {

	/********* Attributdeklaration START *********/

	// Title der in der MainView angezeigt wird
	private String title = "Eintrags Verwaltung";

	// DBHelper
	private DBHelper DBHelper = MainView.getDBHelper();

	private FilteredTableView<Entry> filtertable;

	// Table Columns
	@FXML
	private FilterableStringTableColumn<Entry, String> tablecolumnTitel; // Title
	@FXML
	private FilterableIntegerTableColumn<Entry, Integer> tablecolumnId; // ID
	@FXML
	private FilterableStringTableColumn<Entry, String> tablecolumnLanguage; // Language
	@FXML
	private FilterableDateTableColumn<Entry, Date> tablecolumnModified; // Modified
	@FXML
	private TableColumn<Entry, Integer> tablecolumnFiles; // Files
	@FXML
	private FilterableStringTableColumn<Entry, String> tablecolumnCategorie; // Categorie
	@FXML
	private FilterableStringTableColumn<Entry, String> tablecolumnDescribtion; // Beschreibung
	@FXML
	private FilterableDateTableColumn<Entry, Date> tablecolumnCreated; // Created

	// BorderPane für FilterTable
	@FXML
	private BorderPane borderpaneTable;

	@FXML
	private BorderPane loadingpane;

	@FXML
	private StackPane stackpaneMain;

	ObservableList<Entry> items = FXCollections.observableArrayList();

	/********* Attributdeklaration ENDE *********/

	// Initialize Methode
	@FXML
	void initialize() {
		// Erzeuge FilterTable und gebe Items mit
		filtertable = new FilteredTableView<>(items);
		filtertable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		filtertable.setFixedCellSize(24);

		filtertable.setRowFactory(tv -> {
			TableRow<Entry> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Entry rowData = row.getItem();
					int selectedid = rowData.getId();

					MainViewController mvc = MainView.getMainViewConroller();
					Task<Parent> task = mvc.loadFXML("/main/Entry.fxml");

					task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent arg0) {
							try {
								mvc.scrollpane.setContent(task.get());
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							}

							EntryController ec = mvc.currentLoader.getController();
							ec.loadEntry(selectedid);

							mvc.label_currentView.setText(ec.getTitle());
						}
					});
				}
			});
			return row;
		});

		// Füge ContextMenu zum Table hinzu
		MenuItem deleteRow = new MenuItem("Löschen");
		deleteRow.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (filtertable.getSelectionModel() != null
						&& filtertable.getSelectionModel().getSelectedItem() != null) {
					int id = filtertable.getSelectionModel().getSelectedItem().getId();

					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setContentText("Wirklich löschen??");
					Optional<ButtonType> rs = alert.showAndWait();
					if (rs.get() == ButtonType.OK) {
						MainView.getDBHelper().deleteEntry(id);
					}

					filtertable.getItems().remove(filtertable.getSelectionModel().getSelectedItem());
				}
			}
		});

		ContextMenu contextMenu = new ContextMenu(deleteRow);
		filtertable.setContextMenu(contextMenu);

		// SetCellValueFactory
		tablecolumnId = new FilterableIntegerTableColumn<>("ID");
		tablecolumnId.setCellValueFactory(new PropertyValueFactory("id"));
		tablecolumnId.setMaxWidth(50);
		tablecolumnId.setMinWidth(50);

		tablecolumnTitel = new FilterableStringTableColumn<>("Titel");
		tablecolumnTitel.setCellValueFactory(new PropertyValueFactory("title"));

		tablecolumnDescribtion = new FilterableStringTableColumn<>("Beschreibung");
		tablecolumnDescribtion.setCellValueFactory(new PropertyValueFactory("beschreibung"));

		tablecolumnLanguage = new FilterableStringTableColumn<>("Programmiersprache");
		tablecolumnLanguage.setCellValueFactory(new PropertyValueFactory("language"));

		tablecolumnCategorie = new FilterableStringTableColumn<>("Kategorie");
		tablecolumnCategorie.setCellValueFactory(new PropertyValueFactory("kategoriename"));

		tablecolumnFiles = new TableColumn("Anhänge");
		tablecolumnFiles.setCellValueFactory(new PropertyValueFactory("files"));

		tablecolumnModified = new FilterableDateTableColumn<>("Verändert");
		tablecolumnModified.setCellValueFactory(new PropertyValueFactory("modified"));

		tablecolumnCreated = new FilterableDateTableColumn<>("Erstellt");
		tablecolumnCreated.setCellValueFactory(new PropertyValueFactory("created"));

		// @formatter:off
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				borderpaneTable.setCenter(filtertable);
				filtertable.getColumns().addAll(
						tablecolumnId,
						tablecolumnCategorie,
						tablecolumnTitel,
						tablecolumnDescribtion,
						tablecolumnLanguage,
						tablecolumnFiles,
						tablecolumnModified,
						tablecolumnCreated
						);
			}
		});
		// @formatter:on

		filtertable.addEventHandler(ColumnFilterEvent.FILTER_CHANGED_EVENT, new EventHandler<ColumnFilterEvent>() {
			@Override
			public void handle(ColumnFilterEvent t) {
				System.out.println("Filtered column count: " + filtertable.getFilteredColumns().size());
				System.out.println("Filtering changed on column: " + t.sourceColumn().getText());
				System.out.println("Current filters on column " + t.sourceColumn().getText() + " are:");
				final List<IFilterOperator> filters = t.sourceColumn().getFilters();
				for (IFilterOperator filter : filters) {
					System.out.println("  Type=" + filter.getType() + ", Value=" + filter.getValue());
				}

				applyFilters();
			}
		});

		// Loader Task
		Task<Parent> createMainScene = new Task<Parent>() {
			@Override
			public Parent call() {
				updateMessage("Erzeuge Tabelle");

				int geladeneEintraege = 0;
				int gesamteEintraege = DBHelper.getEntriesRowCount();

				// Update Message
				updateMessage("Lade Einträge " + geladeneEintraege + " / " + gesamteEintraege);

				try {
					String sqlcommand = "SELECT entries.id,entries.created,entries.modified,title,categorie,name,language,entries.describtion FROM entries LEFT JOIN CATEGORIES ON ENTRIES.CATEGORIE = CATEGORIES.ID ORDER BY entries.ID";
					ResultSet rs = DBHelper.getConnection().createStatement().executeQuery(sqlcommand);
					while (rs.next()) {
						items.add(new Entry(rs.getInt("id"), rs.getDate("created"), rs.getDate("modified"),
								rs.getString("title"), rs.getInt("categorie"), rs.getString("name"),
								rs.getInt("language"), rs.getString("describtion"), null,
								DBHelper.getFiles(rs.getInt("entries.id")).size()));

						geladeneEintraege++;
						updateMessage("Lade Einträge " + geladeneEintraege + " / " + DBHelper.getEntriesRowCount());
						updateProgress(geladeneEintraege, gesamteEintraege);
					}
				} catch (Exception e) {
					new Error(e.getMessage());
					e.printStackTrace();
				}

				// Return Pane
				return null;
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
		// Set Stylesheet
		root.getStylesheets().add("MetroStyle.css");
		// Set Background to #e0e0e0
		root.setStyle("-fx-background-color: #e0e0e0");

		loadingpane.setCenter(root);

		// Display loaded FXML onSucceed
		createMainScene.setOnSucceeded(e -> {
			filtertable.setItems(items);
			stackpaneMain.getChildren().remove(loadingpane);
		});

		// Start Thread
		new Thread(createMainScene).start();
	}

	/********* GETTER & SETTER *********/
	public String getTitle() {
		return title;
	}

	/********** Filter **********/

	public void applyFilters() {
		try {
			final ObservableList<Entry> newData = FXCollections.observableArrayList();
			newData.addAll(items);

			// Filter the data...
			filterId(tablecolumnId, newData);
			filterTitle(tablecolumnTitel, newData);
			filterLanguage(tablecolumnLanguage, newData);
			filterModified(tablecolumnModified, newData);
			filterCreated(tablecolumnCreated, newData);
			filterCategorie(tablecolumnCategorie, newData);
			filterDescription(tablecolumnDescribtion, newData);

			// Display the filtered results
			filtertable.setItems(newData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Filtert die Id Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	private void filterId(FilterableIntegerTableColumn col, ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<NumberOperator<Integer>> filters = col.getFilters();
		for (NumberOperator<Integer> filter : filters) {
			for (Entry item : newData) {
				int tableValue = item.getId();

				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.
				if (filter.getType() == NumberOperator.Type.EQUALS) {
					if (tableValue != filter.getValue()) {
						remove.add(item);
					}
				} else if (filter.getType() == NumberOperator.Type.NOTEQUALS) {
					if (tableValue == filter.getValue()) {
						remove.add(item);
					}
				} else if (filter.getType() == NumberOperator.Type.GREATERTHAN) {
					if (tableValue < filter.getValue()) {
						remove.add(item);
					}
				} else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS) {
					if (tableValue <= filter.getValue()) {
						remove.add(item);
					}
				} else if (filter.getType() == NumberOperator.Type.LESSTHAN) {
					if (tableValue >= filter.getValue()) {
						remove.add(item);
					}
				} else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS) {
					if (tableValue > filter.getValue()) {
						remove.add(item);
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	/**
	 * Filtert die Title Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	private void filterTitle(FilterableStringTableColumn col, ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<StringOperator> filters = col.getFilters();
		for (StringOperator filter : filters) {
			for (Entry item : newData) {
				String tableValue = item.getTitle();
				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.
				String vergleich = null;
				if (tableValue != null && !tableValue.equals("")) {
					vergleich = tableValue.toUpperCase();
				}
				if (filter.getType() == StringOperator.Type.EQUALS) {
					if (!filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
					if (filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.CONTAINS) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.contains(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.STARTSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.startsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.ENDSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.endsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	/**
	 * Filtert die Programmiersprache Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	private void filterLanguage(FilterableStringTableColumn<Entry, String> tablecolumnLanguage2,
			ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<StringOperator> filters = tablecolumnLanguage2.getFilters();
		for (StringOperator filter : filters) {
			for (Entry item : newData) {
				String tableValue = item.getLanguageInText();
				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.
				String vergleich = null;
				if (tableValue != null && !tableValue.equals("")) {
					vergleich = tableValue.toUpperCase();
				}
				if (filter.getType() == StringOperator.Type.EQUALS) {
					if (!filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
					if (filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.CONTAINS) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.contains(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.STARTSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.startsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.ENDSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.endsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	/**
	 * Filtert die Modified Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	@SuppressWarnings("unused")
	private void filterModified(FilterableDateTableColumn col, ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<DateOperator> filters = col.getFilters();
		for (DateOperator filter : filters) {
			for (Entry item : newData) {
				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.

				Date vergleich = null;
				if (item.getModified() != null) {
					vergleich = item.getModified();
				}
				if (filter.getType() == DateOperator.Type.EQUALS) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (!removeTime(vergleich).equals(removeTime(filter.getValue()))) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (removeTime(vergleich).equals(removeTime(filter.getValue()))) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.BEFORE) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (!vergleich.before(filter.getValue())) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.BEFOREON) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (vergleich.compareTo(filter.getValue()) > 0) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.AFTER) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (!vergleich.after(filter.getValue())) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.AFTERON) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (vergleich.compareTo(filter.getValue()) < 0) {
							// In between
							remove.add(item);

						}
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	/**
	 * Filtert die Credate Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	@SuppressWarnings("unused")
	private void filterCreated(FilterableDateTableColumn<Entry, Date> tablecolumnCreated2,
			ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<DateOperator> filters = tablecolumnCreated2.getFilters();
		for (DateOperator filter : filters) {
			for (Entry item : newData) {
				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.

				Date vergleich = null;
				if (item.getModified() != null) {
					vergleich = item.getModified();
				}
				if (filter.getType() == DateOperator.Type.EQUALS) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (!removeTime(vergleich).equals(removeTime(filter.getValue()))) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (removeTime(vergleich).equals(removeTime(filter.getValue()))) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.BEFORE) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (!vergleich.before(filter.getValue())) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.BEFOREON) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (vergleich.compareTo(filter.getValue()) > 0) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.AFTER) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (!vergleich.after(filter.getValue())) {
							// In between
							remove.add(item);

						}
					}
				} else if (filter.getType() == StringOperator.Type.AFTERON) {
					if (vergleich != null && !removeTime(vergleich).equals("")) {
						if (vergleich.compareTo(filter.getValue()) < 0) {
							// In between
							remove.add(item);

						}
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	/**
	 * Filtert die Categorie Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	private void filterCategorie(FilterableStringTableColumn col, ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<StringOperator> filters = col.getFilters();
		for (StringOperator filter : filters) {
			for (Entry item : newData) {
				String tableValue = item.getKategoriename();
				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.
				String vergleich = null;
				if (tableValue != null && !tableValue.equals("")) {
					vergleich = tableValue.toUpperCase();
				}
				if (filter.getType() == StringOperator.Type.EQUALS) {
					if (!filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
					if (filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.CONTAINS) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.contains(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.STARTSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.startsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.ENDSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.endsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	/**
	 * Filtert die Description Spalte
	 * 
	 * @param Column
	 * @param Items
	 */
	private void filterDescription(FilterableStringTableColumn col, ObservableList<Entry> newData) {
		// Here's an example of how you could filter the ID column
		final List<Entry> remove = new ArrayList<>();
		final ObservableList<StringOperator> filters = col.getFilters();
		for (StringOperator filter : filters) {
			for (Entry item : newData) {
				String tableValue = item.getBeschreibung();
				// Note: not all Type's are supported for each Operator.
				// Look at the validTypes() method to see what types are
				// available.
				String vergleich = null;
				if (tableValue != null && !tableValue.equals("")) {
					vergleich = tableValue.toUpperCase();
				}
				if (filter.getType() == StringOperator.Type.EQUALS) {
					if (!filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.NOTEQUALS) {
					if (filter.getValue().toUpperCase().equals(vergleich)) {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.CONTAINS) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.contains(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.STARTSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.startsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				} else if (filter.getType() == StringOperator.Type.ENDSWITH) {
					if (vergleich != null && !vergleich.equals("")) {
						if (!vergleich.endsWith(filter.getValue().toUpperCase())) {
							remove.add(item);
						}
					} else {
						remove.add(item);
					}
				}
			}
		}
		newData.removeAll(remove);
	}

	private Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}