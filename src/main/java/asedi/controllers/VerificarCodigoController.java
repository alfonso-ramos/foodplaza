package asedi.controllers;

import asedi.services.AuthService;
import asedi.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class VerificarCodigoController {

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private Label messageLabel;
    
    @FXML
    private Button cambiarPasswordButton;

    private String email;
    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        // Inicialización si es necesaria
    }

    public void setEmail(String email) {
        this.email = email;
        messageLabel.setText("Se ha enviado un código a: " + email);
    }

    @FXML
    private void handleCambiarPassword(ActionEvent event) {
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (code.isEmpty() || newPassword.isEmpty()) {
            AlertUtils.mostrarError("Error", "Todos los campos son obligatorios.");
            return;
        }
        
        if (newPassword.length() < 6) {
            AlertUtils.mostrarError("Error", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        try {
            // Deshabilitar el botón para evitar múltiples envíos
            cambiarPasswordButton.setDisable(true);
            messageLabel.setText("Procesando...");
            
            // Ejecutar en un hilo separado para no bloquear la interfaz
            new Thread(() -> {
                try {
                    authService.verificarCodigoYCambiarPassword(email, code, newPassword);
                    // Actualizar la UI en el hilo de JavaFX
                    javafx.application.Platform.runLater(() -> {
                        AlertUtils.mostrarInformacion("Éxito", "Contraseña actualizada correctamente. Serás redirigido al inicio de sesión.");
                        // Redirigir a la pantalla de login
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                            Parent root = loader.load();
                            Stage stage = (Stage) cambiarPasswordButton.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    // Manejar errores en el hilo de JavaFX
                    javafx.application.Platform.runLater(() -> {
                        AlertUtils.mostrarError("Error", "No se pudo actualizar la contraseña: " + e.getMessage());
                        cambiarPasswordButton.setDisable(false);
                        messageLabel.setText("");
                    });
                }
            }).start();
            
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Ocurrió un error inesperado: " + e.getMessage());
            cambiarPasswordButton.setDisable(false);
            messageLabel.setText("");
        }
    }
}
