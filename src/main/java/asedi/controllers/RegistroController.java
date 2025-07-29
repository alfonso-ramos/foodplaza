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

    @FXML
    private void initialize() {
        // Configurar el botón de registro
        registrarButton.setOnAction(e -> registrarUsuario());
        
        // Configurar el enlace para regresar al login
        regresarLink.setOnAction(e -> {
            try {
                // Cargar la vista de login
                Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
                
                // Obtener la escena actual
                Scene scene = regresarLink.getScene();
                if (scene == null) {
                    // Si no hay escena, crear una nueva
                    scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("FoodPlaza - Inicio de Sesión");
                    stage.show();
                } else {
                    // Si ya hay una escena, usarla
                    scene.setRoot(root);
                    Stage stage = (Stage) scene.getWindow();
                    stage.setTitle("FoodPlaza - Inicio de Sesión");
                }
            } catch (IOException ex) {
                System.err.println("Error al cargar la vista de login: " + ex.getMessage());
                ex.printStackTrace();
                // Mostrar mensaje de error al usuario
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No se pudo cargar la pantalla de inicio de sesión. Por favor, intente nuevamente.");
                alert.showAndWait();
            }
        });
    }

    private void registrarUsuario() {
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmarPassword = confirmarPasswordField.getText();

        // Validar campos vacíos
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, completa todos los campos.");
            return;
        }

        // Validar correo
        if (!esEmailValido(email)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo inválido", "Ingresa un correo electrónico válido.");
            return;
        }

        // Validar contraseñas iguales
        if (!password.equals(confirmarPassword)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Contraseñas no coinciden", "Las contraseñas no coinciden. Intenta de nuevo.");
            return;
        }

        // Simular registro exitoso
        mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso", "Usuario registrado correctamente.");
        
        // Aquí podrías guardar en base de datos o continuar el flujo
    }

    private boolean esEmailValido(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
