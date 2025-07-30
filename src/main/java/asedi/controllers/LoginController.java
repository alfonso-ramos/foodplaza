package asedi.controllers;

import asedi.services.AuthService;
import asedi.models.User;
import javafx.event.ActionEvent;
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
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Por favor ingrese su correo y contraseña", Alert.AlertType.WARNING);
            return;
        }
        
        // Validar formato de correo electrónico
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Por favor ingrese un correo electrónico válido", Alert.AlertType.WARNING);
            return;
        }
        
        AuthService authService = AuthService.getInstance();
        if (authService.login(email, password)) {
            User user = authService.getCurrentUser();
            String dashboardPath = "/views/" + getDashboardForRole(user.getRole()) + ".fxml";
            String title = "Panel de " + user.getRole().getDisplayName() + " - FoodPlaza";
            loadView(dashboardPath, title);
        } else {
            showAlert("Error", "Usuario o contraseña incorrectos", Alert.AlertType.ERROR);
        }
    }
    
    private String getDashboardForRole(User.UserRole role) {
        return switch (role) {
            case ADMIN -> "adminDashboard";
            case MANAGER -> "gerente/gerenteDashboard";
            case USER -> "usuario/usuarioDashboard";
        };
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
            System.out.println("Intentando cargar vista: " + fxmlPath);
            
            // Verificar si el archivo FXML existe
            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("No se pudo encontrar el archivo: " + fxmlPath);
            }
            
            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root);
            
            // Cargar estilos CSS
            loadStyles(fxmlPath, scene);
            
            // Configurar la ventana
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            
            // Maximizar la ventana si es el dashboard
            if (fxmlPath.equals("/views/adminDashboard.fxml")) {
                stage.setMaximized(true);
            }
            
            // Centrar la ventana
            stage.centerOnScreen();
            
            // Mostrar la ventana
            stage.show();
            
            System.out.println("Vista cargada exitosamente: " + fxmlPath);
            
        } catch (IOException e) {
            String errorMsg = "Error al cargar la vista " + fxmlPath + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showAlert("Error", errorMsg, Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga los estilos CSS para una vista específica.
     * @param fxmlPath Ruta del archivo FXML
     * @param scene Escena a la que se aplicarán los estilos
     */
    private void loadStyles(String fxmlPath, Scene scene) {
        try {
            String cssPath;
            
            // Determinar la ruta del CSS basado en la vista
            if (fxmlPath.equals("/views/adminDashboard.fxml")) {
                cssPath = "/styles/adminDashboard/adminDashboard.css";
            } else if (fxmlPath.equals("/views/registro.fxml")) {
                cssPath = "/styles/registro.css";
            } else {
                // Ruta por defecto para otras vistas
                cssPath = fxmlPath.replace("/views/", "/styles/").replace(".fxml", ".css");
            }
            
            // Cargar el CSS si existe
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                System.out.println("Estilos cargados desde: " + cssPath);
            } else {
                System.out.println("No se encontraron estilos en: " + cssPath);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar estilos para " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
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
