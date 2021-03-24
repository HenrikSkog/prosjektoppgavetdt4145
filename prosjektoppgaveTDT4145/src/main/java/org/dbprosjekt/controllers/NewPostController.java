package org.dbprosjekt.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.dbprosjekt.App;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

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

		if(selectedTag.equals("No tag"))
			selectedTag = null;

		int isAnonymous = 0;

		if(anonymousBox.isSelected()) {
			isAnonymous = 1;
		}

		queryGenerator.insertThreadPost(titleInput.getText(), textInput.getText(), selectedTag, isAnonymous);

		Program2Controller.reload();
	}

	@FXML
	//Går tilbake til hovedvinduet
	public void back() throws SQLException {
		Program2Controller.reload();
	}

	@FXML
	//sjekker om det skal være mulig å poste anonymt og oppretter mulighet for dette
	public void initialize() {
		var queryGenerator = new DatabaseQueryGenerator();
		boolean allowsAnon = queryGenerator.currentCourseAllowsAnonymous();

		if(!allowsAnon) {
			anonymousBox.setDisable(true);
		} else {
			anonymousBox.setDisable(false);
		}

		tags.setValue("No tag");
		tags.setItems(
			FXCollections.observableArrayList(
				"No tag", "Question", "Homework ", "Homework solution", "Lectures notes", "General announcement"
		));

	}
}
