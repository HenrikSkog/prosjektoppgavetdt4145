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
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("newpost"), 800, 1080);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    public static void SetRoot(Parent p){
        scene.setRoot(p);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        System.out.println(App.class.getResource("fxml/" + fxml + ".fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}