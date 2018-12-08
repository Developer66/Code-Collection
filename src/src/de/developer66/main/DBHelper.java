package de.developer66.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;

/*
 *  DBHelper Class erleichtert Eingaben und
 *  Veränderungen der H2 Database
 */
public class DBHelper {
	/******* Attribut Deklarationen Start *******/

	// Connection zur DB
	private Connection c = null;

	// User Credits
	private String username;
	private String password;

	/******* Attribut Deklarationen Ende *******/

	// Konstruktor | Parameterliste : Username, Passwd. NULL lassen wenn DB keinen
	// PasswdSchutz
	public DBHelper(String username, String passwd) {
		try {
			new DBHelper();
			// DBPath (Homepath of jarfile)
			File dbfile = new File(getDBPath());

			// JDBC Treiber
			Class.forName("org.h2.Driver");
			// Stelle Verbindung her
			if (username.length() == 0 || passwd.length() == 0) {
				c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), "admin", "");

				this.username = "admin";
				password = null;
			} else {
				// Setze variablen
				this.username = username;
				password = passwd;

				c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), username, passwd);
			}
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	// Leerer Konstruktor für erzeugen des Objektes und abfragen des DBFile Path
	// ohne sich mit der Datenbank zu Verbinden
	public DBHelper() {
	}

	/*
	 * Close Connection Methode
	 */
	public void close() {
		try {
			// Schließe Datenbank und Compress it
			System.out.print("Compressing... ");
			c.createStatement().execute("SHUTDOWN COMPACT");
			System.out.print("Done\n");

			c.close();
		} catch (Exception e) {
			error("Beim schließen der Verbindung zur DB ist ein Fehler aufgetreten");
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/************ Einstellungen ************/

	/*
	 * Methode zum Verändern von Einstellungen mit Rückgabewert erfolgreich ja oder
	 * nein
	 * 
	 * name = Settingsname value = Wert
	 */
	public boolean changeSetting(String name, String value) {
		// Check auf null
		if (name.length() == 0 || value.length() == 0) {
			System.err.println("Values can't be null");
			// Values dürfen nicht leer sein
			return false;
		}
		try {
			// SQL String
			String sqlcommand;

			// Checke auf Null-Value
			if (!value.toLowerCase().equals("null")) {
				sqlcommand = "UPDATE settings SET value = '" + value + "' WHERE name = '" + name + "';";
			} else {
				sqlcommand = "UPDATE settings SET value = 'NULL' WHERE name = '" + name + "';";
			}

			// Execute command
			c.createStatement().execute(sqlcommand);

			// True für erfolgreich
			return true;
		} catch (Exception e) {
			error("Ups. Irgendetwas ist schief gegangen...");
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// False für Fehler
			return false;
		}
	}

	/*
	 * Methode zum erstellen von neuen Einstellungen mit Rückgabewert erfolgreich ja
	 * oder nein
	 * 
	 * name = Settingsname value = Wert
	 */
	public boolean insertSetting(String name, String value) {
		// Check auf null
		if (name.length() == 0 || value.length() == 0) {
			System.err.println("Values can't be null");
			// Values dürfen nicht leer sein
			return false;
		}

		try {
			// SQL String
			String sqlcommand;

			// Checke auf Null-Value
			if (value.toLowerCase().equals("null")) {
				sqlcommand = "INSERT INTO settings (name,value) VALUES (" + name + ",NULL)";
			} else {
				sqlcommand = "INSERT INTO settings (name,value) VALUES (" + name + ",'" + value + "')";
			}

			// Execute command
			c.createStatement().execute(sqlcommand);

			// True für erfolgreich
			return true;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// False für Fehler
			return false;
		}
	}

	/*
	 * Methode zum bekommen einer Settings Value
	 */
	public String getSetting(String name) {
		try {
			// SQL String
			String sqlcommand = "SELECT * FROM settings WHERE name = '" + name + "'";
			String value = null;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value = rs.getString("value");
			}
			// Return Value
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// Null für Fehler
			return null;
		}
	}

	/*
	 * Mehtode zum zurückgeben der Eingestellten Sprache
	 */
	public Locale getLanguage() {
		try {
			// SQL String
			String sqlcommand = "SELECT * FROM settings WHERE name = 'language'";
			Locale systemLanguage = null;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				systemLanguage = new Locale(
						rs.getString("value").toLowerCase() + "_" + rs.getString("value").toUpperCase(), "");
			}
			return systemLanguage;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// Null für Fehler
			return null;
		}
	}

	/************** Einträge ************/

	/*
	 * Methode zum Erstellen neuer Einträge
	 * 
	 * Erklärung:
	 * 
	 * In dieser Methode werden die Informationen die für einen neuen Eintrag
	 * benötigt werden gesammelt. Die Variablen in der Parameterliste werden
	 * nacheinander überprüft und danach summiert. Später werden diese dann in einen
	 * SQL-Befehl eingesetzt. Falls eine Variable die benötigt wird nicht stimmt
	 * wird ein Fehlertext mit dieser Information angereichert und später als Text
	 * zurückgegeben
	 * 
	 * Benötigte Felder: - Title - kategorie - Code
	 */
	public Pair<Integer, String> createNewEntry(Entry entry) {
		// SQL Rows & Columns
		String values = "";
		String columns = "";
		// Fehler Vaiablen
		String fehlertext = "";
		int fehler = 0;

		// Set Created Value
		columns = columns + "created,";
		values = values + "CURRENT_TIMESTAMP(),";

		// Set Modified Value
		columns = columns + "modified,";
		values = values + "CURRENT_TIMESTAMP(),";

		// Set Role Value
		columns = columns + "role,";
		values = values + "'" + getUsername().replaceAll("'", "''") + "',";

		// Überprüfe Titel
		if (entry.getTitle().length() != 0) {
			// Summiere Variablen
			columns = columns + "title,";
			values = values + "'" + entry.getTitle().replace("'", "''") + "',";
		} else {
			// Zähle Fehler +1
			fehler++;
			fehlertext = fehlertext + "Titel, ";
		}

		// Überprüfe Beschreibung
		if (entry.getBeschreibung().length() != 0) {
			// Summiere Variablen
			columns = columns + "describtion,";
			values = values + "'" + entry.getBeschreibung().replace("'", "''") + "',";
		} else {
			// Summiere Variablen
			columns = columns + "describtion,";
			values = values + "NULL,";
		}

		// Überprüfe kategorie
		if (entry.getKategorie() > -1) {
			// Summiere Variablen
			columns = columns + "categorie,";
			values = values + "'" + entry.getKategorie() + "',";
		} else {
			// Zähle Fehler +1
			fehler++;
			fehlertext = fehlertext + "Kategorie, ";
		}

		// Überprüfe code
		if (entry.getCode().length() != 0) {
			// Summiere Variablen
			columns = columns + "code,";
			values = values + "'" + entry.getCode().replace("'", "''") + "',";
		} else {
			// Zähle Fehler +1
			fehler++;
			fehlertext = fehlertext + "Code, ";
		}

		// Setze den SQL String zusammen
		if (fehler == 0) {
			// Lösche die Letzten Zeichen der Vaiablen
			columns = columns.substring(0, columns.length() - 1);
			values = values.substring(0, values.length() - 1);

			// SQL Befehl
			String sql = "INSERT INTO entries (" + columns + ") VALUES (" + values + ");";

			try {
				// New Statement
				Statement statement = c.createStatement();

				// Execute command
				c.createStatement().execute(sql);

				// Var for recived id
				int id = 0;
				// ResultSet
				ResultSet rs = statement.executeQuery("SELECT SCOPE_IDENTITY() AS last_id;");
				// get ID
				while (rs.next()) {
					id = rs.getInt("last_id");
				}

				// True new Pair with ID and text
				return new Pair<Integer, String>(id, null);
			} catch (Exception e) {
				// Error Meldung
				System.err.println("Ups. Irgendetwas ist schief gegangen...");
				System.err.println(e.getClass().getName() + ": " + e.getMessage());

				// Show Error Screen
				new Error(
						"Tut uns leid... Dies hätte nicht passieren dürfen. Bitte überprüfen Sie ihre Eingabe und versuchen Sie es erneut");

				// False für Fehler
				return null;
			}
		} else {
			// Return Fehlertext - letzten 2 Zeichen (Letzte zwei Zeichen: ", ")
			return new Pair<Integer, String>(-1, fehlertext.substring(0, fehlertext.length() - 2));
		}
	}

	/*
	 * Methode zum überschreiben eines neuen Eintrags
	 * 
	 * SQL-Command:
	 * 
	 * UPDATE ENTRIES SET (ID,MODIFIED,TITLE,DESCRIBTION,LANGUAGE,CATEGORIE,CODE) =
	 * ([ID],now(),'[TITLE]','[BESCHREIBUNG]',[LANGUAGE],[KATEGORIE],'[CODE]') WHERE
	 * id = [ID];
	 */
	public String saveEntry(Entry entry) {
		// SQL Rows & Columns
		String values = "";
		String columns = "";
		// Fehler Vaiablen
		String fehlertext = "";
		int fehler = 0;

		// Set Modified Value
		columns = columns + "modified,";
		values = values + "CURRENT_TIMESTAMP(),";

		// Überprüfe Titel
		if (entry.getTitle() != null && entry.getTitle().length() != 0) {
			// Summiere Variablen
			columns = columns + "title,";
			values = values + "'" + entry.getTitle().replace("'", "''") + "',";
		} else {
			// Zähle Fehler +1
			fehler++;
			fehlertext = fehlertext + "Titel, ";
		}

		// Überprüfe Beschreibung
		if (entry.getBeschreibung() != null && entry.getBeschreibung().length() != 0) {
			// Summiere Variablen
			columns = columns + "describtion,";
			values = values + "'" + entry.getBeschreibung().replace("'", "''") + "',";
		} else {
			// Summiere Variablen
			columns = columns + "describtion,";
			values = values + "NULL,";
		}

		// Überprüfe kategorie
		if (entry.getKategorie() > -1) {
			// Summiere Variablen
			columns = columns + "categorie,";
			values = values + "'" + entry.getKategorie() + "',";
		} else {
			// Zähle Fehler +1
			fehler++;
			fehlertext = fehlertext + "Kategorie, ";
		}

		// Überprüfe code
		if (entry.getCode() != null && entry.getCode().length() != 0) {
			// Summiere Variablen
			columns = columns + "code,";
			values = values + "'" + entry.getCode().replace("'", "''") + "',";
		} else {
			// Zähle Fehler +1
			fehler++;
			fehlertext = fehlertext + "Code, ";
		}

		// Setze den SQL String zusammen
		if (fehler == 0) {
			// Lösche die Letzten Zeichen der Vaiablen
			columns = columns.substring(0, columns.length() - 1);
			values = values.substring(0, values.length() - 1);

			// SQL Befehl
			String sql = "UPDATE ENTRIES SET (" + columns + ") = (" + values + ") WHERE id = " + entry.getId();

			try {
				// Execute command + Return id
				c.createStatement().executeUpdate(sql);

				// Return null for Done
				return null;
			} catch (Exception e) {
				// Error Meldung
				System.err.println("Ups. Irgendetwas ist schief gegangen...");
				System.err.println(e.getClass().getName() + ": " + e.getMessage());

				// Show Error Screen
				new Error(
						"Tut uns leid... Dies hätte nicht passieren dürfen. Bitte überprüfen Sie ihre Eingabe und versuchen Sie es erneut");

				// null für Fehler
				return "Error occured";
			}
		} else {
			// Return Fehlertext - letzten 2 Zeichen (Letzte zwei Zeichen: ", ")
			return fehlertext.substring(0, fehlertext.length() - 2);
		}
	}

	/*
	 * Methode zum Abrufen eines Kompletten Eintrages benötigt im entryController.
	 * 
	 * Parameterliste EntryID
	 */
	public Entry getEntry(int id) {
		Entry entry = null;
		try {
			// SQL String
			String sqlcommand = "SELECT * FROM ENTRIES WHERE id = " + id;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				entry = new Entry(rs.getInt("id"), rs.getDate("created"), rs.getDate("modified"), rs.getString("title"),
						rs.getInt("categorie"), null, 0, rs.getString("describtion"), rs.getString("code"),
						getFiles(id).size());
			}

			return entry;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			new Error(e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/**
	 * Methode zum Löschen eines Eintrags
	 * 
	 */
	public void deleteEntry(int id) {
		try {
			// SQL String
			String sqlcommand = "DELETE FROM entries WHERE id = " + id;

			// Execute command
			c.createStatement().execute(sqlcommand);
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			new Error(e.getMessage());
		}
	}

	/*
	 * Methode zum abrufen des Anzahl von Einträgen
	 * 
	 * Über die SQL Abfrage Count wir die Gesamtzahl der Eintrage summiert und
	 * zurückgegeben
	 */
	public int getEntriesRowCount() {
		try {
			// SQL String
			String sqlcommand = "SELECT Count(ID) AS count FROM ENTRIES";
			int value = 0;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value = rs.getInt("count");
			}
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// -1 für Fehler
			return -1;
		}
	}

	/*
	 * Methode zum Abrufen des Letzen Eintrags (Datum)
	 * 
	 * Über die SQL Funktion Max wird hier das größte Datumzurückgegeben
	 */
	public Date getLastEntryDate() {
		try {
			// SQL String
			String sqlcommand = "SELECT max(created) AS date FROM ENTRIES";
			Date value = null;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value = rs.getDate("date");
			}
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// -1 für Fehler
			return null;
		}
	}

	/*
	 * Methode für EintragsHistory
	 * 
	 * In dieser Methode werden alle Einträge summiert und Monaten zugeordnet. Diese
	 * werden dann kumuliert und in der Klasse "MonthValuesEntries" als ArrayList
	 * abgelegt um diese im Dashboard als Grafik darzustellen.
	 * 
	 * SQL-Befehle: - Erster Eintrag: SELECT month(created) AS month,
	 * monthname(created) as monthname, year(created) AS year, COUNT(id) AS entries
	 * FROM ENTRIES GROUP BY month, year, monthname ORDER BY year, month LIMIT 1
	 * 
	 * - Letzter Eintrag: SELECT month(created) AS month, monthname(created) as
	 * monthname, year(created) AS year, COUNT(id) AS entries FROM ENTRIES GROUP BY
	 * month, year, monthname ORDER BY year DESC, month DESC LIMIT 1
	 * 
	 * - Alle Abgeufen: SELECT month(created) AS month, monthname(created) as
	 * monthname, year(created) AS year, COUNT(id) AS entries FROM ENTRIES GROUP BY
	 * month, year, monthname ORDER BY year, month
	 * 
	 */
	public ArrayList<Jahresverlauf> getEntriesHistory() {
		try {
			// Liste mit allen Monatswerten
			ArrayList<Jahresverlauf> list = new ArrayList<>();

			// ResultSet
			ResultSet rs = c.createStatement().executeQuery(
					"SELECT month(created) as month, year(created) AS year, COUNT(id) AS entries FROM ENTRIES GROUP BY month,year LIMIT 12");
			while (rs.next()) {
				list.add(new Jahresverlauf(rs.getInt("month"), rs.getInt("year"), rs.getInt("entries")));
			}

			// Liste wird zurückgegeben
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/************ Kategorie ************/

	/*
	 * Methode zum anlegen neuer Kategorien. Als erstes wird geprüft ob bereits eine
	 * Kategorie mit eingegebenen Namen existiert
	 * 
	 * SQL-Befehl: - Checke ob es Name schon gibt: SELECT * FROM categories WHERE
	 * name = '[NAME]' - Insert Command INSERT INTO categories
	 * (name,created,modified,describtion) VALUES
	 * ('[NAME]',now(),now(),'[DESCRIBTION]')
	 */
	public String createNewCategorie(String name, String describtion) {
		try {
			// SQL String
			String checkcommand = "SELECT * FROM categories WHERE name = '" + name + "'";
			// Variable für gefundene Rows
			int rowsFound = 0;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(checkcommand);
			// Count Rows
			while (rs.next()) {
				rowsFound++;
			}

			// Wenn Name schon existiert wird ein Fehler zurückgegeben
			if (rowsFound != 0) {
				return "Eine Kategorie mit gleichen Namen existiert bereits";
			} else { // Name existiert nicht
				// SQL Command
				String insercommand = "INSERT INTO categories (name,created,modified,describtion) VALUES ('"
						+ name.replace("'", "\"") + "',now(),now(),'" + describtion.replace("'", "\"") + "')";
				// Execute Command
				c.createStatement().execute(insercommand);
				// Return "" for succesfull
				return "";
			}
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// Error
			return "Fehler aufgetreten... Message: " + e.getMessage();
		}
	}

	/*
	 * Edit Categorie Methode
	 * 
	 * In dieser Methode wird eine Kategorie Editiert...
	 * 
	 * SQL-Befehl: UPDATE categories SET (name,describtion,modified) =
	 * ('[NAME]','[BESCHREIBUNG]',now()) WHERE name = '[NAME]'
	 * 
	 */
	public void editCategorie(int id, String name, String describtion) {
		try {
			// SQL String
			String sqlcommand = "UPDATE categories SET (name,describtion,modified) = ('" + name.replace("'", "\"")
					+ "','" + describtion.replace("'", "\"") + "',now()) WHERE id = " + id;

			// Execute command with ResultSet
			c.createStatement().executeUpdate(sqlcommand);
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/*
	 * Methode zum löschen von Kategorien. Alle Einträge werden auf Kategorie = 1
	 * gesetzt ID 1 ist die Default Kategorie die immer vorhanden ist und nicht
	 * gelöscht werden kann
	 * 
	 * SQL-Befehl: DELETE FROM categories WHERE name = '[NAME]'
	 */
	public void deleteCategorie(int id) {
		try {
			// SQL String
			String sqlcommand = "DELETE FROM categories WHERE id = " + id;
			// Update SQL
			String updatecommand = "UPDATE entries SET categorie = " + 1 + " WHERE categorie = " + id;

			// Execute command
			c.createStatement().executeUpdate(sqlcommand);
			// Execute command
			c.createStatement().executeUpdate(updatecommand);
		} catch (Exception e) {
			// Error Meldung
			new Error(e.getMessage());
		}
	}

	public String getCategorieName(int id) {
		try {
			// SQL String
			String sqlcommand = "SELECT * FROM Categories WHERE id = " + id;
			String value = "";

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value = rs.getString("name");
			}
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/*
	 * Methode zum Abrufen der Kategorien
	 * 
	 * Daten werden von der Datenbank eingelesen und in einer ArrayList gespeichert
	 * um später wieder ausgelesen werden zukönnen
	 * 
	 */
	public ArrayList<String> getCategorieNames() {
		try {
			// SQL String
			String sqlcommand = "SELECT * FROM Categories";
			ArrayList<String> value = new ArrayList<>();

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value.add(rs.getString("name"));
			}
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/*
	 * Methode zum Abrufen der Kategorien
	 * 
	 * Daten werden von der Datenbank eingelesen und in einer ArrayList gespeichert
	 * um später wieder ausgelesen werden zukönnen
	 * 
	 */
	public int getCategorieCount() {
		try {
			// SQL String
			String sqlcommand = "SELECT Count(id) AS count FROM Categories";
			int value = 0;

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value = rs.getInt("count");
			}
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return -1;
		}
	}

	/*
	 * Methode zum Abrufen des Top Kategorie die für die Anzeige im Dashboard
	 * gebraucht wird
	 * 
	 * SQL-Befehl: SELECT COUNT(ENTRIES.ID) AS count, CATEGORIE AS categorieID,
	 * CATEGORIES.NAME FROM entries LEFT JOIN CATEGORIES ON ENTRIES.CATEGORIE =
	 * CATEGORIES.ID GROUP BY CATEGORIEID ORDER BY count DESC LIMIT 1
	 */
	public String getTopCategorie() {
		try {
			// SQL String
			String sqlcommand = "SELECT COUNT(ENTRIES.ID) AS count, CATEGORIE AS categorieID, CATEGORIES.NAME FROM entries LEFT JOIN CATEGORIES ON ENTRIES.CATEGORIE = CATEGORIES.ID GROUP BY CATEGORIEID ORDER BY count DESC LIMIT 1";

			// Name für Top-Categorie
			String catName = "";

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				catName = rs.getString("name");
			}

			return catName;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/*
	 * Methode zum Abrufen wie viele Eintrage einer Kategorie angehören (PieChart)
	 */
	public ArrayList<EntryCategorieCount> getEntryCategorieCount() {
		try {
			/*
			 * SQL Command:
			 * 
			 * SELECT COUNT(ENTRIES.ID) AS COUNT, CATEGORIE AS CATEGORIEID, CATEGORIES.NAME
			 * FROM ENTRIES LEFT JOIN CATEGORIES ON ENTRIES.CATEGORIE = CATEGORIES.ID GROUP
			 * BY CATEGORIEID
			 *
			 */

			// SQL String
			String sqlcommand = "SELECT COUNT(ENTRIES.ID) AS COUNT, CATEGORIE AS CATEGORIEID, CATEGORIES.NAME FROM ENTRIES LEFT JOIN CATEGORIES ON ENTRIES.CATEGORIE = CATEGORIES.ID GROUP BY CATEGORIEID ";
			ArrayList<EntryCategorieCount> value = new ArrayList<>();

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value.add(new EntryCategorieCount(rs.getString("name"), rs.getLong("count")));
			}

			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/*
	 * Methode zum Abrufen aller Information des Tabels Kategorie um diese dann in
	 * der KategorieVerwaltung zu verwenden
	 */
	public ArrayList<Kategorie> getFullCategorieInformations() {
		try {
			// SQL String
			String sqlcommand = "SELECT * FROM Categories";
			ArrayList<Kategorie> value = new ArrayList<>();

			// Execute command with ResultSet
			ResultSet rs = c.createStatement().executeQuery(sqlcommand);
			// Get Value
			while (rs.next()) {
				value.add(new Kategorie(rs.getInt("id"), rs.getString("name"), rs.getString("describtion"),
						rs.getDate("created"), rs.getDate("modified")));
			}
			return value;
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			// null für Fehler
			return null;
		}
	}

	/************ ProgrammingLanguage ************/

	/**
	 * Methode gibt die Programmiersprache anhand der Id als Name zurück
	 * 
	 * @param LanguageID
	 * @return Programmiersprache in Textform
	 */
	// TODO Implement Methode
	public String getProgrammingLanguage(int id) {
		return "";
	}

	/*************** FILES GET | SET ***************/

	/*
	 * Methode zum Abrufen der Files mit eingegebenen ID
	 * 
	 * SQL-Command: SELECT id, name, size FROM FILES WHERE entries_id = [ID]
	 */
	public ObservableList<EintragFiles> getFiles(int id) {
		try {
			// SQL Command to get all Files with custom ID
			String sql = "SELECT id,name,size FROM FILES WHERE entries_id = " + id;

			// Arraylist für Files
			ObservableList<EintragFiles> files = FXCollections.observableArrayList();

			// ResultSet
			ResultSet rs = c.createStatement().executeQuery(sql);

			// Get Files
			while (rs.next()) {
				files.add(new EintragFiles(rs.getInt("id"), rs.getString("name"), rs.getString("size")));
			}

			// Return List
			return files;
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			new Error(e.getMessage());
		}

		return null;
	}

	/*
	 * Methode zum Uploaden eines neuen Files
	 * 
	 * SQL-Command:
	 * 
	 * INSERT INTO FILES (name, size, entries_id, filecontent) VALUES ('[NAME]',
	 * '[SIZE]', '[ID]', ?);
	 */
	public void uploadFile(File file, long size, int eintragId) {
		try {
			System.out.println("Loading File up to Database...");

			// File Inputstream
			FileInputStream fileInputStream = null;
			// Set File to InputStream
			fileInputStream = new FileInputStream(file);

			// SQL Command
			String sql = "INSERT INTO FILES (name, size, entries_id, filecontent) VALUES ('" + file.getName() + "','"
					+ size + "','" + eintragId + "', ? );";

			// Prepared Statement
			PreparedStatement ps = c.prepareStatement(sql);

			// Insert Filecontent
			ps.setBinaryStream(1, fileInputStream, (int) file.length());

			// Execute SQL
			ps.executeUpdate();

			// Close PreparedStatement & fileInputStream
			ps.close();
			fileInputStream.close();

			System.out.println("File written successfully");
		} catch (Exception e) {
			new Error(e.getMessage());
		}
	}

	/*
	 * Methode zum Abrufen eines Files
	 */
	public File getFile(int id, boolean showSaveDialog) {
		try {
			// SQL Command
			String query = "SELECT name, filecontent, LENGTH(filecontent) FROM FILES WHERE id = " + id;

			// File
			File file = null;

			// Statement
			PreparedStatement statement = c.prepareStatement(query);

			// ResultSet
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				// Wenn FileSaveDialog gewünscht ist wird dieser nun angezeigt
				if (showSaveDialog == true) {
					// New FileChooser
					FileChooser fc = new FileChooser();
					// Set InitialFileName to Result Name
					fc.setInitialFileName(result.getString("name"));
					// Ad Extension Filter
					fc.getExtensionFilters().add(new ExtensionFilter("All", "*.*"));
					// Show Save Dialog
					file = fc.showSaveDialog(null);
				} else {
					// Default File in OS Temp
					file = new File(System.getProperty("java.io.tmpdir") + result.getString("name"));
				}

				// Wenn File ausgewählt wurde
				if (file != null) {
					// Get Blob from DB
					Blob blob = result.getBlob("filecontent");
					// Set InputStream
					InputStream inputStream = blob.getBinaryStream();
					// Set OutputStream
					OutputStream outputStream = new FileOutputStream(file.getAbsolutePath());

					// ByteRead
					int bytesRead = -1;
					// ByteBuffer
					byte[] buffer = new byte[4096];
					// Reade & Write Byted
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					// Close Streams
					inputStream.close();
					outputStream.close();
				}
			}

			// Return File
			return file;
		} catch (Exception e) {
			// Show Error message
			new Error(e.getMessage());
			// Return null for Error
			return null;
		}
	}

	/*
	 * Methode zum Abrufen eines Files und speichern in einem Custom Verzeichnis
	 */
	public File writeFileToCustomDirecory(File direcory, int ID) {
		try {
			// SQL Command
			String query = "SELECT name, filecontent, LENGTH(filecontent) FROM FILES WHERE id = " + ID;

			// File
			File file = null;

			// Statement
			PreparedStatement statement = c.prepareStatement(query);

			// ResultSet
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				file = new File(direcory.getAbsolutePath() + File.separator + result.getString("name"));

				// Wenn File ausgewählt wurde
				if (file != null) {
					// Get Blob from DB
					Blob blob = result.getBlob("filecontent");
					// Set InputStream
					InputStream inputStream = blob.getBinaryStream();
					// Set OutputStream
					OutputStream outputStream = new FileOutputStream(file.getAbsolutePath());

					// ByteRead
					int bytesRead = -1;
					// ByteBuffer
					byte[] buffer = new byte[4096];
					// Reade & Write Byted
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					// Close Streams
					inputStream.close();
					outputStream.close();
				}
			}

			// Return
			return file;
		} catch (Exception e) {
			// Show Error message
			new Error(e.getMessage());
			// Return null
			return null;
		}
	}

	/*
	 * Methode zum löschen von einer Datei
	 * 
	 * SQL-COMMAND: DELTE FROM FILES WHERE id = [ID];
	 */
	public void deleteFile(int id) {
		try {
			// SQL String
			String sqlcommand = "DELETE FROM FILES WHERE id = " + id;

			// Execute command
			c.createStatement().executeUpdate(sqlcommand);
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			new Error(e.getMessage());
		}
	}

	/*
	 * Methode zum Uploaden eines neuen UserImages Die ID in der Settings DB wird
	 * gewechselt und der File wird hochgeladen Parameterliste: Bufferimage
	 */
	public void uploadUserImage(File userimage) {
		try {
			FileInputStream fileInputStream = null;
			// Set File to InputStream
			fileInputStream = new FileInputStream(userimage);

			// Delete Old Image
			String deletSQL = "DELETE FROM FILES WHERE name = 'userimage.jpg' AND entries_id = NULL";
			// Execute Command
			c.createStatement().execute(deletSQL);

			// Inser empty ROW to get new ID
			String tempRow = "INSERT INTO FILES () VALUES ()";
			// New Statement
			Statement statement = c.createStatement();

			// Execute command + Return id
			c.createStatement().executeUpdate(tempRow, Statement.RETURN_GENERATED_KEYS);

			// Var for recived id
			int id = 0;
			// ResultSet
			ResultSet rs = statement.getGeneratedKeys();
			// get ID
			while (rs.next()) {
				id = rs.getInt(1);
			}

			// SQL Command
			String sql = "UPDATE FILES SET (id, name, size, entries_id, filecontent) = (" + id + ",'userimage.jpg','"
					+ userimage.length() + "',NULL, ? );";

			// Prepared Statement
			PreparedStatement ps = c.prepareStatement(sql);

			// Insert Filecontent
			ps.setBinaryStream(1, fileInputStream, (int) userimage.length());

			// Execute SQL
			ps.executeUpdate();

			// Close PreparedStatement & fileInputStream
			ps.close();
			fileInputStream.close();

			// Update Userimage ID in Settings DB
			changeSetting("userimage", "" + id);
		} catch (Exception e) {
			new Error(e.getMessage());
		}
	}

	/*************** VERSION | UPDATE ***************/

	/*
	 * Methode zum Abrufen der Datenbank Version
	 * 
	 * Über die Methode get Setting wird die Versions Nummer zurückgegeben
	 */
	public String getDBVersion() {
		return getSetting("version_nr");
	}

	/*
	 * Diese Methode wird am Start durchgelaufen und überprüft, ob ein Unterschied
	 * zwischen DB-Version und Jar-Version besteht. Wenn ja dann wir die DB
	 * upgedated
	 */
	public Task<Void> onUpdate(Stage stage) {
		// New Dialog
		Dialog<Void> dialog = new Dialog<>();
		// Add Cancel Button
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

		int JarVersion = MainView.getJarVersion();
		int DBVersion = 0;
		try {
			DBVersion = Integer.parseInt(getDBVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("DBVersion: " + DBVersion);
		System.out.println("JarVersion: " + JarVersion);

		Task<Void> updateThread = null;

		if (DBVersion == JarVersion) {
			System.out.println("Jar und DB auf gleicher version");
			new MainView(stage);
		} else {
			System.out.println("Ein Update der DB muss ausgeführt werden.");

			// Task
			updateThread = new Task<Void>() {
				@Override
				public Void call() {
					// Update Message
					updateMessage("Version Update");

					int i = 0;
					while (i < 100) {
						i++;
						updateProgress(i, 100);
						updateMessage("Version Update " + i + "%");
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					return null;
				}
			};

			// ProgressBar
			ProgressBar pBar = new ProgressBar();
			// Load Value from Task
			pBar.progressProperty().bind(updateThread.progressProperty());
			// Set PrefWidth
			pBar.setPrefWidth(250);
			// New Loading Label
			Label statusLabel = new Label();
			// Get Text
			statusLabel.textProperty().bind(updateThread.messageProperty());
			// Set Style to Label
			statusLabel.setStyle("-fx-font: 24 arial;");

			// Layout
			VBox root = new VBox(statusLabel, pBar);

			// SetFill Width TRUE
			root.setFillWidth(true);
			// Center Items
			root.setAlignment(Pos.CENTER);

			// Set View to root
			dialog.getDialogPane().setContent(root);

			// Show Dialog
			dialog.show();

			updateThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					dialog.close();
					// Starte MainWindow
					new MainView(stage);
				}
			});

			// Start Thread
			new Thread(updateThread).start();
		}
		return updateThread;
	}

	/*************** GETTER & SETTER ***************/
	// GetUsername
	public String getUsername() {
		return username;
	}

	// GetPasswd
	public String getPasswd() {
		return password;
	}

	// Get Connection
	public Connection getConnection() {
		return c;
	}

	// Get File Size
	public String getFileSize(long size) {
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	// Get DB filepath
	public String getDBPath() {
		/*************** Jar Check ***************/
		String jarpath = System.getProperty("user.dir") + System.getProperty("file.separator");
		File jarfile = new File(jarpath);

		/*************** DB Check ***************/
		String dbpath = jarfile.getAbsolutePath();
		File dbfile = new File(dbpath + System.getProperty("file.separator") + "codecollection");

		return dbfile.getAbsolutePath();
	}

	// Get DB SIze
	public String getDBSize() {
		long size = new File(getDBPath() + ".mv.db").length();
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	// Methode zum Aufruf des Errors
	void error(String message) {
		new Error(message);
	}
}