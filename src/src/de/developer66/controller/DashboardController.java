package de.developer66.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.developer66.main.DBHelper;
import de.developer66.main.EntryCategorieCount;
import de.developer66.main.Jahresverlauf;
import de.developer66.main.MainView;
import de.developer66.util.DoughnutChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardController {
	/********** Attribut Deklarationen START **********/

	// Label f�r Name der Top Kategorie
	@FXML
	private Label label_topKategorie;

	// Label f�r Datum des letzten Eintrags
	@FXML
	private Label label_lastEntry;

	// Label f�r Anzahl aller Eintr�ge
	@FXML
	private Label label_entries;

	// Label f�r DB Gr��e
	@FXML
	private Label label_dbSize;

	// Label f�r AnzahlDerEintr�ge f�r Top Kategorie
	@FXML
	private Label label_kategorieCount;

	// DBHelper from MainView
	private DBHelper dbHelper = MainView.getDBHelper();

	// PieChart
	@FXML
	private VBox vboxDonutChart;

	private DoughnutChart doughnutChart;

	// LineChart
	@FXML
	private LineChart<String, Number> LineChart;

	// Title
	private String title = "Dashboard";

	/********** Attribut Deklarationen ENDE **********/

	// Get Title Getter
	public String getTitle() {
		return title;
	}

	/*
	 * Initialize den Controller
	 */
	@FXML
	void initialize() {
		try {
			// Lade Labels
			initLabels();
			// Lade PieChart
			initDoughnutChart();
			// Lade LineChart
			initLineChart();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * In dieser Methode werden die Labels f�r die Allegemeinen Informationen
	 * gef�llt
	 */
	void initLabels() {
		// Label f�r Anzahl aller Eintr�ge
		label_entries.setText("" + dbHelper.getEntriesRowCount());
		// Label f�r DB Gr��e
		label_dbSize.setText(dbHelper.getDBSize());
		// Label f�r Top Kategorie Anzahl
		label_kategorieCount.setText("" + dbHelper.getCategorieCount());
		// Label f�r Name der Top Kategorie
		label_topKategorie.setText(dbHelper.getTopCategorie());
		// Label f�r Letzter Eintrag
		Date lastEntrie = dbHelper.getLastEntryDate();
		if (lastEntrie == null) { // Wenn noch kein Eintrag Vorhanden wird das Label auf "-" gesetzt
			label_lastEntry.setText("-");
		} else { // Datum Vorhanden
			// DateFormatter (Pattern: "dd-MM-yyyy")
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
			// Setze Label auf formatted date
			label_lastEntry.setText(df.format(lastEntrie));
		}
	}

	/*
	 * Alle Kategorien werden eingelesen und danach in das PieChart geladen und
	 * angezeigt falls keine angezeigt werden bleibt das Chart leer
	 */
	void initDoughnutChart() {
		// New Array f�r PieChart.Data
		ObservableList<DoughnutChart.Data> doughnutChartData = FXCollections.observableArrayList();

		// Erzeuge Dougnut Chart
		doughnutChart = new DoughnutChart(doughnutChartData);
		// Setze DoughnutChart in VBox
		vboxDonutChart.getChildren().add(doughnutChart);

		// List f�r die Values
		ArrayList<EntryCategorieCount> list = dbHelper.getEntryCategorieCount();

		if (list.size() != 0) {
			// Setze Titel
			doughnutChart.setTitle("Kategorie");

			// Durchsuche Daten und f�ge sie dem PieChart hinzu
			for (EntryCategorieCount entry : list) {
				doughnutChartData.add(new PieChart.Data(entry.getName(), entry.getValue()));
			}

			// Falls DonughtChart leer ist wird "Keine Daten vorhanden" angezeigt
			// sonst werden die Daten auf das PieChart gesetzt
			doughnutChart.setData(doughnutChartData);
		} else {
			doughnutChart.setTitle("Keine Daten vorhanden");
		}
	}

	/*
	 * In dieser Methode wird ein kumulierter Verlauf aller Eintr�ge angezeigt
	 */
	void initLineChart() {
		// Definiere die Serie
		Series series = new Series();
		// Setze Name auf "Gesamt�bersicht"
		series.setName("Jahrenverlauf");

		// Lade Liste aus der DB
		ArrayList<Jahresverlauf> values = dbHelper.getEntriesHistory();

		if (values.size() != 0) {
			// intr�ge werden durchgesehen und angezeigt
			for (int i = 0; i < values.size(); i++) {
				// Erzeuge neuen Calendar
				Calendar c = Calendar.getInstance();
				// Setze calender auf Werte der DB
				c.set(values.get(i).getYear(), values.get(i).getMonth() - 1, 1);
				// Get Monthname
				String month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
				// Add Data to series
				series.getData().add(new XYChart.Data(month, values.get(i).getValue()));
			}

			LineChart.getData().add(series);
		} else {
			LineChart.setTitle("Keine Daten vorhanden");
		}
	}

	/********** Getter ***********/
	public LineChart<String, Number> getLineChart() { // Get LineChart
		return LineChart;
	}

	public DoughnutChart getDoughnutChart() { // Get PieChart
		return doughnutChart;
	}
}