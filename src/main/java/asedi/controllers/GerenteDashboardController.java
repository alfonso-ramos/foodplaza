package asedi.controllers;

import asedi.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GerenteDashboardController {
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
    public void cargarPanelPrincipal() {
        cargarVista("gerente/panelPrincipal");
    }

    @FXML
    public void cargarLocales() {
        cargarVista("gerente/locales");
    }

    @FXML
    public void cargarReportes() {
        cargarVista("gerente/reportes");
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

    private void cargarVista(String vista) {
        try {
            // En un entorno real, aquí cargaríamos la vista correspondiente
            // Por ahora mostramos un mensaje de "en desarrollo"
            contenidoPane.getChildren().clear();
            
            // Crear un mensaje simple
            javafx.scene.control.Label mensaje = new javafx.scene.control.Label("Vista de " + vista + "\n(En desarrollo)");
            mensaje.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
            
            contenidoPane.getChildren().add(mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
