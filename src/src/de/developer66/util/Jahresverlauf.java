package de.developer66.util;

public class Jahresverlauf {
	private int month;
	private int year;
	private int value;

	public Jahresverlauf(int month, int year, int value) {
		this.month = month;
		this.year = year;
		this.value = value;
	}

	public int getMonth() {
		return month;
	}

	public int getValue() {
		return value;
	}

	public int getYear() {
		return year;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setYear(int year) {
		this.year = year;
	}

}
