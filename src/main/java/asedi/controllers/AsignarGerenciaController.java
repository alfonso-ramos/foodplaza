package asedi.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import asedi.model.Local;
import asedi.model.Plaza;
import asedi.model.Usuario;
import asedi.services.LocalService;
import asedi.services.PlazaService;
import asedi.services.UsuarioService;
import asedi.utils.ImageUtils;
import java.util.List;

public class AsignarGerenciaController implements Initializable {
    @FXML private TextField emailField;
    @FXML private Button buscarBtn;
    @FXML private HBox userInfoBox;
    @FXML private ImageView userImage;
    @FXML private Text userName;
    @FXML private Text userEmail;
    @FXML private Text userPhone;
    @FXML private ComboBox<Plaza> plazaCombo;
    @FXML private ComboBox<Local> localCombo;
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button asignarBtn;
    @FXML private Button cancelarBtn;
    
    private Usuario usuarioSeleccionado;
    private final PlazaService plazaService = new PlazaService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final LocalService localService = new LocalService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración inicial del controlador
        userInfoBox.setVisible(false);
        plazaCombo.setDisable(true);
        localCombo.setDisable(true);
        asignarBtn.setDisable(true);
        loadingOverlay.setVisible(false);
        
        // Configurar listeners
        configurarEventos();
        
