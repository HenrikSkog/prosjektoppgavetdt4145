package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.App;
import org.dbprosjekt.database.DatabaseQueryGenerator;

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
    private Button signInButton;

    @FXML
    private Text errorText;

    @FXML
    private Text regErrorText;

    @FXML
    private void signIn() {
        String email = emailTextInput.getText();
        String password = passwordTextInput.getText();


        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from user where email='" + email + "' and password='" + password + "'";

        if (queryGenerator.queryHasResultRows(queryString))
            System.out.println("Signed in");
        else {
            System.out.println("No such user");
            errorText.setText("Invalid username or password");
        }
    }

    @FXML
    private void register() {
        boolean emailInUse = false;
        boolean usernameInUse = false;

        String email = regEmailTextInput.getText();
        String password = regPasswordTextInput.getText();
        String username = regUsernameTextInput.getText();

        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from user where email='" + email + "'";

        if (queryGenerator.queryHasResultRows(queryString))
            emailInUse = true;
        queryString = "select * from user where username='" + username + "'";
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
        }
    }
}
