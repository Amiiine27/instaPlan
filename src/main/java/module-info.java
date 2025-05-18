module org.example.projets2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;

    opens org.example.projets2 to javafx.fxml;
    exports org.example.projets2;
    opens org.example.projets2.controller to javafx.fxml;
    exports org.example.projets2.controller;
}