package asedi.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            System.out.println("Email: " + email + ", Password: " + password);
            // Aquí puedes validar el login con la base de datos
        });
    }
}
