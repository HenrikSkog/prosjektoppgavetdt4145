package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.ResultSet;
import java.sql.SQLException;

//Kontrollerer promotering av brukere til instructor, samt adding og removing av brukerer til et course
public class ManageUsersController {
	@FXML
	private Text path;
	@FXML
	private TextField removeEmail;
	@FXML
	private TextField addEmail;
	@FXML
	private TextField promoteEmail;
	@FXML
	private Text promoteMessage;
	@FXML
	private Text addMessage;
	@FXML
	private Text removeMessage;

	@FXML
	//Promoterer en bruker dersom den finnes og ikke allerede er instructor
	private void promoteUser() throws SQLException {
		String email = promoteEmail.getText();
		DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
		String queryString = "select * from User where Email ='" + email + "'";
		if (!queryGenerator.queryHasResultRows(queryString)) {
			promoteMessage.setText("There exists no such user");
			return;
		}
		ResultSet rs = queryGenerator.query(queryString);
		rs.next();
		if (rs.getString("Type").equals("instructor")) {
			promoteMessage.setText("Already instructor");
			return;
		}
		queryGenerator.promoteUser(email);
		promoteMessage.setText("User successfully promoted");
	}

	@FXML
	//Legger en bruker til i et course dersom den finnes og ikke allerede er i courset
	private void addToCourse() {
		String email = addEmail.getText();
		DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
		String queryString = "select * from User where Email ='" + email + "'";
		if (!queryGenerator.queryHasResultRows(queryString)) {
			addMessage.setText("There exists no such user");
			return;
		}
		queryString = "select * from User inner join InCourse on User.Email = InCourse.Email where InCourse.SubjectID='" + Session.getCourseID() + "' and InCourse.Term='" + Session.getTerm() + "' and User.Email='" + email + "'";
		if (queryGenerator.queryHasResultRows(queryString)) {
			addMessage.setText("User already in course");
			return;
		}
		queryGenerator.insertInCourse(email, Session.getCourseID(), Session.getTerm());
		addMessage.setText("User successfully added");
	}

	@FXML
	//Fjerner en bruker fra et course dersom brukeren finnes og er med i courset.
	private void removeFromCourse() {
		String email = removeEmail.getText();
		DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
		String queryString = "select * from User where Email ='" + email + "'";
		if (!queryGenerator.queryHasResultRows(queryString)) {
			removeMessage.setText("There exists no such user");
			return;
		}
		queryString = "select * from User inner join InCourse on User.Email = InCourse.Email where InCourse.SubjectID='" + Session.getCourseID() + "' and InCourse.Term='" + Session.getTerm() + "' and User.Email='" + email + "'";
		if (!queryGenerator.queryHasResultRows(queryString)) {
			removeMessage.setText("User not in course");
			return;
		}
		queryGenerator.removeInCourse(email);
		removeMessage.setText("User successfully removed");
	}

	@FXML
	//Går tilbake til hovedvinduet
	private void goBack() throws SQLException {
		ProgramController.reload();
	}

	@FXML
	//Setter staten til progreammet på toppen av vinduet
	public void initialize() {
		path.setText(Session.ToString());
	}
}
