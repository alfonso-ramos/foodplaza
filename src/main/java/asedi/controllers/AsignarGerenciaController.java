package asedi.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import asedi.model.Plaza;
import asedi.model.Usuario;
import asedi.services.PlazaService;
import asedi.services.UsuarioService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;

public class AsignarGerenciaController implements Initializable {
    @FXML private TextField emailField;
    @FXML private Button buscarBtn;
    @FXML private HBox userInfoBox;
    @FXML private ImageView userImage;
    @FXML private Text userName;
    @FXML private Text userEmail;
    @FXML private Text userPhone;
    @FXML private ComboBox<Plaza> plazaCombo;
    @FXML private ComboBox<String> localCombo;
    @FXML private Button asignarBtn;
    @FXML private Button cancelarBtn;
    
    private Usuario usuarioSeleccionado;
    private final PlazaService plazaService = new PlazaService();
    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar visibilidad inicial
        userInfoBox.setVisible(false);
        plazaCombo.setDisable(true);
        localCombo.setDisable(true);
        asignarBtn.setDisable(true);
        
        // Cargar plazas
        cargarPlazas();
        
        // Configurar listeners
        configurarEventos();
    }
    
    private void cargarPlazas() {
        try {
            plazaCombo.setItems(FXCollections.observableArrayList(plazaService.obtenerTodas()));
        } catch (Exception e) {
            mostrarError("Error al cargar las plazas: " + e.getMessage());
        }
    }
    
    private void configurarEventos() {
        buscarBtn.setOnAction(_ -> buscarUsuario());
        plazaCombo.getSelectionModel().selectedItemProperty().addListener((_, __, newVal) -> {
            if (newVal != null) {
                cargarLocalesPorPlaza(newVal.getId());
                localCombo.setDisable(false);
            } else {
                localCombo.setDisable(true);
                localCombo.getItems().clear();
            }
            actualizarEstadoBotonAsignar();
        });
        
        localCombo.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> 
            actualizarEstadoBotonAsignar()
        );
        
        asignarBtn.setOnAction(_ -> asignarGerencia());
        cancelarBtn.setOnAction(_ -> cancelar());
    }
    
    private void buscarUsuario() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            mostrarError("Por favor ingrese un correo electrónico");
            return;
        }
        
        try {
            usuarioSeleccionado = usuarioService.buscarPorEmail(email);
            if (usuarioSeleccionado != null) {
                mostrarInfoUsuario(usuarioSeleccionado);
                plazaCombo.setDisable(false);
            } else {
                mostrarError("No se encontró ningún usuario con ese correo electrónico");
                userInfoBox.setVisible(false);
                plazaCombo.setDisable(true);
                localCombo.setDisable(true);
            }
        } catch (Exception e) {
            mostrarError("Error al buscar el usuario: " + e.getMessage());
        }
    }
    
    private void mostrarInfoUsuario(Usuario usuario) {
        userName.setText(usuario.getNombreCompleto());
        userEmail.setText(usuario.getEmail());
        userPhone.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado");
        // Aquí podrías cargar la imagen del usuario si está disponible
        // userImage.setImage(...);
        userInfoBox.setVisible(true);
    }
    
    private void cargarLocalesPorPlaza(Long idPlaza) {
        try {
            // Aquí deberías implementar la lógica para cargar los locales de la plaza seleccionada
            // Por ahora, lo dejamos con datos de ejemplo
            localCombo.getItems().setAll("Local 1", "Local 2", "Local 3");
            localCombo.getSelectionModel().clearSelection();
        } catch (Exception e) {
            mostrarError("Error al cargar los locales: " + e.getMessage());
        }
    }
    
    private void actualizarEstadoBotonAsignar() {
        asignarBtn.setDisable(
            usuarioSeleccionado == null || 
            plazaCombo.getSelectionModel().getSelectedItem() == null ||
            localCombo.getSelectionModel().getSelectedItem() == null
        );
    }
    
    private void asignarGerencia() {
        try {
            // Aquí implementarías la lógica para asignar el rol de gerente
            // y asociar el local al usuario
            // Get the selected plaza and local (commented out as they're not used yet)
            // Plaza plaza = plazaCombo.getSelectionModel().getSelectedItem();
            // String localSeleccionado = localCombo.getSelectionModel().getSelectedItem();
            
            // Llamar al servicio para actualizar el usuario
            // usuarioService.asignarRolGerente(usuarioSeleccionado.getId(), localSeleccionado);
            
            mostrarExito("Gerencia asignada exitosamente");
            limpiarFormulario();
        } catch (Exception e) {
            mostrarError("Error al asignar la gerencia: " + e.getMessage());
        }
    }
    
    private void cancelar() {
        limpiarFormulario();
    }
    
    private void limpiarFormulario() {
        emailField.clear();
        userInfoBox.setVisible(false);
        plazaCombo.getSelectionModel().clearSelection();
        localCombo.getItems().clear();
        plazaCombo.setDisable(true);
        localCombo.setDisable(true);
        asignarBtn.setDisable(true);
        usuarioSeleccionado = null;
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
