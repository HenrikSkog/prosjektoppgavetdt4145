package org.dbprosjekt.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.sql.SQLException;

//Kontrollerer oppretting av nye posts
public class NewPostController {
	@FXML
	TextField titleInput;

	@FXML
	TextArea textInput;

	@FXML
	ChoiceBox tags;

	@FXML
	Pane container;

	@FXML
	CheckBox anonymousBox;

	@FXML
	//Oppretter en ny post i mappen programmet befinner seg i
	public void newPost() throws SQLException {
		var queryGenerator = new DatabaseQueryGenerator();

		var selectedTag = tags.getValue().toString();

		if (selectedTag.equals("No tag"))
			selectedTag = null;

		queryGenerator.insertThreadPost(titleInput.getText(), textInput.getText(), selectedTag, anonymousBox.isSelected());

		ProgramController.reload();
	}

	@FXML
	//Går tilbake til hovedvinduet
	public void back() throws SQLException {
		ProgramController.reload();
	}

	@FXML
	//sjekker om det skal være mulig å poste anonymt og oppretter mulighet for dette
	public void initialize() {
		var queryGenerator = new DatabaseQueryGenerator();
		boolean allowsAnon = queryGenerator.currentCourseAllowsAnonymous();

		anonymousBox.setDisable(!allowsAnon);

		tags.setValue("No tag");
		tags.setItems(
			FXCollections.observableArrayList(
				"No tag", "Question", "Homework ", "Homework solution", "Lectures notes", "General announcement"
			));

	}
}
