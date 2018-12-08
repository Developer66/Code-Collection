package de.developer66.util;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;

/*
 * Klasse zum abspeichern aller Informationen aus dem Table Kategorie Benötigt
 * für die Kategorie Verwaltung
 */
public class Kategorie {
	private SimpleStringProperty name;
	private SimpleStringProperty describtion;
	private int id;
	private Date erstellt;
	private Date veraendert;

	public Kategorie(int id, String name, String describtion, Date erstellt, Date veraendert) {
		this.id = id;
		this.name = new SimpleStringProperty(name);
		this.describtion = new SimpleStringProperty(describtion);
		this.erstellt = erstellt;
		this.veraendert = veraendert;
	}

	public SimpleStringProperty getNameProperty() {
		return name;
	}

	public SimpleStringProperty getDescribtionProperty() {
		return describtion;
	}

	public String getName() {
		return name.get();
	}

	public String getDescribtion() {
		return describtion.get();
	}

	public int getId() {
		return id;
	}

	public Date getErstellt() {
		return erstellt;
	}

	public Date getVeraendert() {
		return veraendert;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = new SimpleStringProperty(name);
	}

	public void setDescribtion(String describtion) {
		this.describtion = new SimpleStringProperty(describtion);
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt;
	}

	public void setVeraendert(Date veraendert) {
		this.veraendert = veraendert;
	}
}