        // Cargar plazas en segundo plano
        cargarPlazas();
    }
    
    @FXML
    private void onBuscarUsuario(javafx.event.ActionEvent event) {
        buscarUsuario();
    }
    
    @FXML
    private void onPlazaSeleccionada(javafx.event.ActionEvent event) {
        Plaza plazaSeleccionada = plazaCombo.getSelectionModel().getSelectedItem();
        if (plazaSeleccionada != null) {
            cargarLocalesPorPlaza(plazaSeleccionada.getId());
            localCombo.setDisable(false);
        } else {
            localCombo.setDisable(true);
            localCombo.getItems().clear();
        }
        actualizarEstadoBotonAsignar();
    }
    
    @FXML
    private void onLocalSeleccionado(javafx.event.ActionEvent event) {
        actualizarEstadoBotonAsignar();
    }
    
    @FXML
    private void onAsignarGerencia(javafx.event.ActionEvent event) {
        asignarGerencia();
    }
    
    @FXML
    private void onCancelar(javafx.event.ActionEvent event) {
        limpiarFormulario();
    }
    
    private void cargarPlazas() {
        mostrarCarga(true);
        new Thread(() -> {
            try {
                List<Plaza> plazas = plazaService.obtenerTodas();
                Platform.runLater(() -> {
                    plazaCombo.setItems(FXCollections.observableArrayList(plazas));
                    mostrarCarga(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarError("Error al cargar las plazas: " + e.getMessage());
                    mostrarCarga(false);
                });
            }
        }).start();
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
        cancelarBtn.setOnAction(_ -> limpiarFormulario());
    }
    
    private void buscarUsuario() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            mostrarError("Por favor ingrese un correo electrónico");
            return;
        }
        
        mostrarCarga(true);
        System.out.println("Buscando usuario con email: " + email); // Debug log
        
        new Thread(() -> {
            try {
                System.out.println("Hilo de búsqueda iniciado"); // Debug log
                usuarioSeleccionado = usuarioService.buscarPorEmail(email);
                System.out.println("Respuesta del servicio: " + (usuarioSeleccionado != null ? "Usuario encontrado" : "Usuario no encontrado")); // Debug log
                
                Platform.runLater(() -> {
                    try {
                        if (usuarioSeleccionado != null) {
                            System.out.println("Mostrando información del usuario: " + usuarioSeleccionado.getEmail()); // Debug log
                            mostrarInfoUsuario(usuarioSeleccionado);
                            plazaCombo.setDisable(false);
                            // Limpiar selecciones previas
                            plazaCombo.getSelectionModel().clearSelection();
                            localCombo.getItems().clear();
                            localCombo.setDisable(true);
                            asignarBtn.setDisable(true);
                        } else {
                            System.out.println("No se encontró el usuario"); // Debug log
                            mostrarError("No se encontró ningún usuario con el correo: " + email);
                            userInfoBox.setVisible(false);
                            plazaCombo.setDisable(true);
                            localCombo.setDisable(true);
                        }
                    } catch (Exception e) {
                        System.err.println("Error en Platform.runLater: " + e.getMessage());
                        e.printStackTrace();
                        mostrarError("Error al procesar la respuesta del servidor");
                    } finally {
                        mostrarCarga(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error en el hilo de búsqueda: " + e.getMessage());
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    String errorMsg = "Error al buscar el usuario: ";
                    if (e.getMessage() != null && e.getMessage().contains("ConnectException")) {
                        errorMsg += "No se pudo conectar al servidor. Verifique su conexión a Internet.";
                    } else if (e.getMessage() != null && e.getMessage().contains("UnknownHostException")) {
                        errorMsg += "No se pudo resolver el host. Verifique su conexión a Internet o la URL del servidor.";
                    } else {
                        errorMsg += e.getMessage();
                    }
                    mostrarError(errorMsg);
                    mostrarCarga(false);
                });
            }
        }, "BuscarUsuarioThread").start();
    }
    
    private void mostrarInfoUsuario(Usuario usuario) {
        // Usar getNombreCompleto() en lugar de concatenar nombre y apellido
        userName.setText(usuario.getNombreCompleto());
        userEmail.setText(usuario.getEmail());
        userPhone.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado");
        
        // Cargar imagen de perfil si está disponible
        if (usuario.getImagenUrl() != null && !usuario.getImagenUrl().isEmpty()) {
            try {
                Image img = new Image(usuario.getImagenUrl(), 80, 80, true, true);
                userImage.setImage(img);
            } catch (Exception e) {
                // Usar imagen por defecto si hay error al cargar la imagen
                userImage.setImage(ImageUtils.getDefaultUserImage(usuario.getNombreCompleto()));
            }
        } else {
            userImage.setImage(ImageUtils.getDefaultUserImage(usuario.getNombreCompleto()));
        }
        
        userInfoBox.setVisible(true);
    }
    
    private void cargarLocalesPorPlaza(Long idPlaza) {
        if (idPlaza == null) {
            localCombo.getItems().clear();
            localCombo.setDisable(true);
            return;
        }
        
        mostrarCarga(true);
        new Thread(() -> {
            try {
                List<Local> locales = localService.obtenerLocalesPorPlaza(idPlaza);
                Platform.runLater(() -> {
                    localCombo.setItems(FXCollections.observableArrayList(locales));
                    localCombo.getSelectionModel().clearSelection();
                    localCombo.setDisable(locales.isEmpty());
                    if (locales.isEmpty()) {
                        mostrarError("No se encontraron locales para la plaza seleccionada");
                    }
                    mostrarCarga(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarError("Error al cargar los locales: " + e.getMessage());
                    localCombo.setDisable(true);
                    mostrarCarga(false);
                });
            }
        }).start();
    }
    
    private void actualizarEstadoBotonAsignar() {
        boolean habilitar = usuarioSeleccionado != null && 
                          plazaCombo.getSelectionModel().getSelectedItem() != null &&
                          localCombo.getSelectionModel().getSelectedItem() != null;
        asignarBtn.setDisable(!habilitar);
    }
    
    private void asignarGerencia() {
        if (usuarioSeleccionado == null) {
            mostrarError("No se ha seleccionado ningún usuario");
            return;
        }
        
        Local localSeleccionado = localCombo.getSelectionModel().getSelectedItem();
        if (localSeleccionado == null) {
            mostrarError("No se ha seleccionado ningún local");
            return;
        }
        
        // Confirmar con el usuario
        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar asignación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(String.format(
            "¿Está seguro de asignar a %s como gerente del local %s?",
            usuarioSeleccionado.getNombreCompleto(),
            localSeleccionado.getNombre()
        ));
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Proceder con la asignación
                mostrarCarga(true);
                
                // Crear un nuevo hilo para realizar las operaciones en segundo plano
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Primero asignamos el rol de gerente
                            boolean exitoRol = usuarioService.asignarRolGerente(usuarioSeleccionado.getId());
                            
                            if (exitoRol) {
                                // Actualizar el local con el ID del gerente
                                Local local = localCombo.getSelectionModel().getSelectedItem();
                                local.setIdGerente(usuarioSeleccionado.getId());
                                
                                // Actualizar el local en el servidor
                                boolean exitoActualizacion = localService.actualizarLocal(local, null);
                                
                                Platform.runLater(() -> {
                                    mostrarCarga(false);
                                    if (exitoActualizacion) {
                                        mostrarExito("Gerente asignado exitosamente al local");
                                        limpiarFormulario();
                                    } else {
                                        mostrarError("Se asignó el rol de gerente, pero hubo un error al actualizar el local. Por favor, intente nuevamente.");
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    mostrarCarga(false);
                                    mostrarError("No se pudo asignar el rol de gerente. Intente nuevamente.");
                                });
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                mostrarCarga(false);
                                mostrarError("Error al asignar la gerencia: " + e.getMessage());
                            });
                            e.printStackTrace();
                        }
                    }
                });
                
                // Iniciar el hilo
                t.start();
            }
        });
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
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    private void mostrarExito(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    private void mostrarCarga(boolean mostrar) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(mostrar);
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
        
        // Deshabilitar controles durante la carga
        emailField.setDisable(mostrar);
        buscarBtn.setDisable(mostrar);
        plazaCombo.setDisable(mostrar || usuarioSeleccionado == null);
        localCombo.setDisable(mostrar || plazaCombo.getSelectionModel().getSelectedItem() == null);
        asignarBtn.setDisable(mostrar || !(usuarioSeleccionado != null && 
                                         plazaCombo.getSelectionModel().getSelectedItem() != null && 
                                         localCombo.getSelectionModel().getSelectedItem() != null));
    }
}
