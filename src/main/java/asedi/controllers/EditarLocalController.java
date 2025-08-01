package asedi.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import asedi.model.Local;
import asedi.services.LocalService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditarLocalController implements Initializable {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtHorario;
    @FXML private TextField txtTipoComercio;
    @FXML private TextField txtUbicacion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    private Local local;
    private LocalesController localesController;
    private LocalService localService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        localService = new LocalService();
        
        // Configurar ComboBox de estados
        cmbEstado.getItems().addAll("Activo", "Inactivo", "En mantenimiento");
        
        // Configurar eventos de los botones
        btnGuardar.setOnAction(event -> guardarCambios());
        btnCancelar.setOnAction(event -> cerrarVentana());
    }
    
    public void setLocal(Local local) {
        this.local = local;
        cargarDatosLocal();
    }
    
    public void setLocalesController(LocalesController controller) {
        this.localesController = controller;
    }
    
    private void cargarDatosLocal() {
        if (local != null) {
            txtNombre.setText(local.getNombre());
            txtDescripcion.setText(local.getDescripcion());
            txtHorario.setText(local.getHorario());
            txtTipoComercio.setText(local.getTipoComercio());
            txtUbicacion.setText(local.getUbicacion());
            cmbEstado.setValue(local.getEstado());
        }
    }
    
    private void guardarCambios() {
        // Validar campos obligatorios
        if (txtNombre.getText().isEmpty() || txtTipoComercio.getText().isEmpty() || 
            txtUbicacion.getText().isEmpty() || cmbEstado.getValue() == null) {
            
            mostrarError("Por favor complete todos los campos obligatorios.");
            return;
        }
        
        try {
            // Actualizar el objeto local con los nuevos valores
            local.setNombre(txtNombre.getText().trim());
            local.setDescripcion(txtDescripcion.getText().trim());
            local.setHorario(txtHorario.getText().trim());
            local.setTipoComercio(txtTipoComercio.getText().trim());
            local.setUbicacion(txtUbicacion.getText().trim());
            local.setEstado(cmbEstado.getValue());
            
            // Llamar al servicio para actualizar el local
            // Se pasa una lista vacía de imágenes ya que no se están manejando imágenes en este controlador
            boolean exito = localService.actualizarLocal(local, new java.util.ArrayList<>());
            
            if (exito) {
                mostrarMensaje("Éxito", "El local se ha actualizado correctamente.", AlertType.INFORMATION);
                if (localesController != null && local != null) {
                    // Recargar los locales para la plaza actual
                    localesController.cargarLocales(local.getPlazaId());
                }
                cerrarVentana();
            } else {
                mostrarError("No se pudo actualizar el local. Intente nuevamente.");
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar el local: " + e.getMessage());
            e.printStackTrace();
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
    
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
