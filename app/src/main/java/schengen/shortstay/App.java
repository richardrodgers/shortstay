/**
 * Copyright 2024 Richard Rodgers
 * SPDX-Licence-Identifier: Apache-2.0
 */
package schengen.shortstay;

import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var i18n = ResourceBundle.getBundle("i18n");
        var loader = new FXMLLoader(getClass().getResource("/schengen.fxml"), i18n);
        Parent root = (Parent)loader.load();
        primaryStage.setTitle(i18n.getString("appTitle"));
        primaryStage.setScene(new Scene(root, 560, 500));
        primaryStage.setOnCloseRequest(_ -> ((Controller)loader.getController()).dispose());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}