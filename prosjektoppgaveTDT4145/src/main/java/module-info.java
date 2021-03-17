module org.dbprosjekt {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports org.dbprosjekt.controllers to javafx.fxml;
    exports org.dbprosjekt to javafx.graphics;

    opens org.dbprosjekt.controllers to javafx.fxml;

    opens org.dbprosjekt;



}