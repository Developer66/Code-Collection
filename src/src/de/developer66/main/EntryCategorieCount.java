package de.developer66.main;

/*
 * Klasse zum Abspeichern von Informationen (PieChart)
 * 
 * In dieser Klasse wir speziell f�r das PieChart der Name und der zugeh�rige
 * Wert abgelegt und anschlie�en wieder geladen und dargestellt.
 */
public class EntryCategorieCount {
	String name;
	long value;

	public EntryCategorieCount(String name, long value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Long getValue() {
		return value;
	}
}
