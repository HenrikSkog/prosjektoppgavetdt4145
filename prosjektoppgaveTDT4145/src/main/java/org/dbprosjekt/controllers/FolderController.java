package org.dbprosjekt.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

public class FolderController {
    @FXML
    private TextField folderNameInput;

    @FXML
    private void createFolder(){
        String name = folderNameInput.getText();
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        queryGenerator.insertFolder(name);
    }
}
