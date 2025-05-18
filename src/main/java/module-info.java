module org.example.projets2 {
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;
    requires com.calendarfx.view;

    opens org.example.projets2 to javafx.fxml;
    exports org.example.projets2;
    opens org.example.projets2.controller to javafx.fxml;
    exports org.example.projets2.controller;
}