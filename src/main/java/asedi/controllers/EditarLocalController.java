package asedi.controllers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import java.util.ResourceBundle;

import asedi.model.Local;
import asedi.services.LocalService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditarLocalController implements Initializable {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtHorarioApertura;
    @FXML private TextField txtHorarioCierre;
    @FXML private ComboBox<String> cmbTipoComercio;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ImageView imagenActual;
    @FXML private ImageView vistaPreviaImagen;
    @FXML private Label nombreArchivoLabel;
    @FXML private Label sinImagenLabel;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private VBox vistaPreviaContainer;
    @FXML private Button btnEliminarImagen;
    @FXML private Button btnSeleccionarImagen;
    
    private Local local;
    private LocalesController localesController;
    private LocalService localService;
    private File imagenSeleccionada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el servicio si no está ya inicializado
        if (localService == null) {
            localService = new LocalService();
        }
        
        // Configurar ComboBox de tipos de comercio
        cmbTipoComercio.getItems().addAll(
            "Restaurante", "Cafetería", "Tienda de ropa", 
            "Supermercado", "Farmacia", "Otro"
        );
        
        // Configurar ComboBox de estados
        cmbEstado.getItems().addAll("Activo", "Inactivo", "En mantenimiento");
    }
    
    @FXML
    private void initialize() {
        // Configurar los botones
        btnGuardar.setOnAction(event -> guardarCambios());
        btnCancelar.setOnAction(event -> cerrarVentana());
        btnEliminarImagen.setOnAction(event -> eliminarImagen());
        
        // Configurar el botón de seleccionar imagen
        if (btnSeleccionarImagen != null) {
            btnSeleccionarImagen.setOnAction(event -> seleccionarImagen());
        }
    }
    
    public void setLocal(Local local) {
        this.local = local;
        cargarDatosLocal();
    }
    
    public void setLocalesController(LocalesController controller) {
        this.localesController = controller;
        
        // Inicializar el servicio si es necesario
        if (localService == null) {
            localService = new LocalService();
        }
    }
    

    
    private void cargarDatosLocal() {
        if (local != null) {
            // Cargar datos básicos
            txtNombre.setText(local.getNombre());
            txtDescripcion.setText(local.getDescripcion());
            txtHorarioApertura.setText(local.getHorarioApertura());
            txtHorarioCierre.setText(local.getHorarioCierre());
            txtDireccion.setText(local.getDireccion());
            
            // Establecer valores en los ComboBox
            if (local.getTipoComercio() != null) {
                cmbTipoComercio.setValue(local.getTipoComercio());
            }
            if (local.getEstado() != null) {
                cmbEstado.setValue(local.getEstado());
            }
            
            // Manejar la imagen
            if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
                // Cargar la imagen existente
                cargarImagen(local.getImagenUrl());
                
                // Actualizar la interfaz
                if (nombreArchivoLabel != null) {
                    nombreArchivoLabel.setText(obtenerNombreArchivo(local.getImagenUrl()));
                }
                if (btnEliminarImagen != null) {
                    btnEliminarImagen.setDisable(false);
                }
                if (sinImagenLabel != null) {
                    sinImagenLabel.setVisible(false);
                }
                
                // Ocultar el contenedor de vista previa
                if (vistaPreviaContainer != null) {
                    vistaPreviaContainer.setVisible(false);
                }
            } else {
                // No hay imagen, mostrar placeholder
                loadPlaceholderImage();
                
                // Actualizar la interfaz
                if (nombreArchivoLabel != null) {
                    nombreArchivoLabel.setText("Ningún archivo seleccionado");
                }
                if (btnEliminarImagen != null) {
                    btnEliminarImagen.setDisable(true);
                }
                if (sinImagenLabel != null) {
                    sinImagenLabel.setVisible(true);
                }
                
                // Ocultar el contenedor de vista previa
                if (vistaPreviaContainer != null) {
                    vistaPreviaContainer.setVisible(false);
                }
            }
        }
    }
    
    private Image loadPlaceholderImage() {
        try (var is = getClass().getResourceAsStream("/images/local-placeholder.png")) {
            return new Image(is);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen por defecto: " + e.getMessage());
            return null;
        }
    }
    
    private void cargarImagen(String url) {
        try {
            Image image = new Image(url, true); // Carga asíncrona
            imagenActual.setImage(image);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen: " + e.getMessage());
            imagenActual.setImage(loadPlaceholderImage());
        }
    }
    
    @FXML
    private void eliminarImagen() {
        // Si hay una imagen seleccionada pero no guardada, solo la quitamos de la vista previa
        if (imagenSeleccionada != null) {
            if (vistaPreviaImagen != null) {
                vistaPreviaImagen.setImage(null);
            }
            if (vistaPreviaContainer != null) {
                vistaPreviaContainer.setVisible(false);
            }
            if (nombreArchivoLabel != null) {
                nombreArchivoLabel.setText("Ningún archivo seleccionado");
            }
            imagenSeleccionada = null;
        } 
        // Si hay una imagen guardada, la marcamos para eliminación
        if (local != null && local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
            // Mostrar la imagen por defecto
            if (imagenActual != null) {
                imagenActual.setImage(loadPlaceholderImage());
            }
            // Mostrar el mensaje de sin imagen
            if (sinImagenLabel != null) {
                sinImagenLabel.setVisible(true);
            }
            // Deshabilitar el botón de eliminar
            if (btnEliminarImagen != null) {
                btnEliminarImagen.setDisable(true);
            }
            // Limpiar la URL de la imagen para que se elimine al guardar
            local.setImagenUrl(null);
            local.setImagenPublicId(null);
        }
    }
    
    private String obtenerNombreArchivo(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int lastSlash = url.lastIndexOf('/');
        return lastSlash != -1 ? url.substring(lastSlash + 1) : url;
    }
    
    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen del local");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) btnSeleccionarImagen.getScene().getWindow();
        File archivo = fileChooser.showOpenDialog(stage);
        
        if (archivo != null) {
            try {
                // Cargar la imagen seleccionada
                Image imagen = new Image(archivo.toURI().toString());
                
                // Mostrar la vista previa
                if (vistaPreviaImagen != null) {
                    vistaPreviaImagen.setImage(imagen);
                }
                if (vistaPreviaContainer != null) {
                    vistaPreviaContainer.setVisible(true);
                }
                
                // Actualizar la interfaz
                if (nombreArchivoLabel != null) {
                    nombreArchivoLabel.setText(archivo.getName());
                }
                
                imagenSeleccionada = archivo;
                
                // Si hay una imagen actual, habilitar el botón de eliminar
                if (btnEliminarImagen != null) {
                    btnEliminarImagen.setDisable(false);
                }
                
            } catch (Exception e) {
                mostrarError("Error al cargar la imagen: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void guardarCambios() {
        // Validar campos obligatorios
        if (txtNombre.getText().trim().isEmpty() || 
            cmbTipoComercio.getValue() == null || 
            txtDireccion.getText().trim().isEmpty() ||
            cmbEstado.getValue() == null) {
            mostrarError("Por favor complete todos los campos obligatorios");
            return;
        }

        try {
            // Actualizar los datos del local
            local.setNombre(txtNombre.getText().trim());
            local.setDescripcion(txtDescripcion.getText().trim());
            local.setDireccion(txtDireccion.getText().trim());
            local.setHorarioApertura(txtHorarioApertura.getText().trim());
            local.setHorarioCierre(txtHorarioCierre.getText().trim());
            local.setTipoComercio(cmbTipoComercio.getValue());
            local.setEstado(cmbEstado.getValue());

            // Lista para nuevas imágenes (si se seleccionó una)
            List<File> nuevasImagenes = new ArrayList<>();
            if (imagenSeleccionada != null) {
                // Si hay una nueva imagen seleccionada, la agregamos
                nuevasImagenes.add(imagenSeleccionada);
            } else if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty() && 
                      btnEliminarImagen != null && btnEliminarImagen.isDisabled()) {
                // Si había una imagen pero el botón de eliminar está deshabilitado, significa que se eliminó
                local.setImagenUrl(null);
                local.setImagenPublicId(null);
            }
            
            // Deshabilitar botones durante la operación
            btnGuardar.setDisable(true);
            btnCancelar.setDisable(true);
            
            // Mostrar indicador de carga
            btnGuardar.setText("Guardando...");
            
            // Ejecutar en un hilo separado para no bloquear la interfaz
            new Thread(() -> {
                try {
                    boolean exito = localService.actualizarLocal(local, nuevasImagenes);
                    
                    Platform.runLater(() -> {
                        if (exito) {
                            mostrarMensaje("Éxito", "El local se ha actualizado correctamente.", AlertType.INFORMATION);
                            if (localesController != null) {
                                localesController.recargarLocales();
                            }
                            cerrarVentana();
                        } else {
                            mostrarError("No se pudo actualizar el local. Intente nuevamente.");
                            btnGuardar.setDisable(false);
                            btnCancelar.setDisable(false);
                            btnGuardar.setText("Guardar Cambios");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        mostrarError("Error al actualizar el local: " + e.getMessage());
                        e.printStackTrace();
                        btnGuardar.setDisable(false);
                        btnCancelar.setDisable(false);
                        btnGuardar.setText("Guardar Cambios");
                    });
                }
            }).start();
            
        } catch (Exception e) {
            mostrarError("Error al actualizar el local: " + e.getMessage());
            e.printStackTrace();
            btnGuardar.setDisable(false);
            btnCancelar.setDisable(false);
            btnGuardar.setText("Guardar Cambios");
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
    
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
