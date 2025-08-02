package asedi.controllers;

import asedi.models.RespuestaLogin;
import asedi.models.Usuario;
import asedi.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    
    private final AuthService authService = AuthService.getInstance();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error", "Por favor ingrese su correo y contraseña", Alert.AlertType.ERROR);
            return;
        }

        try {
            RespuestaLogin respuesta = authService.login(email, password);
            if (respuesta != null && respuesta.getUsuario() != null) {
                redirigirSegunRol(respuesta.getUsuario());
            } else {
                mostrarAlerta("Error", "Credenciales inválidas", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void redirigirSegunRol(Usuario usuario) {
        String rutaVista;
        String titulo;
        
        switch (usuario.getRol().toLowerCase()) {
            case "administrador":
                rutaVista = "/views/adminDashboard.fxml";
                titulo = "Panel de Administración";
                break;
            case "gerente":
                rutaVista = "/views/gerente/gerenteDashboard.fxml";
                titulo = "Panel de Gerencia";
                break;
            case "usuario":
            default:
                rutaVista = "/views/usuario/usuarioDashboard.fxml";
                titulo = "Mi Cuenta";
                break;
        }

        loadView(rutaVista, titulo);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        // Centrar la alerta en la ventana
        Window window = emailField.getScene().getWindow();
        alert.setX(window.getX() + (window.getWidth() - 400) / 2);
        alert.setY(window.getY() + (window.getHeight() - 200) / 2);
        
        alert.showAndWait();
    }
    
    @FXML
    private void handleRegistro() {
        try {
            // Cargar la vista de registro
            Parent root = FXMLLoader.load(getClass().getResource("/views/registro.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista de registro", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleRecoverPassword() {
        mostrarAlerta("Recuperar Contraseña", "Funcionalidad en desarrollo.", Alert.AlertType.INFORMATION);
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
            
            // Centrar la ventana
            stage.centerOnScreen();
            
            // Mostrar la ventana
            stage.show();
            
            System.out.println("Vista cargada exitosamente: " + fxmlPath);
            
        } catch (IOException e) {
            String errorMsg = "Error al cargar la vista " + fxmlPath + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            mostrarAlerta("Error", errorMsg, Alert.AlertType.ERROR);
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
    

}
