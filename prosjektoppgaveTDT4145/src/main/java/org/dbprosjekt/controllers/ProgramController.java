package org.dbprosjekt.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import org.dbprosjekt.App;
import org.dbprosjekt.Course;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProgramController {
    @FXML
    private static ComboBox<Course> dropDown;

    @FXML private static ToolBar toolBar;

    @FXML
    private void toNewFolder() throws IOException {
        App.setRoot("folder");
    }

    public void toNewCourse() throws IOException{
        App.setRoot("course");
    }

    public void toNewSubject() throws IOException {
        App.setRoot("subject");
    }
    public static void update() throws SQLException {
        String queryString = "select SubjectID, Term, name from Course natural join Subject";
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Course> courses = new ArrayList<Course>();
        while(rs.next()){
            String id = rs.getString("SubjectID");
            String term = rs.getString("Term");
            String name = rs.getString("name");
            courses.add(new Course(name, term, id));
        }
        Course[] c = new Course[courses.size()];
        for(int i = 0; i<courses.size(); i++){
            c[i] = courses.get(i);
        }
        ObservableList<Course> list = FXCollections.observableArrayList(c);
//        dropDown.setItems(list);

    }
}
