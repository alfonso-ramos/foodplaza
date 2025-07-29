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
        // Inicialización si es necesaria
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos.", Alert.AlertType.ERROR);
            return;
        }
        
        // Validación básica de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.ERROR);
            return;
        }
        
        // Aquí iría la lógica de autenticación
        System.out.println("Iniciando sesión con: " + email);
    }
    
    @FXML
    private void handleCreateAccount() {
        loadView("/views/registro.fxml", "Registro - FoodPlaza");
    }
    
    @FXML
    private void handleRecoverPassword() {
        showAlert("Recuperar Contraseña", "Funcionalidad en desarrollo.", Alert.AlertType.INFORMATION);
    }
    
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/" + fxmlPath.replace("/views/", "").replace(".fxml", ".css"));
            
            // Configurar la ventana
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
