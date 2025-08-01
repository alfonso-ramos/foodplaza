package asedi.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import asedi.model.Local;
import asedi.model.Plaza;
import asedi.services.LocalService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class AgregarLocalController {
    @FXML private Pane contentPane;
    @FXML private ComboBox<Plaza> plazaCombo;
    @FXML private TextField nombreField;
    @FXML private TextArea descripcionField;
    @FXML private TextField direccionField;
    @FXML private TextField horaAperturaField;
    @FXML private TextField horaCierreField;
    @FXML private ComboBox<String> tipoComercioCombo;
    @FXML private ComboBox<String> estadoCombo;
    @FXML private FlowPane thumbnailsContainer;
    @FXML private Label errorLabel;
    
    private List<File> imagenesSeleccionadas = new ArrayList<>();
    @FXML
    public void initialize() {
        // Configurar opciones del ComboBox de tipo de comercio
        tipoComercioCombo.getItems().addAll(
            "Restaurante",
            "Cafetería",
            "Tienda",
            "Servicios",
            "Otro"
        );
        
        // Configurar opciones del ComboBox de estado
        estadoCombo.getItems().addAll(
            "activo",
            "inactivo",
            "en_mantenimiento"
        );
        
        // Seleccionar "activo" por defecto
        estadoCombo.setValue("activo");
        
        // Configurar validaciones
        configurarValidaciones();
    }
    
    public void setPlazas(List<Plaza> plazas) {
        plazaCombo.getItems().setAll(plazas);
        
        if (!plazas.isEmpty()) {
            plazaCombo.getSelectionModel().selectFirst();
        }
    }
    
    private void configurarValidaciones() {
        // Validar formato de hora (HH:mm)
        horaAperturaField.textProperty().addListener((observable, oldValue, newVal) -> {
            if (!newVal.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                horaAperturaField.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1px;");
            } else {
                horaAperturaField.setStyle("");
            }
        });
        
        horaCierreField.textProperty().addListener((observable, oldValue, newVal) -> {
            if (!newVal.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                horaCierreField.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1px;");
            } else {
                horaCierreField.setStyle("");
            }
        });
    }
    
    @FXML
    private void seleccionarImagenes() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imágenes");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        List<File> archivos = fileChooser.showOpenMultipleDialog(null);
        
        if (archivos != null) {
            for (File archivo : archivos) {
                if (imagenesSeleccionadas.size() >= 5) {
                    mostrarError("Solo puedes seleccionar un máximo de 5 imágenes");
                    break;
                }
                
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
        
        for (File imagen : imagenesSeleccionadas) {
            try {
                ImageView imageView = new ImageView(new Image(imagen.toURI().toString()));
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                
                // Botón para eliminar la imagen
                Button btnEliminar = new Button("×");
                btnEliminar.setStyle(
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 50%; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20;"
                );
                
                // Posicionar el botón en la esquina superior derecha
                StackPane stackPane = new StackPane(imageView, btnEliminar);
                StackPane.setAlignment(btnEliminar, Pos.TOP_RIGHT);
                
                // Configurar acción de eliminar
                btnEliminar.setOnAction(event -> {
                    imagenesSeleccionadas.remove(imagen);
                    actualizarVistaPrevia();
                });
                
                thumbnailsContainer.getChildren().add(stackPane);
                
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void guardarLocal() {
        if (validarCampos()) {
            try {
                // Crear el objeto Local con los datos del formulario
                Local local = new Local();
                local.setPlazaId(plazaCombo.getValue().getId());
                local.setNombre(nombreField.getText().trim());
                local.setDescripcion(descripcionField.getText().trim());
                local.setDireccion(direccionField.getText().trim());
                local.setHorarioApertura(horaAperturaField.getText().trim());
                local.setHorarioCierre(horaCierreField.getText().trim());
                local.setTipoComercio(tipoComercioCombo.getValue());
                
                // Llamar al servicio para guardar el local
                LocalService localService = new LocalService();
                boolean exito = localService.guardarLocal(local, imagenesSeleccionadas);
                
                if (exito) {
                    // Cerrar la ventana si se guardó correctamente
                    ((Stage) nombreField.getScene().getWindow()).close();
                } else {
                    mostrarError("Error al guardar el local. Por favor, inténtalo de nuevo.");
                }
                
            } catch (Exception e) {
                mostrarError("Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean validarCampos() {
        if (plazaCombo.getValue() == null) {
            mostrarError("Debes seleccionar una plaza");
            return false;
        }
        
        if (nombreField.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        
        if (direccionField.getText().trim().isEmpty()) {
            mostrarError("La dirección es obligatoria");
            return false;
        }
        
        if (horaAperturaField.getText().trim().isEmpty()) {
            mostrarError("El horario de apertura es obligatorio");
            return false;
        }
        
        if (horaCierreField.getText().trim().isEmpty()) {
            mostrarError("El horario de cierre es obligatorio");
            return false;
        }
        
        if (tipoComercioCombo.getValue() == null) {
            mostrarError("Debes seleccionar un tipo de comercio");
            return false;
        }
        
        // Validar formato de horas
        if (!horaAperturaField.getText().matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            mostrarError("Formato de hora de apertura inválido. Usa HH:mm");
            return false;
        }
        
        if (!horaCierreField.getText().matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            mostrarError("Formato de hora de cierre inválido. Usa HH:mm");
            return false;
        }
        
        // Limpiar mensajes de error si todo está correcto
        errorLabel.setText("");
        return true;
    }
    
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    }
    
    @FXML
    private void cancelar() {
        try {
            // Obtener el panel de contenido principal del dashboard
            Pane mainContentPane = (Pane) contentPane.getScene().lookup("#contenidoPane");
            if (mainContentPane != null) {
                // Cargar la vista de locales
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/locales.fxml"));
                Parent localesView = loader.load();
                
                // Reemplazar el contenido actual con la vista de locales
                mainContentPane.getChildren().setAll(localesView);
            } else {
                // Si no se encuentra el panel de contenido, cerrar la ventana como último recurso
                ((Stage) contentPane.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Si hay un error, cerrar la ventana
            ((Stage) contentPane.getScene().getWindow()).close();
        }
    }
}
