package asedi.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML
    private StackPane contenidoPane;

    @FXML
    public void initialize() {
        // Mostrar un mensaje de bienvenida cuando se inicia el dashboard
        mostrarMensajeBienvenida();
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        try {
            // Cerrar la ventana actual
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            
            // Cargar la vista de login
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inicio de Sesión - FoodPlaza");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de login", AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void mostrarMensajeBienvenida() {
        Label mensaje = new Label("Bienvenido al Panel de Administración\n\nSeleccione una opción del menú para comenzar.");
        mensaje.setStyle("-fx-font-size: 16px; -fx-text-alignment: center;");
        contenidoPane.getChildren().setAll(mensaje);
    }

    @FXML 
    public void cargarPlazas() {
        try {
            // Cargar la vista de gestión de plazas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/plazas.fxml"));
            Parent plazasView = loader.load();
            contenidoPane.getChildren().setAll(plazasView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de plazas", AlertType.ERROR);
        }
    }
    
    @FXML 
    public void cargarAgregarPlaza() {
        try {
            // Cargar la vista para agregar plaza
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarPlaza.fxml"));
            Parent agregarPlazaView = loader.load();
            contenidoPane.getChildren().setAll(agregarPlazaView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de agregar plaza", AlertType.ERROR);
        }
    }
    
    @FXML 
    public void cargarAgregarLocal() {
        try {
            // Cargar la vista para agregar local
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarLocal.fxml"));
            Parent agregarLocalView = loader.load();
            contenidoPane.getChildren().setAll(agregarLocalView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de agregar local", AlertType.ERROR);
        }
    }
    
    @FXML 
    public void cargarAgregarGerentes() {
        try {
            // Cargar la vista para agregar gerentes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarGerente.fxml"));
            Parent agregarGerenteView = loader.load();
            contenidoPane.getChildren().setAll(agregarGerenteView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de agregar gerente", AlertType.ERROR);
        }
    }
    

}
