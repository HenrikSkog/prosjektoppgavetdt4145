package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.SQLException;

public class FolderController {
    @FXML
    private TextField folderNameInput;

    @FXML
    private void createFolder() throws SQLException {
        String name = folderNameInput.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        queryGenerator.insertFolder(name);
        Program2Controller.reload();
    }
    @FXML
    private void goBack(ActionEvent actionEvent) throws SQLException {
        Program2Controller.reload();
    }
}
