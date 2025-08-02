package asedi.controllers;

import asedi.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class UsuarioDashboardController {
    @FXML private Label userNameLabel;
    @FXML private StackPane contenidoPane;

    @FXML
    public void initialize() {
        // Mostrar el nombre del usuario actual
        AuthService authService = AuthService.getInstance();
        if (authService.getCurrentUser() != null) {
            userNameLabel.setText(authService.getCurrentUser().getNombre());
        }
    }

    @FXML
    public void cargarInicio() {
        // Mostrar mensaje de bienvenida
        mostrarMensaje("Bienvenido a FoodPlaza");
    }

    @FXML
    public void cargarPerfil() {
        mostrarMensaje("Perfil de Usuario\n(En desarrollo)");
    }

    @FXML
    public void cargarReservas() {
        mostrarMensaje("Mis Reservas\n(En desarrollo)");
    }

    @FXML
    public void cerrarSesion() {
        try {
            // Cerrar sesión
            AuthService.getInstance().logout();
            
            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) contenidoPane.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Inicio de Sesión - FoodPlaza");
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(String mensaje) {
        contenidoPane.getChildren().clear();
        
        // Crear un mensaje simple
        javafx.scene.control.Label label = new javafx.scene.control.Label(mensaje);
        label.setStyle("-fx-font-size: 16px; -fx-alignment: center; -fx-text-alignment: center;");
        
        contenidoPane.getChildren().add(label);
    }
}
