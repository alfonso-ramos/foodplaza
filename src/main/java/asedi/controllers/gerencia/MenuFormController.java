package asedi.controllers.gerencia;

import asedi.model.Menu;
import asedi.services.MenuService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class MenuFormController {
    @FXML 
    private TextField txtNombre;
    @FXML 
    private TextArea txtDescripcion;
    private Long idLocalGerente;
    @FXML 
    private Label lblErrorNombre;
    
    // Flag to track if the FXML has been loaded
    private boolean initialized = false;
    @FXML 
    private StackPane loadingPane;
    
    // Mapa para almacenar los mensajes de error
    private final Map<String, String> errores = new HashMap<>();
    
    // Estilos CSS para los campos
    private static final String ERROR_STYLE = "-fx-border-color: #d9534f; -fx-border-width: 2px;";
    private static final String NORMAL_STYLE = "";
    
    private Menu menu;
    private MenuController menuController;
    private final MenuService menuService = new MenuService();
    
    @FXML
    public void initialize() {
        // Initialize the error label with empty text
        if (lblErrorNombre != null) {
            lblErrorNombre.setText("");
        }
        configurarValidaciones();
        initialized = true;
    }
    
    private void configurarValidaciones() {
        // Validación del campo nombre
        txtNombre.textProperty().addListener((_, _, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                agregarError("nombre", "El nombre es obligatorio");
                txtNombre.setStyle(ERROR_STYLE);
            } else if (newValue.length() > 100) {
                agregarError("nombre", "El nombre no puede exceder los 100 caracteres");
                txtNombre.setStyle(ERROR_STYLE);
            } else {
                removerError("nombre");
                txtNombre.setStyle(NORMAL_STYLE);
            }
            actualizarMensajesError();
        });
        
        // Validación del campo descripción
        txtDescripcion.textProperty().addListener((_, _, newValue) -> {
            if (newValue != null && newValue.length() > 500) {
                agregarError("descripcion", "La descripción no puede exceder los 500 caracteres");
                txtDescripcion.setStyle(ERROR_STYLE);
            } else {
                removerError("descripcion");
                txtDescripcion.setStyle(NORMAL_STYLE);
            }
            actualizarMensajesError();
        });
    }
    
    private void agregarError(String campo, String mensaje) {
        errores.put(campo, mensaje);
    }
    
    private void removerError(String campo) {
        errores.remove(campo);
    }
    
    private void actualizarMensajesError() {
        if (lblErrorNombre != null) {
            Platform.runLater(() -> {
                lblErrorNombre.setText(errores.getOrDefault("nombre", ""));
            });
        }
    }
    
    private boolean validarFormulario() {
        // Validar campos obligatorios
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            agregarError("nombre", "El nombre es obligatorio");
            txtNombre.setStyle(ERROR_STYLE);
        }
        
        // Validar longitudes máximas
        if (txtNombre.getText() != null && txtNombre.getText().length() > 100) {
            agregarError("nombre", "El nombre no puede exceder los 100 caracteres");
            txtNombre.setStyle(ERROR_STYLE);
        }
        
        if (txtDescripcion.getText() != null && txtDescripcion.getText().length() > 500) {
            agregarError("descripcion", "La descripción no puede exceder los 500 caracteres");
            txtDescripcion.setStyle(ERROR_STYLE);
        }
        
        actualizarMensajesError();
        return errores.isEmpty();
    }
    
    public void setMenu(Menu menu) {
        this.menu = menu != null ? menu : new Menu();
        cargarDatosMenu();
    }
    
    private void cargarDatosMenu() {
        if (menu != null) {
            txtNombre.setText(menu.getNombre() != null ? menu.getNombre() : "");
            txtDescripcion.setText(menu.getDescripcion() != null ? menu.getDescripcion() : "");
        }
    }
    
    @FXML
    private void guardar() {
        // Validar el formulario antes de continuar
        if (!validarFormulario()) {
            mostrarError("Error de validación", "Por favor, corrija los errores en el formulario");
            return;
        }
        
        try {
            if (menu == null) {
                menu = new Menu();
            }
            
            // Obtener los valores del formulario
            menu.setNombre(txtNombre.getText().trim());
            menu.setDescripcion(txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : null);
            menu.setIdLocal(idLocalGerente);
            
            // Mostrar indicador de carga
            mostrarCargando(true);
            
            // Ejecutar la operación en un hilo separado
            Task<Menu> guardarTask = new Task<>() {
                @Override
                protected Menu call() throws Exception {
                    if (menu.getId() == null) {
                        // Crear nuevo menú (devuelve Menu)
                        Menu nuevoMenu = menuService.crear(menu);
                        if (nuevoMenu != null) {
                            return nuevoMenu;
                        }
                        throw new Exception("No se pudo crear el menú");
                    } else {
                        // Actualizar menú existente (devuelve boolean)
                        boolean actualizado = menuService.actualizar(menu);
                        if (actualizado) {
                            return menu; // Devolvemos el menú actualizado
                        }
                        throw new Exception("No se pudo actualizar el menú");
                    }
                }
                
                @Override
                protected void succeeded() {
                    Menu menuGuardado = getValue();
                    Platform.runLater(() -> {
                        mostrarCargando(false);
                        mostrarMensaje(AlertType.INFORMATION, "Éxito", 
                            menu.getId() == null ? "Menú creado correctamente" : "Menú actualizado correctamente");
                        
                        // Notificar al controlador principal para que actualice la vista
                        if (menuController != null) {
                            menuController.actualizarMenuEnTabla(menuGuardado);
                        }
                        
                        // Cerrar el formulario
                        Stage stage = (Stage) txtNombre.getScene().getWindow();
                        stage.close();
                    });
                }
                
                @Override
                protected void failed() {
                    Throwable e = getException();
                    Platform.runLater(() -> {
                        mostrarCargando(false);
                        mostrarError("Error al guardar el menú", 
                            e != null ? e.getMessage() : "Error desconocido");
                        if (e != null) {
                            e.printStackTrace();
                        }
                    });
                }
            };
            
            // Ejecutar la tarea en un hilo separado
            new Thread(guardarTask).start();
            
        } catch (Exception e) {
            mostrarCargando(false);
            mostrarError("Error al guardar el menú", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void cancelar() {
        cerrarVentana();
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarError(String titulo, String mensaje) {
        mostrarMensaje(AlertType.ERROR, titulo, mensaje);
    }
    
    private void mostrarMensaje(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarCargando(boolean mostrar) {
        if (loadingPane != null) {
            loadingPane.setVisible(mostrar);
            loadingPane.setManaged(mostrar);
        }
    }
    
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }
    
    /**
     * Establece el modo del formulario (nuevo o edición)
     * @param esNuevo true si es un nuevo menú, false si es una edición
     */
    public void setModoNuevo(boolean esNuevo) {
        if (esNuevo) {
            // Si es un nuevo menú, creamos una nueva instancia
            this.menu = new Menu();
            // Limpiamos los campos
            txtNombre.clear();
            txtDescripcion.clear();
        }
    }
    
    /**
     * Establece el ID del local para el menú
     * @param idLocal ID del local del gerente
     */
    public void setIdLocal(Long idLocal) {
        if (menu != null) {
            // Establecemos directamente el ID del local en el menú
            menu.setIdLocal(idLocal);
            this.idLocalGerente = idLocal;
        }
    }
}
