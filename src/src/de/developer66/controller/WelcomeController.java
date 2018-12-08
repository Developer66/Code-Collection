package de.developer66.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

/* 
 * Welcome-FXML Controller
 */
public class WelcomeController {

	/************** Attribute **************/
	@FXML
	private CheckBox checkbox;

	@FXML
	private Button buttonContinue;

	// Title
	private String title = "Code Collection";

	/************** Getter & Setter **************/
	// Get Title
	public String getTitle() {
		return title;
	}

	// Button für Continue Button
	Button getContinueButton() {
		return buttonContinue;
	}

	// Checkbox um WelcomeScreen dauerhaft auszublenden
	CheckBox getCheckbox() {
		return checkbox;
	}

	// Initialize
	@FXML
	void initialize() {
		// Set Confinue Button default selected
		buttonContinue.defaultButtonProperty().bind(buttonContinue.focusedProperty());
	}
}
