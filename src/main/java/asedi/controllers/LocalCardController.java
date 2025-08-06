package asedi.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import asedi.model.Local;
import asedi.services.LocalService;

public class LocalCardController implements Initializable {
    @FXML private VBox cardContainer;
    @FXML private ImageView imagenLocal;
    @FXML private Label nombreLabel;
    @FXML private Label tipoComercioLabel;
    @FXML private Label horarioLabel;
    @FXML private Label descripcionLabel;
    
    private Local local;
    private LocalesController parentController;
    private final LocalService localService = new LocalService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar imagen por defecto
        loadPlaceholderImage();
    }
    
    private void loadPlaceholderImage() {
        try (InputStream is = getClass().getResourceAsStream("/images/locales/localDummy.png")) {
            if (is != null) {
                Image placeholder = new Image(is);
                imagenLocal.setImage(placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setLocal(Local local) {
        this.local = local;
        actualizarVista();
    }
    
    private void actualizarVista() {
        if (local != null) {
            // Actualizar texto
            nombreLabel.setText(local.getNombre());
            tipoComercioLabel.setText(local.getTipoComercio());
            horarioLabel.setText(String.format("%s - %s", 
                local.getHorarioApertura() != null ? local.getHorarioApertura() : "", 
                local.getHorarioCierre() != null ? local.getHorarioCierre() : ""
            ));
            
            // Establecer descripción si existe
            if (local.getDescripcion() != null && !local.getDescripcion().isEmpty()) {
                descripcionLabel.setText(local.getDescripcion());
                descripcionLabel.setVisible(true);
            } else {
                descripcionLabel.setVisible(false);
            }
            
            // Cargar imagen si existe
            if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
                try {
                    // Usar un hilo separado para cargar la imagen
                    new Thread(() -> {
                        try {
                            Image image = new Image(local.getImagenUrl(), true); // true para carga en segundo plano
                            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                                if (newVal.doubleValue() == 1.0) {
                                    Platform.runLater(() -> imagenLocal.setImage(image));
                                }
                            });
                        } catch (Exception e) {
                            System.err.println("Error al cargar la imagen: " + e.getMessage());
                        }
                    }).start();
                } catch (Exception e) {
                    System.err.println("Error al cargar la imagen: " + e.getMessage());
                }
            }
            
            // Imagen por defecto
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/local-default.png"));
                imagenLocal.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen por defecto: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleEdit() {
        try {
            // Cargar el FXML del formulario de edición
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/editarLocal.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador y pasar el local a editar
            EditarLocalController controller = loader.getController();
            controller.setLocal(new Local(local)); // Usar una copia para no modificar el original directamente
            controller.setLocalesController(parentController);
            
            // Configurar la ventana de diálogo
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Editar Local");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al cargar el formulario de edición: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        // Mostrar confirmación antes de eliminar
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar este local?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean exito = localService.eliminarLocal(local.getId());
                if (exito) {
                    mostrarMensaje("Éxito", "El local se ha eliminado correctamente.", AlertType.INFORMATION);
                    // Notificar al controlador padre para que actualice la vista
                    if (parentController != null) {
                        parentController.recargarLocales();
                    }
                } else {
                    mostrarError("No se pudo eliminar el local. Intente nuevamente.");
                }
            } catch (Exception e) {
                mostrarError("Error al intentar eliminar el local: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarMensaje(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public void setParentController(LocalesController controller) {
        this.parentController = controller;
    }
}
