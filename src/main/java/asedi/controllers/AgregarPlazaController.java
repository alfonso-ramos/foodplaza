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
import javafx.scene.input.Dragboard;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import asedi.model.Plaza;
import asedi.services.PlazaService;

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
                btnEliminar.setOnAction(_ -> {
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
        
        // Crear diálogo de carga
        Alert loadingAlert = new Alert(AlertType.INFORMATION);
        loadingAlert.setTitle("Guardando plaza");
        loadingAlert.setHeaderText("Guardando plaza y subiendo imágenes...");
        loadingAlert.setContentText("Por favor espere...");
        loadingAlert.show();
        
        // Ejecutar en un hilo separado para no bloquear la interfaz
        new Thread(() -> {
            try {
                // Crear el servicio de plazas
                PlazaService plazaService = new PlazaService();
                
                // Crear el objeto plaza
                Plaza nuevaPlaza = new Plaza(nombre, direccion);
                
                // Guardar la plaza usando el servicio
                Long plazaId = plazaService.guardarPlazaYDevolverId(nuevaPlaza);
                
                if (plazaId != null) {
                    // Subir imágenes si hay alguna seleccionada
                    if (!imagenesSeleccionadas.isEmpty()) {
                        try {
                            // Subir solo la primera imagen (según los requisitos)
                            File imagen = imagenesSeleccionadas.get(0);
                            plazaService.subirImagenPlaza(plazaId, imagen, nombre);
                            
                            // Actualizar la UI en el hilo de JavaFX
                            javafx.application.Platform.runLater(() -> {
                                loadingAlert.close();
                                showAlert("Éxito", "Plaza guardada e imagen subida correctamente", AlertType.INFORMATION);
                                limpiarFormulario();
                            });
                        } catch (Exception e) {
                            // Si falla la subida de la imagen, mostramos un mensaje de advertencia
                            // pero consideramos que la plaza se guardó correctamente
                            System.err.println("Error al subir la imagen: " + e.getMessage());
                            javafx.application.Platform.runLater(() -> {
                                loadingAlert.close();
                                showAlert("Advertencia", 
                                    "La plaza se guardó correctamente, pero hubo un error al subir la imagen: " + e.getMessage(), 
                                    AlertType.WARNING);
                                limpiarFormulario();
                            });
                        }
                    } else {
                        // No hay imágenes para subir
                        javafx.application.Platform.runLater(() -> {
                            loadingAlert.close();
                            showAlert("Éxito", "Plaza guardada correctamente", AlertType.INFORMATION);
                            limpiarFormulario();
                        });
                    }
                } else {
                    throw new Exception("No se pudo guardar la plaza. Intente nuevamente.");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                // Mostrar error en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    loadingAlert.close();
                    errorLabel.setText("Error al guardar la plaza: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    public void cancelar() {
        volverAVistaPlazas();
    }
    
    /**
     * Limpia el formulario después de un guardado exitoso
     */
    private void limpiarFormulario() {
        nombreField.clear();
        direccionField.clear();
        imagenesSeleccionadas.clear();
        thumbnailsContainer.getChildren().clear();
        volverAVistaPlazas();
    }
    
    /**
     * Navega de vuelta a la vista de plazas
     */
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
