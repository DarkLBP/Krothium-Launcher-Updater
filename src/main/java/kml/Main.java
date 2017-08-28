package kml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> parameters = getParameters().getRaw();
        if (parameters.size() != 1) {
            System.err.println("Jar path is required.");
            System.exit(1);
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/main.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("Krothium Launcher Updater");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest((e) -> System.exit(0));
        primaryStage.show();

        MainController mainController = loader.getController();
        File jar = new File(parameters.get(0));
        if (jar.exists() && jar.isFile()) {
            mainController.startUpdate(jar);
        } else {
            System.err.println("Invalid jar path.");
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
