package org.dbprosjekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dbprosjekt.database.DBConn;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    //Starter programmet ved å sette scenen til å være login-skjermen
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 800, 1080);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    //Endrer root til en annen FXML-fil
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    //Endrer root til en JavaFX-node
    public static void setRoot(Parent p){
        scene.setRoot(p);
    }

    //Laster inn FXML-filene
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}