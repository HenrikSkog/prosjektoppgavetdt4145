package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.dbprosjekt.App;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.util.Arrays;

public class NewPostController {
	@FXML
	TextField titleInput;

	@FXML
	TextArea textInput;

	@FXML
	ToggleGroup tag;

	@FXML
	public void newPost() {
		var queryGenerator = new DatabaseQueryGenerator();

		var selectedTag = (RadioButton) tag.getSelectedToggle();
		var tagVal = selectedTag.getText();

		queryGenerator.insertThreadPost(titleInput.getText(), textInput.getText(), tagVal);
	}
}
