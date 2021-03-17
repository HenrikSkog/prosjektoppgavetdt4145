package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.dbprosjekt.database.DatabaseQueryGenerator;

public class LoginController {
  @FXML
  private TextField emailTextInput;

  @FXML
  private TextField passwordTextInput;

  @FXML
  private Button signinButton;

  @FXML
  private void signin() {
    var email = emailTextInput.getText();
    var password = passwordTextInput.getText();

    var queryGenerator = new DatabaseQueryGenerator();
    var queryString = "select * from user where email='" + email +  "' and password='" + password + "'";

    if (queryGenerator.queryHasResultRows(queryString)) {
      System.out.println("Signed in");
    } else {
      System.out.println("No such user");
    }
  }
}
