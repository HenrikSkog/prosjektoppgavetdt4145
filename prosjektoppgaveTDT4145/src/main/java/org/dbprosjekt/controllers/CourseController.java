package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.io.IOException;
import java.sql.SQLException;

public class CourseController {
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
            Program2Controller.initialize();
        }
    }
}
