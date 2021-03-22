package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.dbprosjekt.database.DatabaseQueryGenerator;

public class NewPostController {
	@FXML
	TextField titleInput;

	@FXML
	TextArea textInput;

	@FXML
	public void newPost() {
		var queryGenerator = new DatabaseQueryGenerator();

		String email = "email";

		queryGenerator.insertPost(titleInput.getText(), email, textInput.getText(), false);
	}

}
