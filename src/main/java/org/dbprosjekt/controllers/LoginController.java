package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.ResultSet;
import java.sql.SQLException;

//Kontrollerer innlogging og registrering
public class LoginController {
	@FXML
	private TextField emailTextInput;

	@FXML
	private TextField passwordTextInput;

	@FXML
	private TextField regEmailTextInput;

	@FXML
	private TextField regPasswordTextInput;

	@FXML
	private TextField regUsernameTextInput;

	@FXML
	private Text errorText;

	@FXML
	private Text regErrorText;

	@FXML
	//Logger inn dersom brukernavn og passord matcher en rad i user oppdaterer samtidig Session med email og isAdmin, for så å initialize hovedvinduet
	private void signIn() throws SQLException {
		String email = emailTextInput.getText();
		String password = passwordTextInput.getText();


		DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
		String queryString = "select * from User where email='" + email + "' and password='" + password + "'";

		if (queryGenerator.queryHasResultRows(queryString)) {
			queryString = "select * from User where Email='" + email + "'";
			ResultSet rs = queryGenerator.query(queryString);
			boolean isAdmin = false;
			while (rs.next()) {
				isAdmin = rs.getString("Type").equals("instructor");
			}
			Session.setAdmin(isAdmin);
			Session.setUserID(email);

			ProgramController.initialize();
		} else {
			System.out.println("No such user");
			errorText.setText("Invalid username or password");
		}
	}

	@FXML
	//Registerer en ny bruker dersom hverken epost eller brukernavn er i bruk, oppdaterer session og intializer
	private void register() throws SQLException {
		boolean emailInUse = false;
		boolean usernameInUse = false;

		String email = regEmailTextInput.getText();
		String password = regPasswordTextInput.getText();
		String username = regUsernameTextInput.getText();

		DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
		String queryString = "select * from User where email='" + email + "'";

		if (queryGenerator.queryHasResultRows(queryString))
			emailInUse = true;
		queryString = "select * from User where username='" + username + "'";
		if ((queryGenerator.queryHasResultRows(queryString)))
			usernameInUse = true;
		if (emailInUse) {
			if (usernameInUse)
				regErrorText.setText("Email and username already in use");
			else
				regErrorText.setText("Email already in use");
		} else if (usernameInUse)
			regErrorText.setText("Username already in use");
		else {
			regErrorText.setText("Registration successful");
			queryGenerator.insertUser(email, username, password);
			Session.setUserID(email);
			Session.setAdmin(false);
			ProgramController.initialize();
		}
	}
}
