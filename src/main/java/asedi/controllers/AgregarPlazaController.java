package asedi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AgregarPlazaController {

    @FXML
    private TextField nombreField;
    
    @FXML
    private TextField direccionField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private FlowPane thumbnailsContainer;
    
    private List<File> imagenesSeleccionadas = new ArrayList<>();
    
    @FXML
    public void initialize() {
        // Configurar el contenedor de miniaturas
        thumbnailsContainer.setHgap(10);
        thumbnailsContainer.setVgap(10);
        thumbnailsContainer.setPadding(new Insets(10));
    }
    
    @FXML
    private void seleccionarImagenes() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imágenes");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        // Permitir selección múltiple
        List<File> archivos = fileChooser.showOpenMultipleDialog(null);
        
        if (archivos != null && !archivos.isEmpty()) {
            if (imagenesSeleccionadas.size() + archivos.size() > 5) {
                mostrarError("Solo puedes subir un máximo de 5 imágenes");
                return;
            }
            
            for (File archivo : archivos) {
                if (archivo.length() > 5 * 1024 * 1024) { // 5MB
                    mostrarError("La imagen " + archivo.getName() + " excede el tamaño máximo de 5MB");
                    continue;
                }
                
                if (!imagenesSeleccionadas.contains(archivo)) {
                    imagenesSeleccionadas.add(archivo);
                }
            }
            
            actualizarVistaPrevia();
        }
    }
    
    private void actualizarVistaPrevia() {
        thumbnailsContainer.getChildren().clear();
        
        for (File archivo : imagenesSeleccionadas) {
            try {
                ImageView imageView = new ImageView(new Image(archivo.toURI().toString(), 100, 100, true, true));
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(false);
                
                // Botón para eliminar
                Button btnEliminar = new Button("X");
                btnEliminar.getStyleClass().add("delete-button");
                btnEliminar.setOnAction(e -> {
                    imagenesSeleccionadas.remove(archivo);
                    actualizarVistaPrevia();
                });
                
                StackPane stackPane = new StackPane(imageView, btnEliminar);
                StackPane.setAlignment(btnEliminar, javafx.geometry.Pos.TOP_RIGHT);
                
                thumbnailsContainer.getChildren().add(stackPane);
            } catch (Exception e) {
                mostrarError("Error al cargar la imagen: " + archivo.getName());
            }
        }
    }
    
    @FXML
    private void handleDragOver(javafx.scene.input.DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }
    
    @FXML
    private void handleDrop(javafx.scene.input.DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles()) {
            for (File file : db.getFiles()) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
                    fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                    
                    if (imagenesSeleccionadas.size() >= 5) {
                        mostrarError("Solo puedes subir un máximo de 5 imágenes");
                        break;
                    }
                    
                    if (file.length() > 5 * 1024 * 1024) {
                        mostrarError("La imagen " + file.getName() + " excede el tamaño máximo de 5MB");
                        continue;
                    }
                    
                    if (!imagenesSeleccionadas.contains(file)) {
                        imagenesSeleccionadas.add(file);
                        success = true;
                    }
                }
            }
            
            if (success) {
                actualizarVistaPrevia();
            }
        }
        
        event.setDropCompleted(success);
        event.consume();
    }
    
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
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
