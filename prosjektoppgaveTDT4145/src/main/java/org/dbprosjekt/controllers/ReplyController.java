package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.SQLException;

//Kontrollerer oppretting av nye replies
public class ReplyController {
    @FXML
    private CheckBox anonymous;
    @FXML
    private TextArea replyText;
    @FXML
    private TextField linkInput;
    @FXML
    private Text errorText;

    @FXML
    //Oppretter en ny reply på den posten bruker trykket reply på
    private void postReply() throws SQLException {
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
            queryGenerator.insertPostLink(Integer.parseInt(replyid), Integer.parseInt(linkInput.getText()));
        }
        ProgramController.reload();
    }
    @FXML
    //Går tilbake til hovedvinduet
    private void goBack() throws SQLException {
        ProgramController.reload();
    }
    @FXML
    //Sjekker og setter mulighet for anonyme posts
    public void initialize() throws SQLException {
        var queryGenerator = new DatabaseQueryGenerator();
        boolean allowsAnon = queryGenerator.currentCourseAllowsAnonymous();
        anonymous.setDisable(!allowsAnon);

    }
}
