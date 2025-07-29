package asedi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink createAccountLink;

    @FXML
    private Hyperlink recoverPasswordLink;

    @FXML
    public void initialize() {
        // Configurar el botón de inicio de sesión
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            System.out.println("Email: " + email + ", Password: " + password);
            // Aquí puedes validar el login con la base de datos
        });
        
        // Configurar el enlace para crear cuenta
        createAccountLink.setOnAction(e -> {
            try {
                // Cargar la vista de registro
                Parent root = FXMLLoader.load(getClass().getResource("/views/registro.fxml"));
                
                // Obtener la escena actual
                Scene scene = createAccountLink.getScene();
                if (scene == null) {
                    // Si no hay escena, crear una nueva
                    scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("FoodPlaza - Registro");
                    stage.show();
                } else {
                    // Si ya hay una escena, usarla
                    scene.setRoot(root);
                    Stage stage = (Stage) scene.getWindow();
                    stage.setTitle("FoodPlaza - Registro");
                }
            } catch (IOException ex) {
                System.err.println("Error al cargar la vista de registro: " + ex.getMessage());
                ex.printStackTrace();
                // Mostrar mensaje de error al usuario
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No se pudo cargar la pantalla de registro. Por favor, intente nuevamente.");
                alert.showAndWait();
            }
        });
    }
}
