package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.SQLException;
//Kontrollerer oppretting av nye replies
public class ReplyController {
    @FXML
    private Button back;
    @FXML
    private CheckBox anonymous;
    @FXML
    private TextArea replyText;
    @FXML
    private Text path;
    @FXML
    //Oppretter en ny reply på den posten bruker trykket reply på
    private void postReply(ActionEvent actionEvent) throws SQLException {
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        queryGenerator.insertReply(Session.getReplyingToID(), anonymous.isSelected(), replyText.getText(), Session.getUserID());
        Session.setReplyingToID(0);
        Program2Controller.reload();
    }
    @FXML
    //Går tilbake til hovedvinduet
    private void goBack(ActionEvent actionEvent) throws SQLException {
        Program2Controller.reload();
    }
    @FXML
    //Sjekker og setter mulighet for anonyme posts
    public void initialize() throws SQLException {
        var queryGenerator = new DatabaseQueryGenerator();
        boolean allowsAnon = queryGenerator.currentCourseAllowsAnonymous();
        anonymous.setDisable(!allowsAnon);

    }
}
