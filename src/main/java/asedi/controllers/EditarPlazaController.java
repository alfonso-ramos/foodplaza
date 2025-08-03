package asedi.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import asedi.model.Plaza;
import asedi.services.PlazaService;
import asedi.utils.SuppressWarningsUtil;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditarPlazaController implements Initializable {

    @FXML private TextField nombreField;
    @FXML private TextField direccionField;
    @FXML private ComboBox<String> estadoCombo;
    @FXML private ImageView imagenActual;
    @FXML private Label sinImagenLabel;
    @FXML private Label nombreArchivoLabel;
    @FXML private ImageView vistaPreviaImagen;
    @FXML private VBox vistaPreviaContainer;
    @FXML private Label errorLabel;
    
    private Plaza plaza;
    private File imagenSeleccionada;
    private final PlazaService plazaService = new PlazaService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar estilos
        try {
            // Obtener el nodo raíz (BorderPane)
            BorderPane root = (BorderPane) nombreField.getScene().getRoot();
            
            // Cargar estilos
            String mainCss = getClass().getResource("/styles/main.css").toExternalForm();
            String plazasCss = getClass().getResource("/styles/plazas.css").toExternalForm();
            
            root.getStylesheets().addAll(mainCss, plazasCss);
        } catch (Exception e) {
            System.err.println("Error al cargar estilos: " + e.getMessage());
        }
        
        // Inicializar el ComboBox con los estados
        estadoCombo.getItems().clear();
        estadoCombo.getItems().addAll("activo", "inactivo");
        estadoCombo.getSelectionModel().selectFirst();
        
        // Inicializar la interfaz
        limpiarFormulario();
    }
    
    public void setPlaza(Plaza plaza) {
        this.plaza = plaza;
        cargarDatosPlaza();
    }
    
    private void cargarDatosPlaza() {
        if (plaza != null) {
            nombreField.setText(plaza.getNombre());
            direccionField.setText(plaza.getDireccion());
            estadoCombo.setValue(plaza.getEstado() != null ? plaza.getEstado() : "activo");
            
            // Cargar imagen actual si existe
            if (plaza.getImagenUrl() != null && !plaza.getImagenUrl().isEmpty()) {
                try {
                    Image imagen = new Image(plaza.getImagenUrl(), true);
                    imagen.progressProperty().addListener((obs, oldVal, newVal) -> {
                        SuppressWarningsUtil.unused(obs, oldVal);
                        if (newVal.doubleValue() == 1.0) {
                            Platform.runLater(() -> {
                                imagenActual.setImage(imagen);
                                sinImagenLabel.setVisible(false);
                            });
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error al cargar la imagen: " + e.getMessage());
                    sinImagenLabel.setVisible(true);
                }
            } else {
                sinImagenLabel.setVisible(true);
            }
        }
    }
    
    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File archivo = fileChooser.showOpenDialog(nombreField.getScene().getWindow());
        if (archivo != null) {
            if (archivo.length() > 5 * 1024 * 1024) { // 5MB
                mostrarError("La imagen no debe superar los 5MB");
                return;
            }
            
            imagenSeleccionada = archivo;
            nombreArchivoLabel.setText(archivo.getName());
            
            // Mostrar vista previa
            Image imagen = new Image(archivo.toURI().toString());
            vistaPreviaImagen.setImage(imagen);
            vistaPreviaContainer.setVisible(true);
        }
    }
    
    @FXML
    private void guardarCambios() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        // Mostrar diálogo de confirmación
        Alert confirmDialog = new Alert(
            AlertType.CONFIRMATION,
            "¿Está seguro de que desea guardar los cambios?",
            ButtonType.YES, ButtonType.NO
        );
        confirmDialog.setHeaderText("Confirmar cambios");
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Actualizar los datos de la plaza
                    plaza.setNombre(nombreField.getText().trim());
                    plaza.setDireccion(direccionField.getText().trim());
                    plaza.setEstado(estadoCombo.getValue());
                    
                    // Actualizar la plaza en el servidor
                    boolean exito = plazaService.actualizarPlaza(plaza);
                    
                    if (exito) {
                        // Si hay una nueva imagen, subirla
                        if (imagenSeleccionada != null) {
                            // Aquí iría la lógica para subir la nueva imagen
                            // Esto dependerá de cómo esté implementado tu servicio de imágenes
                        }
                        
                        // Mostrar mensaje de éxito y cerrar la ventana
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Plaza actualizada");
                        alert.setHeaderText(null);
                        alert.setContentText("Los cambios se han guardado correctamente.");
                        alert.showAndWait();
                        cerrarVentana();
                    } else {
                        mostrarError("No se pudo actualizar la plaza. Por favor, intente nuevamente.");
                    }
                } catch (Exception e) {
                    mostrarError("Error al actualizar la plaza: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void cancelar() {
        // Mostrar diálogo de confirmación si hay cambios sin guardar
        if (hayCambiosSinGuardar()) {
            Alert confirmDialog = new Alert(
                AlertType.CONFIRMATION,
                "¿Está seguro de que desea salir sin guardar los cambios?",
                ButtonType.YES, ButtonType.NO
            );
            confirmDialog.setHeaderText("Descartar cambios");
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    cerrarVentana();
                }
            });
        } else {
            cerrarVentana();
        }
    }
    
    private boolean hayCambiosSinGuardar() {
        if (plaza == null) return false;
        
        // Verificar si hay cambios en los campos
        boolean cambiosEnCampos = !nombreField.getText().equals(plaza.getNombre()) ||
                               !direccionField.getText().equals(plaza.getDireccion()) ||
                               !estadoCombo.getValue().equals(plaza.getEstado());
        
        // Verificar si se seleccionó una nueva imagen
        boolean cambiosEnImagen = imagenSeleccionada != null;
        
        return cambiosEnCampos || cambiosEnImagen;
    }
    
    private boolean validarCampos() {
        // Limpiar mensajes de error
        errorLabel.setVisible(false);
        
        // Validar nombre
        if (nombreField.getText() == null || nombreField.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        
        // Validar dirección
        if (direccionField.getText() == null || direccionField.getText().trim().isEmpty()) {
            mostrarError("La dirección es obligatoria");
            return false;
        }
        
        // Validar estado
        if (estadoCombo.getValue() == null || estadoCombo.getValue().trim().isEmpty()) {
            mostrarError("Debe seleccionar un estado");
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }
    
    private void limpiarFormulario() {
        nombreField.clear();
        direccionField.clear();
        estadoCombo.getSelectionModel().select("activo");
        imagenActual.setImage(null);
        vistaPreviaImagen.setImage(null);
        vistaPreviaContainer.setVisible(false);
        errorLabel.setVisible(false);
        sinImagenLabel.setVisible(false);
        nombreArchivoLabel.setText("Ningún archivo seleccionado");
        imagenSeleccionada = null;
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) nombreField.getScene().getWindow();
        stage.close();
    }
}
