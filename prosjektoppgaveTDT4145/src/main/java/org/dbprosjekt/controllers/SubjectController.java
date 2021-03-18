package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;

public class SubjectController {
    @FXML
    private TextField subjectNameInput;
    @FXML
    private TextField subjectIdInput;
    @FXML
    private Text errorMessage;
    @FXML
    public void createSubject() {
        String id = subjectIdInput.getText();
        String name = subjectNameInput.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from subject where subjectId='" + id + "'";
        if(queryGenerator.queryHasResultRows(queryString)){
            errorMessage.setText("A subject with this ID has already been defined");
        }
        else{
            queryString = "";
        }
    }
}
