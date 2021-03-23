package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.io.IOException;
import java.sql.SQLException;

public class CourseController {
    @FXML
    private Text path;
    @FXML
    private CheckBox allowsAnonymous;
    @FXML
    private TextField courseIDInput;
    @FXML
    private ToggleGroup semester;
    @FXML
    private Text errorMessage;
    @FXML
    private Spinner<Integer> semYear;

    public void createCourse() throws IOException, SQLException {
        String id = courseIDInput.getText();
        String sem = ((RadioButton)semester.getSelectedToggle()).getText();
        int year = semYear.getValue();
        boolean allows = allowsAnonymous.isSelected();
        String term = sem+year;
        String queryString = "select * from Course where SubjectID='"+id+"' and Term='"+term+"'";
        String queryString2 = "select * from Subject where SubjectID='"+id+"'";
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        if (queryGenerator.queryHasResultRows(queryString)){
            errorMessage.setText("This course already exists");
        }
        else if(!queryGenerator.queryHasResultRows(queryString2)){
            errorMessage.setText("Please enter a valid CourseID");
        }
        else{
            queryGenerator.insertCourse(id, term, allows);
            queryGenerator.insertInCourse(Session.getUserID(), id, term);
            Program2Controller.initialize();
        }
    }
    @FXML
    private void goBack(ActionEvent actionEvent) throws SQLException {
        Program2Controller.reload();
    }
    @FXML
    public void initialize(){
        path.setText(Session.ToString());
    }
}
