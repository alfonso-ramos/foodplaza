package asedi.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class PlazasController {

    @FXML
    public void initialize() {
        // Inicialización si es necesaria
    }
    
    @FXML
    public void cargarAgregarPlaza(ActionEvent event) {
        try {
            // Cargar la vista para agregar plaza
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarPlaza.fxml"));
            Parent agregarPlazaView = loader.load();
            
            // Obtener el nodo que disparó el evento
            Node sourceNode = (Node) event.getSource();
            
            // Obtener el contenido principal del dashboard y limpiarlo
            StackPane contenidoPane = (StackPane) sourceNode.getScene().lookup("#contenidoPane");
            if (contenidoPane != null) {
                contenidoPane.getChildren().setAll(agregarPlazaView);
            } else {
                // Si no se encuentra el contenidoPane, intentar cargar la vista de otra manera
                Stage stage = (Stage) sourceNode.getScene().getWindow();
                Scene scene = new Scene(agregarPlazaView);
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de agregar plaza: " + e.getMessage(), AlertType.ERROR);
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
