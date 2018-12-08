package de.developer66.installer;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.developer66.main.DBHelper;

public class DBCreator {

	/******* Attribut Deklarationen Start *******/

	// Connection zur DB
	Connection c = null;
	// Statement
	Statement stmt = null;

	/******* Attribut Deklarationen Ende *******/

	// Konstruktor
	public DBCreator() {
	}

	// Erzeuge Datenbank
	// Default user = admin
	public void createDatabase(String user, String passwd) {
		// Versuche Datenbank zuerstellen
		try {
			// JDBC Treiber
			Class.forName("org.h2.Driver");

			// DBPath (Homepath of jarfile)
			File dbfile = new File(new DBHelper().getDBPath());

			// Checke ob Password vorhanden ist wenn ja dann wird DB verschlüsselt
			if (passwd == null) {
				// Erzeuge DB ohne PW
				c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), "admin", "");
			} else {
				// Erzeuge DB mit PW und eigenem User
				c = DriverManager.getConnection("jdbc:h2:" + dbfile.getAbsolutePath(), user, passwd);
			}

			// Erfolgreich Meldung
			System.out.println("Datenbank wurde erzeugt\n");
			// Create Statement
			stmt = c.createStatement();

			// Create SettingTable
		} catch (Exception e) {
			// Error Meldung
			System.err.println("Ups. Irgendetwas ist schief gegangen...");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/*
	 * Erzeuge SettingsTable Methode Parameterliste: - Locale für Systemsprache
	 */
	public void createSettingsTable(Locale systemLanguage) {
		try {
			/*
			 * Erzeuge Settings Table Aufbau:
			 * +-------------------+------------------------------------------------+ | name
			 * | value |
			 * +-------------------+------------------------------------------------+ |
			 * version | versionsNr | | release_date | Release Date of this version | |
			 * language | current Language | | installation_date | date of first
			 * installation | | update_check | automatic | manual updatecheck on GitHub | |
			 * db_type | Type of Database (PostgreSQL | MySQL | Intern) | | skip_welcome |
			 * Boolean if Welcome is skiped or not | | userimage | File ID for UserImage |
			 * +-------------------+------------------------------------------------+
			 */

			/*
			 * Creating Table mit folgendem Code: DROP TABLE settings IF EXISTS; CREATE
			 * TABLE settings ( name text, value text );
			 */
			// Execute Befehl DROP & CREATE
			stmt.execute("DROP TABLE IF EXISTS SETTINGS;");
			stmt.execute("CREATE TABLE settings (name text, value text)");

			// Date heute
			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

			// Belege Tabele mit Informationen die oben im Aufbau stehen
			String[] queries = { "INSERT INTO settings (name,value) VALUES ('version_nr',NULL)",
					"INSERT INTO settings (name,value) VALUES ('release_date',NULL)",
					"INSERT INTO settings (name,value) VALUES ('language','" + systemLanguage.getLanguage() + "')",
					"INSERT INTO settings (name,value) VALUES ('installation_date','" + df.format(date) + "')",
					"INSERT INTO settings (name,value) VALUES ('update_check','auto')",
					"INSERT INTO settings (name,value) VALUES ('db_type','Intern')",
					"INSERT INTO settings (name,value) VALUES ('skip_welcome','false')",
					"INSERT INTO settings (name,value) VALUES ('userimage',NULL)" };

			// Schreibe Queries
			for (String command : queries) {
				try {
					stmt.execute(command);
				} catch (Exception e) {
					System.err.println("Error on string: " + command);
					System.err.println("Grund: " + e.getMessage());
				}
			}
			// Erfolgs Bestätigung
			System.out.println("Settings Table wurde erfolgreich erstellt und mit Informationen befüllt");
		} catch (Exception e) {
			System.err.println("Irgendwas ist schiefgegangen.");
			System.err.println("Grund:" + e.getMessage());
		}
	}

	// Methode zum erzeugen des EntriesTable
	public void createEntriesTable() {
		try {
			/*
			 * Table mit Einträgen:
			 * 
			 * EntriesTable. Aufbau:
			 * 
			 * +-------------+------------------+--------------------+-------+--------------
			 * +-----------------+-----------+------+----------+ | id | created | modified |
			 * title | describtion | language | categorie | code | role |
			 * +-------------+------------------+--------------------+-------+--------------
			 * +-----------------+-----------+------+----------+ | Eintrags ID |
			 * Erstellungsdatum | Letzte Veränderung | Titel | Beschreibung |
			 * Programmsprache | Kategorie | Code | Benutzer |
			 * +-------------+------------------+--------------------+-------+--------------
			 * +-----------------+-----------+------+----------+
			 */

			/*
			 * Creating Table mit folgendem Code: CREATE TABLE IF NOT EXISTS entries ( id
			 * BIGINT auto_increment, created TIMESTAMP, modified TIMESTAMP, title TEXT,
			 * describtion TEXT, language INT, categorie INT, code TEXT, role TEXT );
			 */
			// Execute Befehl CREATE
			stmt.execute(
					"CREATE TABLE IF NOT EXISTS entries ( id BIGINT auto_increment, created TIMESTAMP, modified TIMESTAMP, title TEXT, describtion TEXT, language INT, categorie INT, code TEXT, role TEXT);");

			// Erfolgs Bestätigung
			System.out.println("Entries Table wurde erfolgreich erstellt und mit Informationen befüllt");
		} catch (Exception e) {
			System.err.println("Irgendwas ist schiefgegangen.");
			System.err.println("Grund:" + e.getMessage());
			new Error(e.getMessage());
		}
	}

	// Methode zum erstellen des KategorieTable
	public void createCategoriesTable() {
		/*
		 * Table Aufbau:
		 * 
		 * +----+----------------+------------------+----------------+---------------+ |
		 * id | name | created | modified | descritbion |
		 * +----+----------------+------------------+----------------+---------------+ |
		 * ID | Kategorie Name | Erstellungsdatum | Verändert Date | Describtion |
		 * +----+----------------+------------------+----------------+---------------+
		 *
		 */
		try {

			/*
			 * Creating Table mit folgendem Code: CREATE TABLE IF NOT EXISTS categories ( id
			 * BIGINT auto_increment, name TEXT, created TIMESTAMP, modified TIMESTAMP,
			 * describtion TEXT );
			 *
			 * Insert Default Kategorie SQL-Command: INSERT INTO categories
			 * (name,created,modified,describtion) VALUES ('Default',now(),now(),'Default
			 * Kategorie')
			 */
			// Execute Befehl CREATE
			stmt.execute(
					"CREATE TABLE IF NOT EXISTS categories ( id BIGINT auto_increment, name TEXT, created TIMESTAMP, modified TIMESTAMP, describtion TEXT );");
			// Insert default Entry
			stmt.execute(
					"INSERT INTO categories (name,created,modified,describtion) VALUES ('Default',now(),now(),'Default Kategorie')");

			// Erfolgs Bestätigung
			System.out.println("Kategorie Table wurde erfolgreich erstellt und mit Informationen befüllt");
		} catch (Exception e) {
			System.err.println("Irgendwas ist schiefgegangen.");
			System.err.println("Grund:" + e.getMessage());
		}
	}

	// Methode zum erstellen des FileTable
	public void createFileTable() {
		/*
		 * Table Struktur:
		 * 
		 * +----+----------+-----------------+-------------+-------+ | ID | NAME |
		 * ENTRIES_ID | FILECONTENT | SIZE |
		 * +----+----------+-----------------+-------------+-------+ | Id | Filename |
		 * Id des Eintrags | Fileinhalt | Größe |
		 * +----+----------+-----------------+-------------+-------+
		 */

		try {
			/*
			 * Creating Table mit folgendem Code: CREATE TABLE IF NOT EXISTS files ( id
			 * BIGINT auto_increment, name TEXT, entries_id integer, filecontent bytea, size
			 * BIGINT );
			 */
			// Execute Befehl CREATE
			stmt.execute(
					"CREATE TABLE FILES ( id bigint auto_increment, name text, entries_id integer, filecontent bytea, size BIGINT );");

			// Erfolgs Bestätigung
			System.out.println("File Table wurde erfolgreich erstellt und mit Informationen befüllt");
		} catch (Exception e) {
			System.err.println("Irgendwas ist schiefgegangen.");
			System.err.println("Grund:" + e.getMessage());
		}
	}

	// Methode zum schließen der Connection & Statement
	public void close() {
		try {
			c.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}