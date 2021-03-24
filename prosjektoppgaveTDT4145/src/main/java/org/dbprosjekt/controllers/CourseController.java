package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.io.IOException;
import java.sql.SQLException;

//Kontrollerer oppretting av et nytt Course
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

    //Oppretter et nytt course dersom det ikke allerede eksisterer og IDen allerede finnes i Subject i databasen
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
    //Endrer scene til hovedvinduet
    private void goBack(ActionEvent actionEvent) throws SQLException {
        Program2Controller.reload();
    }
    @FXML
    //Setter staten til progreammet på toppen av vinduet
    public void initialize(){
        path.setText(Session.ToString());
    }
}
