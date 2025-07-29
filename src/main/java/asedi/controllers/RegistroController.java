package asedi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegistroController {

    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmarPasswordField;
    @FXML private Button registrarButton;
    @FXML private Hyperlink regresarLink;
    @FXML private Hyperlink iniciarSesionLink;

    @FXML
    private void initialize() {
        // Inicialización si es necesaria
    }
    
    @FXML
    private void handleRegistrar() {
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmarPasswordField.getText();
        
        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!esEmailValido(email)) {
            showAlert("Error", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.ERROR);
            return;
        }
        
        if (password.length() < 6) {
            showAlert("Error", "La contraseña debe tener al menos 6 caracteres.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Las contraseñas no coinciden.", Alert.AlertType.ERROR);
            return;
        }
        
        // Aquí iría la lógica de registro
        System.out.println("Registrando usuario: " + email);
        showAlert("Registro exitoso", "¡Bienvenido a FoodPlaza, " + nombre + "!", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void handleRegresar() {
        loadView("/views/login.fxml", "Inicio de Sesión - FoodPlaza");
    }
    
    @FXML
    private void handleIniciarSesion() {
        loadView("/views/login.fxml", "Inicio de Sesión - FoodPlaza");
    }
    
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/" + fxmlPath.replace("/views/", "").replace(".fxml", ".css"));
            
            // Configurar la ventana
            Stage stage = (Stage) nombreField.getScene().getWindow();
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
    
    private boolean esEmailValido(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }
}
