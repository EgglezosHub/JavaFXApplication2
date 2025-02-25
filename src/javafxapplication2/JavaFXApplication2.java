package javafxapplication2;
//JavaFXApplication2
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;





public class JavaFXApplication2 extends Application {
 
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file      
        Parent root = FXMLLoader.load(getClass().getResource("/javafxapplication2/FXMLDocument.fxml"));
        // Set up the scene
        Scene scene = new Scene(root,1000,600);
        scene.getStylesheets().add(getClass().getResource("/javafxapplication2/style.css").toExternalForm());      
        Image icon = new Image("/Images/icon.png");
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ChatterBox");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }  
} 