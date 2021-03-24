package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.SQLException;

//Kontrollerer oppretting, sletting og endring av navn for folders
public class FolderController {
    @FXML
    private Text renameError;
    @FXML
    private Text deleteError;
    @FXML
    private TextField renameInput;
    @FXML
    private Text path;
    @FXML
    private TextField folderNameInput;

    @FXML
    //Oppretter en ny folder
    private void createFolder() throws SQLException {
        String name = folderNameInput.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        queryGenerator.insertFolder(name);
        Program2Controller.reload();
    }
    @FXML
    //Går tilbake til hovedvinduet
    private void goBack(ActionEvent actionEvent) throws SQLException {
        Program2Controller.reload();
    }
    @FXML
    //Setter staten til progreammet på toppen av vinduet
    public void initialize(){
        path.setText(Session.ToString());
    }
    @FXML
    //Sletter folderen programmet befinner seg i dersom det befinner seg i en.
    private void deleteFolder(ActionEvent actionEvent) throws SQLException {
        if(Session.getCurrentFolderID()==0){
            deleteError.setText("No folder selected");
            return;
        }
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        queryGenerator.removeFolder(Session.getCurrentFolderID());
        Session.setFolderID(0);
        Program2Controller.reload();
    }
    @FXML
    //Endrer navnet på folderen programmet befinner seg i dersom det befinner seg i en.
    private void renameFolder(ActionEvent actionEvent) throws SQLException {
        if(Session.getCurrentFolderID()==0){
            renameError.setText("No folder Selected");
            return;
        }
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        queryGenerator.renameFolder(Session.getCurrentFolderID(), renameInput.getText());
        path.setText(Session.ToString());
    }
}
