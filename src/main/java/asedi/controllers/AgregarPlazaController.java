package asedi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AgregarPlazaController {

    @FXML
    private TextField nombreField;
    
    @FXML
    private TextField direccionField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    public void initialize() {
        // Inicialización básica si es necesaria
    }
    
    @FXML
    public void guardarPlaza() {
        // Limpiar mensajes de error previos
        errorLabel.setText("");
        
        // Obtener los valores del formulario
        String nombre = nombreField.getText().trim();
        String direccion = direccionField.getText().trim();
        
        // Validar campos requeridos
        if (nombre.isEmpty() || direccion.isEmpty()) {
            errorLabel.setText("Por favor complete todos los campos");
            return;
        }
        
        // Validaciones adicionales
        if (nombre.length() < 3) {
            errorLabel.setText("El nombre debe tener al menos 3 caracteres");
            return;
        }
        
        if (direccion.length() < 5) {
            errorLabel.setText("La dirección debe tener al menos 5 caracteres");
            return;
        }
        
        try {
            // Aquí iría la lógica para guardar la plaza en la base de datos
            // Por ahora, solo mostramos un mensaje de éxito
            System.out.println("Guardando plaza: " + nombre + ", " + direccion);
            
            // Mostrar mensaje de éxito
            showAlert("Éxito", "Plaza guardada correctamente", AlertType.INFORMATION);
            
            // Volver a la vista de plazas
            volverAVistaPlazas();
            
        } catch (Exception e) {
            errorLabel.setText("Error al guardar la plaza: " + e.getMessage());
            e.printStackTrace();
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
