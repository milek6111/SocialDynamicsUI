module pl.edu.agh.modelui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires SocialModelDynamicLibrary;
    requires org.jgrapht.core;

    opens pl.edu.agh.modelui to javafx.fxml;
    exports pl.edu.agh.modelui;
    exports pl.edu.agh.modelui.controllers;
    opens pl.edu.agh.modelui.controllers to javafx.fxml;
}