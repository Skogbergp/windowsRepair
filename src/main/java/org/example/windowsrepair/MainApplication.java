package org.example.windowsrepair;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        MainView view = new MainView();
        Scene scene = new Scene((Parent) view.getRoot(), 700, 500);
        stage.setTitle("Windows Repair Tool");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        CommandService.shutdown();
    }
}