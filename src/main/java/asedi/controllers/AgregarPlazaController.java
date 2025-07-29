package asedi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AgregarPlazaController {

    @FXML
    private TextField nombreField;
    
    @FXML
    private TextField ubicacionField;
    
    @FXML
    private TextField capacidadField;
    
    @FXML
    public void initialize() {
        // Inicialización si es necesaria
    }
    
    @FXML
    public void guardarPlaza() {
        // Validar campos
        if (nombreField.getText().isEmpty() || ubicacionField.getText().isEmpty() || capacidadField.getText().isEmpty()) {
            showAlert("Error", "Por favor complete todos los campos", AlertType.ERROR);
            return;
        }
        
        try {
            // Validar que la capacidad sea un número
            int capacidad = Integer.parseInt(capacidadField.getText());
            
            // Aquí iría la lógica para guardar la plaza en la base de datos
            // Por ahora, solo mostramos un mensaje de éxito
            showAlert("Éxito", "Plaza guardada correctamente", AlertType.INFORMATION);
            
            // Volver a la vista de plazas
            volverAVistaPlazas();
            
        } catch (NumberFormatException e) {
            showAlert("Error", "La capacidad debe ser un número válido", AlertType.ERROR);
        }
    }
    
    @FXML
    public void cancelar() {
        volverAVistaPlazas();
    }
    
    private void volverAVistaPlazas() {
        try {
            // Obtener el nodo actual
            Node sourceNode = nombreField;
            if (sourceNode == null || sourceNode.getScene() == null) {
                throw new IllegalStateException("No se pudo obtener la escena actual");
            }
            
            // Buscar el contenido principal del dashboard
            StackPane contenidoPane = (StackPane) sourceNode.getScene().lookup("#contenidoPane");
            if (contenidoPane != null) {
                // Cargar la vista de plazas dentro del contenido principal
                Parent plazasView = FXMLLoader.load(getClass().getResource("/views/plazas.fxml"));
                contenidoPane.getChildren().setAll(plazasView);
            } else {
                // Si no se encuentra el contenidoPane, cargar la vista de plazas en una nueva escena
                Stage stage = (Stage) sourceNode.getScene().getWindow();
                Parent plazasView = FXMLLoader.load(getClass().getResource("/views/plazas.fxml"));
                Scene scene = new Scene(plazasView);
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo volver a la vista de plazas: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
