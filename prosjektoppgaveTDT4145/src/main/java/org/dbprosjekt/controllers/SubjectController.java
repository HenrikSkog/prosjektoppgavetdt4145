package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.io.IOException;
import java.sql.SQLException;

//Kontrollerer opprtting av et nytt Subject
public class SubjectController {
    @FXML
    private Text path;
    @FXML
    private TextField subjectNameInput;
    @FXML
    private TextField subjectIdInput;
    @FXML
    private Text errorMessage;
    @FXML
    //Inserter et nytt Subject, dersom det ikke finnes fra før
    public void createSubject() throws IOException, SQLException {
        String id = subjectIdInput.getText();
        String name = subjectNameInput.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from Subject where SubjectId='" + id + "'";
        if(queryGenerator.queryHasResultRows(queryString)){
            errorMessage.setText("A subject with this ID has already been defined");
        }
        else{
            queryGenerator.insertSubject(id, name);
            ProgramController.reload();
        }
    }
    @FXML
    //Endrer scene til "hovedvinduet"
    private void goBack() throws SQLException {
        ProgramController.reload();
    }
    @FXML
    //Setter staten til progreammet på toppen av vinduet
    public void initialize(){
        path.setText(Session.ToString());
    }
}
