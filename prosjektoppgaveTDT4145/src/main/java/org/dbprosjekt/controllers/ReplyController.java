package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.SQLException;

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
    private TextField linkInput;
    @FXML
    private Text errorText;

    @FXML
    private void postReply(ActionEvent actionEvent) throws SQLException {
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();

        if(!linkInput.getText().equals("")) {
            errorText.setText("");
            var tpExists = queryGenerator.threadPostExists(linkInput.getText());
            if(!tpExists) {
                linkInput.setText("");
                errorText.setText("There is no initial post with this id.");
                return;
            }
//            handle link insert after reply insert as we need replyID
        }

        queryGenerator.insertReply(Session.getReplyingToID(), anonymous.isSelected(), replyText.getText(), Session.getUserID());
        Session.setReplyingToID(0);

        if(!linkInput.getText().equals("")) {
            String replyid = queryGenerator.getLastInsertedID();
            queryGenerator.insertPostLink(replyid, linkInput.getText());
        }
        Program2Controller.reload();
    }
    @FXML
    private void goBack(ActionEvent actionEvent) throws SQLException {
        Program2Controller.reload();
    }
    @FXML
    public void initialize() throws SQLException {
        var queryGenerator = new DatabaseQueryGenerator();
        boolean allowsAnon = queryGenerator.currentCourseAllowsAnonymous();
        anonymous.setDisable(!allowsAnon);

    }
}
