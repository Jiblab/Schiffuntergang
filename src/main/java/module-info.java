module org.example.schiffuntergang {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires com.google.gson;

    opens org.example.schiffuntergang to javafx.fxml,com.google.gson;
    opens org.example.schiffuntergang.components to com.google.gson;
    exports org.example.schiffuntergang to javafx.graphics;
}

