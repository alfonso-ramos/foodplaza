package asedi.controllers;

import java.io.File;
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
    
    private File imagenSeleccionada = null;
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
    
    public void setPlazas(java.util.List<Plaza> plazas) {
        plazaCombo.getItems().setAll(plazas);
        
        if (!plazas.isEmpty()) {
            plazaCombo.getSelectionModel().selectFirst();
        }
    }
    
    private void configurarValidaciones() {
        // Validar formato de hora (HH:mm)
        horaAperturaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                horaAperturaField.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1px;");
            } else {
                horaAperturaField.setStyle("");
            }
        });
        
        horaCierreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                horaCierreField.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1px;");
            } else {
                horaCierreField.setStyle("");
            }
        });
    }
    
    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File archivo = fileChooser.showOpenDialog(null);
        
        if (archivo != null) {
            if (archivo.length() > 5 * 1024 * 1024) { // 5MB
                mostrarError("La imagen " + archivo.getName() + " excede el tamaño máximo de 5MB");
                return;
            }
            
            imagenSeleccionada = archivo;
            actualizarVistaPrevia();
        }
    }
    
    private void actualizarVistaPrevia() {
        thumbnailsContainer.getChildren().clear();
        
        if (imagenSeleccionada != null) {
            try {
                ImageView imageView = new ImageView(new Image(imagenSeleccionada.toURI().toString()));
                imageView.setFitWidth(150);
                imageView.setFitHeight(150);
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
                    imagenSeleccionada = null;
                    actualizarVistaPrevia();
                });
                
                thumbnailsContainer.getChildren().add(stackPane);
                
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + e.getMessage());
                imagenSeleccionada = null;
            }
        }
    }
    
    @FXML
    @SuppressWarnings("unused") // Event parameter is required by JavaFX but not used
    private void guardarLocal(javafx.event.ActionEvent event) {
        // Limpiar mensajes de error previos
        limpiarMensajesError();
        
        // Validar campos obligatorios
        if (!validarCampos()) {
            return;
        }
        
        // Deshabilitar el botón de guardar para evitar múltiples clics
        Button guardarBtn = (Button) nombreField.getScene().lookup("#guardarBtn");
        if (guardarBtn != null) {
            guardarBtn.setDisable(true);
            guardarBtn.setText("Guardando...");
        }
        
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
            local.setEstado(estadoCombo.getValue());
            
            System.out.println("\n=== INTENTANDO GUARDAR LOCAL ===");
            System.out.println("Nombre: " + local.getNombre());
            System.out.println("Plaza ID: " + local.getPlazaId());
            // Llamar al servicio para guardar el local
            LocalService localService = new LocalService();
            boolean exito = localService.guardarLocal(local, imagenSeleccionada);
            
            if (exito) {
                System.out.println("Local guardado exitosamente");
                // Cerrar la ventana si se guardó correctamente
                ((Stage) nombreField.getScene().getWindow()).close();
                
                // Mostrar mensaje de éxito (opcional)
                mostrarMensaje("Éxito", "El local se ha guardado correctamente.", javafx.scene.control.Alert.AlertType.INFORMATION);
            } else {
                mostrarError("No se pudo guardar el local. Por favor, verifica los datos e inténtalo de nuevo.");
            }
            
        } catch (Exception e) {
            System.err.println("Error al guardar el local: " + e.getMessage());
            e.printStackTrace();
            
            // Manejar errores específicos de la API
            String mensajeError = "Error al guardar el local: ";
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            
            if (errorMessage.contains("500")) {
                mensajeError += "Error en el servidor. Por favor, inténtalo más tarde o contacta al administrador.";
            } else if (errorMessage.contains("404")) {
                mensajeError += "No se encontró el recurso solicitado.";
            } else if (errorMessage.contains("400") || errorMessage.contains("422")) {
                mensajeError += "Datos inválidos. Verifica que toda la información sea correcta.";
            } else if (errorMessage.contains("tamaño máximo") || errorMessage.contains("tamaño del archivo")) {
                mensajeError = errorMessage; // Usar el mensaje específico de tamaño de archivo
            } else {
                mensajeError += errorMessage;
            }
            
            mostrarError(mensajeError);
            
        } finally {
            // Re-habilitar el botón de guardar
            if (guardarBtn != null) {
                guardarBtn.setDisable(false);
                guardarBtn.setText("Guardar");
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
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");
        errorLabel.setVisible(true);
        
        // Hacer scroll al mensaje de error si es necesario
        errorLabel.requestFocus();
    }
    
    private void mostrarMensaje(String titulo, String mensaje, javafx.scene.control.Alert.AlertType tipo) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void limpiarMensajesError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void cancelar(javafx.event.ActionEvent event) {
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
