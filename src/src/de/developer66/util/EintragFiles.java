package de.developer66.util;

import de.developer66.main.MainView;

/*
 * Class für File TableView
 */
public class EintragFiles {
	private int id; // ID
	private String name; // Name
	private String size; // Size

	public EintragFiles(int id, String name, String size) {
		this.id = id;
		this.name = name;
		this.size = size;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSize() {
		return size;
	}

	public String getFormattedSize() {
		try {
			return MainView.getDBHelper().getFileSize(Long.parseLong(size));
		} catch (Exception e) {
			return null;
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
