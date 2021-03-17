package org.dbprosjekt.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import org.dbprosjekt.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}