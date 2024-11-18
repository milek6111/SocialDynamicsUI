module org.example.modelui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires SocialModelDynamicLibrary;
    requires org.jgrapht.core;

    opens org.example.modelui to javafx.fxml;
    exports org.example.modelui;
    exports org.example.modelui.controllers;
    opens org.example.modelui.controllers to javafx.fxml;
}