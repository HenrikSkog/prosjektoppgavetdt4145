package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.ResultSet;
import java.sql.SQLException;

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
    private void promoteUser(ActionEvent actionEvent) throws SQLException {
        String email = promoteEmail.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from User where Email ='" + email + "'";
        if (!queryGenerator.queryHasResultRows(queryString)) {
            promoteMessage.setText("There exists no such user");
            return;
        }
        ResultSet rs = queryGenerator.query(queryString);
        rs.next();
        if (rs.getString("Type").equals("instructor")){
            promoteMessage.setText("Already instructor");
            return;
        }
        queryGenerator.promoteUser(email);
        promoteMessage.setText("User successfully promoted");
    }
    @FXML
    private void addToCourse(ActionEvent actionEvent) throws SQLException {
        String email = addEmail.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from User where Email ='"+email+"'";
        if(!queryGenerator.queryHasResultRows(queryString)){
            addMessage.setText("There exists no such user");
            return;
        }
        queryString = "select * from User inner join InCourse on User.Email = InCourse.Email where InCourse.SubjectID='"+Session.getCourseID()+"' and InCourse.Term='"+Session.getTerm()+"' and User.Email='"+email+"'";
        if(queryGenerator.queryHasResultRows(queryString)){
            addMessage.setText("User already in course");
            return;
        }
        queryGenerator.insertInCourse(email, Session.getCourseID(), Session.getTerm());
        addMessage.setText("User successfully added");
    }
    @FXML
    private void removeFromCourse(ActionEvent actionEvent) throws SQLException {
        String email = removeEmail.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        String queryString = "select * from User where Email ='"+email+"'";
        if (!queryGenerator.queryHasResultRows(queryString)){
            removeMessage.setText("There exists no such user");
            return;
        }
        queryString = "select * from User inner join InCourse on User.Email = InCourse.Email where InCourse.SubjectID='"+Session.getCourseID()+"' and InCourse.Term='"+Session.getTerm()+"' and User.Email='"+email+"'";
        if (!queryGenerator.queryHasResultRows(queryString)){
            removeMessage.setText("User not in course");
            return;
        }
        queryGenerator.removeInCourse(email);
        removeMessage.setText("User successfully removed");
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
