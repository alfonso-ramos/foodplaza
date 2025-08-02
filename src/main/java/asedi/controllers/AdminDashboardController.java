package asedi.controllers;

import asedi.services.AuthService;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML
    private StackPane contenidoPane;
    
    @FXML
    private Label userLabel;

    @FXML
    public void initialize() {
        // Mostrar información del usuario actual
        AuthService authService = AuthService.getInstance();
        if (authService.getCurrentUser() != null) {
            userLabel.setText(authService.getCurrentUser().getNombre());
        }
        
        // Mostrar un mensaje de bienvenida cuando se inicia el dashboard
        mostrarMensajeBienvenida();
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        try {
            // Cerrar sesión
            AuthService.getInstance().logout();
            
            // Cerrar la ventana actual
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            
            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            
            stage.setScene(new Scene(root));
            stage.setTitle("Inicio de Sesión - FoodPlaza");
            stage.setMaximized(false);
            stage.centerOnScreen();
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
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Text welcomeText = new Text("Bienvenido al Panel de Administración");
        welcomeText.setStyle("-fx-font-size: 28px; -fx-fill: #2c3e50; -fx-font-weight: bold;");
        
        Text instructionText = new Text("Seleccione una opción del menú para comenzar");
        instructionText.setStyle("-fx-font-size: 16px; -fx-fill: #7f8c8d;");
        
        welcomeBox.getChildren().addAll(welcomeText, instructionText);
        contenidoPane.getChildren().setAll(welcomeBox);
    }
    
    @FXML
    public void cargarInicio() {
        mostrarMensajeBienvenida();
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
    public void cargarLocales() {
        try {
            // Cargar la vista de gestión de locales
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/locales.fxml"));
            Parent localesView = loader.load();
            contenidoPane.getChildren().setAll(localesView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de locales", AlertType.ERROR);
        }
    }
    
    @FXML 
    public void cargarAsignarGerencia() {
        try {
            // Cargar la vista de asignación de gerencia
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/asignarGerencia.fxml"));
            Parent asignarGerenciaView = loader.load();
            contenidoPane.getChildren().setAll(asignarGerenciaView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de asignar gerencia", AlertType.ERROR);
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
