package asedi.controllers;

import asedi.services.AuthService;
import asedi.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class VerificarCodigoController {

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private Label messageLabel;

    private String email;

    private final AuthService authService = AuthService.getInstance();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleCambiarPassword(ActionEvent event) {
        String code = codeField.getText();
        String newPassword = newPasswordField.getText();

        if (code.isEmpty() || newPassword.isEmpty()) {
            AlertUtils.mostrarError("Error", "El código y la nueva contraseña no pueden estar vacíos.");
            return;
        }

        try {
            authService.verificarCodigoYCambiarPassword(email, code, newPassword);
            AlertUtils.mostrarInformacion("Éxito", "Contraseña actualizada exitosamente.");
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", e.getMessage());
        }
    }
}
