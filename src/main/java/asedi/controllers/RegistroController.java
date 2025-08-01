package asedi.controllers;

import asedi.services.AuthService;
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
import javafx.stage.Window;
import java.io.IOException;
import java.util.regex.Pattern;
import javafx.scene.control.Alert.AlertType;


public class RegistroController {

    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmarPasswordField;
    @FXML private Button registrarButton;
    @FXML private Hyperlink regresarLink;
    @FXML private Hyperlink iniciarSesionLink;
    
    private final AuthService authService;

    public RegistroController() {
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    private void initialize() {
        // Configurar el botón de registro para que se active con Enter
        registrarButton.setOnAction(_ -> handleRegistrar());
        
        // Configurar el campo de teléfono para solo aceptar números
        telefonoField.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("\\d*")) {
                telefonoField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    @FXML
    private void handleRegistrar() {
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmarPasswordField.getText();
        
        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos.", AlertType.ERROR);
            return;
        }
        
        if (!esEmailValido(email)) {
            showAlert("Error", "Por favor, ingrese un correo electrónico válido.", AlertType.ERROR);
            return;
        }
        
        // Validar contraseña
        String errorPassword = validarContrasena(password);
        if (errorPassword != null) {
            showAlert("Error en la contraseña", errorPassword, AlertType.ERROR);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Las contraseñas no coinciden.", AlertType.ERROR);
            return;
        }
        
        // Deshabilitar el botón para evitar múltiples envíos
        registrarButton.setDisable(true);
        
        // Mostrar indicador de carga
        Alert loadingAlert = new Alert(AlertType.INFORMATION);
        loadingAlert.setTitle("Registrando...");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Creando su cuenta, por favor espere...");
        loadingAlert.show();
        
        // Ejecutar en un hilo separado para no bloquear la interfaz
        new Thread(() -> {
            try {
                // Llamar al servicio de autenticación
                boolean registroExitoso = authService.registrarUsuario(nombre, email, telefono, password);
                
                // Volver al hilo de JavaFX para actualizar la interfaz
                javafx.application.Platform.runLater(() -> {
                    loadingAlert.close();
                    
                    if (registroExitoso) {
                        // Mostrar mensaje de éxito
                        Alert successAlert = new Alert(AlertType.INFORMATION);
                        successAlert.setTitle("Registro Exitoso");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("¡Su cuenta ha sido creada exitosamente!");
                        successAlert.showAndWait();
                        
                        // Redirigir a la pantalla de inicio de sesión
                        handleIniciarSesion();
                    }
                });
                
            } catch (Exception e) {
                // Manejar errores en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    loadingAlert.close();
                    showAlert("Error en el registro", e.getMessage(), AlertType.ERROR);
                    registrarButton.setDisable(false);
                });
            }
        }).start();
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
            
            // Intentar cargar estilos solo si el archivo CSS existe
            String cssPath = "/styles/" + fxmlPath.replace("/views/", "").replace(".fxml", ".css");
            try {
                if (getClass().getResource(cssPath) != null) {
                    scene.getStylesheets().add(cssPath);
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar el archivo CSS: " + cssPath);
                e.printStackTrace();
            }
            
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
    
    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Obtener la ventana actual para mostrar la alerta centrada
        Window window = registrarButton.getScene().getWindow();
        alert.setX(window.getX() + (window.getWidth() - 400) / 2);
        alert.setY(window.getY() + (window.getHeight() - 200) / 2);
        
        alert.showAndWait();
    }
    
    private boolean esEmailValido(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }
    
    private String validarContrasena(String password) {
        if (password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "La contraseña debe contener al menos un número.";
        }
        
        // Opcional: Validar caracteres especiales si es necesario
        // if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
        //     return "La contraseña debe contener al menos un carácter especial.";
        // }
        
        return null; // Retorna null si la contraseña es válida
    }
}
