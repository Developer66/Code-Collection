package de.developer66.util;

import java.util.Date;

import de.developer66.main.MainView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/*
 * In dieser Klasse wir ein kompletter Eintrag gespeichert
 */
public class Entry {
	private SimpleIntegerProperty id;
	private SimpleObjectProperty<Date> created = null;
	private SimpleObjectProperty<Date> modified = null;
	private SimpleStringProperty title;
	private SimpleIntegerProperty kategorie;
	private SimpleStringProperty kategoriename;
	private SimpleIntegerProperty language;
	private SimpleStringProperty beschreibung;
	private SimpleStringProperty code;
	private SimpleIntegerProperty files;

	// Konstruktor
	public Entry(int ID, Date Erstellt, Date Veraendert, String Title, int Kategorie, String kategoriename,
			int Language, String Beschreibung, String Code, int files) {
		this.id = new SimpleIntegerProperty(ID);
		this.created = new SimpleObjectProperty<Date>(Erstellt);
		this.modified = new SimpleObjectProperty<Date>(Veraendert);
		this.title = new SimpleStringProperty(Title);
		this.kategorie = new SimpleIntegerProperty(Kategorie);
		this.kategoriename = new SimpleStringProperty(kategoriename);
		this.language = new SimpleIntegerProperty(Language);
		this.beschreibung = new SimpleStringProperty(Beschreibung);
		this.code = new SimpleStringProperty(Code);
		this.files = new SimpleIntegerProperty(files);
	}

	public int getId() {
		return id.get();
	}

	public Date getCreated() {
		return created.get();
	}

	public Date getModified() {
		return modified.get();
	}

	public String getTitle() {
		return title.get();
	}

	public int getKategorie() {
		return kategorie.get();
	}

	public int getLanguage() {
		return language.get();
	}

	public String getBeschreibung() {
		return beschreibung.get();
	}

	public String getCode() {
		return code.get();
	}

	public void setId(int id) {
		this.id = new SimpleIntegerProperty(id);
	}

	public void setCreated(Date created) {
		this.created = new SimpleObjectProperty<Date>(created);
	}

	public void setModified(Date modified) {
		this.modified = new SimpleObjectProperty<Date>(modified);
	}

	public void setTitle(String title) {
		this.title = new SimpleStringProperty(title);
	}

	public void setKategorie(int kategorie) {
		this.kategorie = new SimpleIntegerProperty(kategorie);
	}

	public void setLanguage(int language) {
		this.language = new SimpleIntegerProperty(language);
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = new SimpleStringProperty(beschreibung);
	}

	public void setCode(SimpleStringProperty code) {
		this.code = code;
	}

	public String getKategoriename() {
		return kategoriename.get();
	}

	public void setKategoriename(String kategoriename) {
		this.kategoriename = new SimpleStringProperty(kategoriename);
	}

	public String getLanguageInText() {
		return MainView.getDBHelper().getProgrammingLanguage(language.get());
	}

	/**
	 * @return the files
	 */
	public int getFiles() {
		return files.get();
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(int files) {
		this.files = new SimpleIntegerProperty(files);
	}

}