package org.dbprosjekt.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.dbprosjekt.App;

import java.io.IOException;

public class ProgramController {
    @FXML
    private void toNewFolder() throws IOException {
        App.setRoot("folder");
    }

    public void toNewCourse() throws IOException{
        App.setRoot("course");
    }
}
