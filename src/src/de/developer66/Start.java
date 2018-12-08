package de.developer66;

import java.io.File;
import java.util.Locale;

import de.developer66.installer.Installer;
import de.developer66.main.DBHelper;
import de.developer66.main.Login;
import javafx.application.Application;
import javafx.stage.Stage;

public class Start extends Application {
	/*************************************************************************
	 * @author Developer66
	 *************************************************************************/

	public static void main(String[] args) {
		// Launch Application
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		/*************** Welcome ***************/
		System.out.println("  CodeCollection");
		System.out.println("Made by Developer66");

		/*************** DB Check ***************/
		String dbpath = new DBHelper().getDBPath();
		File dbfile = new File(dbpath + ".mv.db");
		System.out.println("\nDB-Filepath: " + dbfile.getAbsolutePath() + "\n");

		/*************** Sprachen Check ***************/
		Locale locale = Locale.getDefault();
		System.out.println("Länderkennung: " + locale);

		// Installer wird aufgerufen
		if (!dbfile.exists()) {
			// Konsolen Log das System nicht initialisiert wurde
			System.out.println("Es wurde keine vorhandene Datenbank gefunden.");
			System.out.println("Installer wird aufgerufen um System zu initialisieren");

			// Starte Installer
			new Installer();
		} else {
			// DB wurde gefunden
			System.out.println("\nDB was found");

			// Starte Login
			new Login().start(primaryStage);
		}
	}
}
