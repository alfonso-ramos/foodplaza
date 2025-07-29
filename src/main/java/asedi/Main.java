package asedi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el archivo FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root);
            
            // Configurar la ventana
            primaryStage.setTitle("FoodPlaza - Inicio de Sesión");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
            // Mostrar la ventana
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar la pantalla de login: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Test");
        launch(args);
    }
}