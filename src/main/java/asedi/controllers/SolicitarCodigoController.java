package asedi.controllers;

import asedi.services.AuthService;
import asedi.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SolicitarCodigoController {

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    private void handleEnviarCodigo(ActionEvent event) {
        String email = emailField.getText();
        if (email.isEmpty()) {
            AlertUtils.mostrarError("Error", "El campo de correo electrónico no puede estar vacío.");
            return;
        }

        try {
            authService.solicitarCodigoRecuperacion(email);
            messageLabel.setText("Se ha enviado un código de verificación a tu correo electrónico.");
            
            // Navegar a la siguiente vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asedi/views/verificarCodigo.fxml"));
            Parent root = loader.load();
            VerificarCodigoController controller = loader.getController();
            controller.setEmail(email);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", e.getMessage());
        }
    }
}
