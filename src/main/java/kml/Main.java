package kml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/main.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("Krothium Launcher Updater");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest((e) -> System.exit(0));
        primaryStage.show();

        MainController mainController = loader.getController();
        mainController.startUpdate();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
