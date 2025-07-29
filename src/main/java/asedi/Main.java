package asedi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el archivo FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Region root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root);
            
            // Configurar la ventana
            primaryStage.setTitle("FoodPlaza - Inicio de Sesión");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
            
            // Asegurar que el contenido se redimensione correctamente
            root.prefWidthProperty().bind(scene.widthProperty());
            root.prefHeightProperty().bind(scene.heightProperty());
            
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