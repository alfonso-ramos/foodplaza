package asedi.controllers.gerencia;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import asedi.model.Menu;
import asedi.model.Usuario;
import asedi.services.MenuService;
import asedi.services.AuthService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.stream.Collectors;

public class MenuController implements Initializable {
    @FXML private TableView<Menu> tablaMenus;
    @FXML private TableColumn<Menu, String> colNombre;
    @FXML private TableColumn<Menu, String> colDescripcion;
    @FXML private TableColumn<Menu, Void> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Label lblNoMenus;
    @FXML private StackPane loadingPane;
    
    private final MenuService menuService = new MenuService();
    private final ObservableList<Menu> menus = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AuthService authService = AuthService.getInstance();
    
    private Long idLocalGerente;
    private List<Menu> menusFiltrados = new ArrayList<>();
    
    // Constants for UI strings
    // Constants for pagination
    //private static final int TAMANIO_PAGINA = 10;  // Unused constant
    private int paginaActual = 0;
    private static final String MSG_ELIMINAR_CONFIRM = "¿Está seguro de que desea eliminar el menú %s?";
    private static final String MSG_ELIMINAR_EXITO = "Menú eliminado correctamente";
    private static final String MSG_ELIMINAR_ERROR = "No se pudo eliminar el menú";
    private static final String TITULO_ELIMINAR = "Eliminar Menú";
    private static final String TITULO_EDITAR = "Editar Menú";
    private static final String TITULO_NUEVO = "Nuevo Menú";
    
    private void mostrarCargando(boolean mostrar) {
        Platform.runLater(() -> {
            if (mostrar) {
                // Clear existing content
                loadingPane.getChildren().clear();
                
                // Create spinner
                ProgressIndicator spinner = new ProgressIndicator();
                spinner.getStyleClass().add("loading-indicator");
                
                // Create loading text
                Label loadingText = new Label("Cargando menús...");
                loadingText.getStyleClass().add("loading-text");
                
                // Create container
                VBox container = new VBox(10, spinner, loadingText);
                container.setAlignment(Pos.CENTER);
                container.getStyleClass().add("loading-container");
                
                // Add to loading pane
                loadingPane.getChildren().add(container);
                
                // Make sure loading pane is visible
                loadingPane.setVisible(true);
                loadingPane.setManaged(true);
                loadingPane.toFront();
            } else {
                loadingPane.setVisible(false);
                loadingPane.setManaged(false);
            }
        });
    }
    
    private void mostrarError(String titulo, String mensaje) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> mostrarError(titulo, mensaje));
        }
    }
    
    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String mensaje) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(tipo, mensaje, ButtonType.OK);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> mostrarMensaje(tipo, titulo, mensaje));
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarBusqueda();
        cargarMenus();
    }
    
    /**
     * Configura la funcionalidad de búsqueda
     */
    @SuppressWarnings("unused")
    private void configurarBusqueda() {
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarMenus());
    }
    
    /**
     * Filtra los menús según el texto de búsqueda
     */
    private void filtrarMenus() {
        String filtro = txtBuscar.getText().toLowerCase();
        if (filtro.isEmpty()) {
            tablaMenus.setItems(menus);
        } else {
            List<Menu> filtrados = menus.stream()
                .filter(menu -> menu.getNombre().toLowerCase().contains(filtro) ||
                              (menu.getDescripcion() != null && 
                               menu.getDescripcion().toLowerCase().contains(filtro)))
                .collect(Collectors.toList());
            tablaMenus.setItems(FXCollections.observableArrayList(filtrados));
        }
        actualizarVisibilidadTabla();
    }
    
    /**
     * Actualiza la visibilidad de la tabla y el mensaje de "no hay menús"
     */
    private void actualizarVisibilidadTabla() {
        boolean hayMenus = tablaMenus != null && !tablaMenus.getItems().isEmpty();
        if (tablaMenus != null) {
            tablaMenus.setVisible(hayMenus);
        }
        if (lblNoMenus != null) {
            lblNoMenus.setVisible(!hayMenus);
        }
    }
    
    /**
     * Carga los menús del local del gerente actual
     */
    private void cargarMenus() {
        // Mostrar indicador de carga
        mostrarCargando(true);
        
        // Obtener el ID del local del gerente si no está establecido
        if (idLocalGerente == null) {
            idLocalGerente = obtenerIdLocalGerente();
            if (idLocalGerente == null) {
                mostrarError("Error", "No se pudo determinar el local del gerente");
                mostrarCargando(false);
                return;
            }
        }
        
        // Crear tarea para cargar los menús en segundo plano
        Task<List<Menu>> task = new Task<>() {
            @Override
            protected List<Menu> call() throws Exception {
                try {
                    return menuService.obtenerPorLocal(idLocalGerente);
                } catch (Exception e) {
                    throw new Exception("Error al conectar con el servidor: " + e.getMessage());
                }
            }
        };
        
        // Configurar manejadores de eventos para la tarea
        task.setOnSucceeded(e -> {
            try {
                List<Menu> menusLocal = task.getValue();
                if (menusLocal != null && !menusLocal.isEmpty()) {
                    menus.setAll(menusLocal);
                    filtrarMenus();
                    Platform.runLater(() -> {
                        tablaMenus.setVisible(true);
                        if (lblNoMenus != null) {
                            lblNoMenus.setVisible(false);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        tablaMenus.setVisible(false);
                        if (lblNoMenus != null) {
                            lblNoMenus.setVisible(true);
                            lblNoMenus.setText("No se encontraron menús para mostrar");
                        }
                    });
                }
            } catch (Exception ex) {
                mostrarError("Error", "Error al procesar los menús: " + ex.getMessage());
            } finally {
                mostrarCargando(false);
            }
        });
        
        task.setOnFailed(e -> {
            mostrarError("Error", "Error al cargar los menús: " + 
                (task.getException() != null ? task.getException().getMessage() : "Error desconocido"));
            mostrarCargando(false);
        });
        
        // Ejecutar la tarea en el executor
        executor.submit(task);
    }
    
    private void configurarTabla() {
        try {
            // Configurar las columnas de la tabla
            colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
            colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
            
            // Configurar columna de acciones
            configurarColumnaAcciones();
            
            // Obtener el usuario actual desde AuthService
            Usuario usuarioActual = authService.getCurrentUser();
            System.out.println("Usuario actual: " + (usuarioActual != null ? usuarioActual.getEmail() : "null"));
            
            if (usuarioActual != null) {
                // Obtener el local asignado del AuthService
                Usuario.Local local = authService.getLocalAsignado();
                
                // Si no hay local en el servicio, verificar si está en el usuario
                if (local == null && usuarioActual.getLocal() != null) {
                    local = usuarioActual.getLocal();
                    authService.setLocalAsignado(local);
                }
                
                System.out.println("Local del usuario: " + (local != null ? local.getId() : "null"));
                
                if (local != null) {
                    idLocalGerente = local.getId();
                    System.out.println("ID del local del gerente: " + idLocalGerente);
                    
                    if (idLocalGerente != null) {
                        configurarUI();
                        cargarDatos();
                        return;
                    }
                }
            }
            
            // Si llegamos aquí, no se pudo obtener el local del gerente
            String mensajeError = "No se pudo obtener el local asignado. ";
            mensajeError += "Usuario: " + (usuarioActual != null ? "existe" : "no existe") + "; ";
            mensajeError += "Local: " + (usuarioActual != null && usuarioActual.getLocal() != null ? "existe" : "no existe");
            
            System.err.println("Error: " + mensajeError);
            mostrarError("Acceso denegado", "No tiene un local asignado o no tiene permisos para acceder a esta sección.");
            
            // Cerrar la ventana actual de manera segura
            Platform.runLater(() -> {
                Node source = (Node) txtBuscar;
                if (source != null) {
                    Scene scene = source.getScene();
                    if (scene != null) {
                        Window window = scene.getWindow();
                        if (window instanceof Stage) {
                            ((Stage) window).close();
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error en initialize: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "Ocurrió un error al inicializar la ventana de menús.");
            Platform.runLater(() -> {
                Node source = (Node) txtBuscar;
                if (source != null) {
                    Scene scene = source.getScene();
                    if (scene != null) {
                        Window window = scene.getWindow();
                        if (window instanceof Stage) {
                            ((Stage) window).close();
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Maneja el evento del botón Nuevo Menú
     */
    @FXML
    private void mostrarFormularioNuevo() {
        mostrarFormularioMenu(null);
    }
    
    /**
     * Configura la columna de acciones con botones de editar y eliminar
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox botones = new HBox(5, btnEditar, btnEliminar);
            
            {
                // Estilo de los botones
                btnEditar.getStyleClass().add("btn-editar");
                btnEliminar.getStyleClass().add("btn-eliminar");
                
                // Acción del botón editar
                btnEditar.setOnAction(event -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    if (menu != null) {
                        mostrarFormularioMenu(menu);
                    }
                });
                
                // Acción del botón eliminar
                btnEliminar.setOnAction(event -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    if (menu != null) {
                        confirmarEliminarMenu(menu);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(botones);
                }
            }
        });
    }
    
    /**
     * Muestra el formulario para editar o crear un menú
     * @param menu El menú a editar, o null para crear uno nuevo
     */
    private void mostrarFormularioMenu(Menu menu) {
        try {
            // Obtener el ID del local del gerente actual
            if (idLocalGerente == null) {
                idLocalGerente = obtenerIdLocalGerente();
                if (idLocalGerente == null) {
                    mostrarError("Error", "No se pudo determinar el local del gerente");
                    return;
                }
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/views/gerencia/menus/form_menu.fxml"));
            Parent root = loader.load();
            
            MenuFormController controller = loader.getController();
            controller.setMenu(menu);
            controller.setIdLocal(idLocalGerente);
            
            Stage stage = new Stage();
            stage.setTitle(menu == null ? TITULO_NUEVO : TITULO_EDITAR);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Recargar los menús después de cerrar el formulario
            cargarMenus();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el formulario de menú");
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene el ID del local del gerente actual
     * @return ID del local o null si no se pudo determinar
     */
    private Long obtenerIdLocalGerente() {
        try {
            // Aquí deberías implementar la lógica para obtener el ID del local
            // del gerente actualmente autenticado.
            // Por ahora, retornamos un valor de ejemplo (1L).
            return 1L;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Muestra un diálogo de confirmación para eliminar un menú
     * @param menu El menú a eliminar
     */
    private void confirmarEliminarMenu(Menu menu) {
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            String.format(MSG_ELIMINAR_CONFIRM, menu.getNombre()),
            ButtonType.YES, ButtonType.NO
        );
        alert.setTitle(TITULO_ELIMINAR);
        alert.setHeaderText(null);
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                eliminarMenu(menu);
            }
        });
    }
    
    /**
     * Elimina un menú
     * @param menu El menú a eliminar
     */
    private void eliminarMenu(Menu menu) {
        if (menu == null || menu.getId() == null) return;
        
        mostrarCargando(true);
        
        Task<Boolean> tareaEliminar = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return menuService.eliminar(menu.getId());
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    mostrarCargando(false);
                    if (getValue()) {
                        menus.remove(menu);
                        mostrarMensaje(Alert.AlertType.INFORMATION, "Éxito", MSG_ELIMINAR_EXITO);
                    } else {
                        mostrarError("Error", MSG_ELIMINAR_ERROR);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    mostrarCargando(false);
                    mostrarError("Error", getException().getMessage());
                });
            }
        };
        
        new Thread(tareaEliminar).start();
    }
    
    /**
     * Actualiza un menú en la tabla
     * @param menuActualizado El menú con los datos actualizados
     */
    public void actualizarMenuEnTabla(Menu menuActualizado) {
        if (menuActualizado == null) return;
        
        Platform.runLater(() -> {
            // Buscar si el menú ya existe
            boolean encontrado = false;
            for (int i = 0; i < menus.size(); i++) {
                if (menus.get(i).getId().equals(menuActualizado.getId())) {
                    menus.set(i, menuActualizado);
                    encontrado = true;
                    break;
                }
            }
            
            // Si no existe, agregarlo
            if (!encontrado) {
                menus.add(menuActualizado);
            }
            
            // Actualizar la vista
            filtrarMenus(txtBuscar.getText());
        });
    }
    
    private void configurarUI() {
        // Configurar el campo de búsqueda
        txtBuscar.textProperty().addListener((obs, oldValue, newValue) -> filtrarMenus(newValue));
        
        // Configurar la columna de acciones
        configurarColumnaAcciones();
        
        // Configurar la tabla
        tablaMenus.setRowFactory(e -> {
            TableRow<Menu> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Menu menuSeleccionado = row.getItem();
                    editarMenu(menuSeleccionado);
                }
            });
            return row;
        });
    }
    
    private void cargarDatos() {
        if (idLocalGerente == null) {
            mostrarError("Error", "No se pudo identificar el local del gerente.");
            return;
        }

        mostrarCargando(true);
        
        Task<List<Menu>> tareaCarga = new Task<>() {
            @Override
            protected List<Menu> call() throws Exception {
                return menuService.obtenerPorLocal(idLocalGerente);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    mostrarCargando(false);
                    List<Menu> menusCargados = getValue();
                    if (menusCargados != null) {
                        menus.clear();
                        menus.addAll(menusCargados);
                        filtrarMenus(txtBuscar.getText());
                    } else {
                        mostrarError("Error", "No se pudieron cargar los menús");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    mostrarCargando(false);
                    Throwable exception = getException();
                    String mensajeError = exception != null ? exception.getMessage() : "Error desconocido";
                    mostrarError("Error", "No se pudieron cargar los menús: " + mensajeError);
                    
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                });
            }
        };

        executor.submit(tareaCarga);
    }
    
    private void filtrarMenus(String busqueda) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            menusFiltrados = new ArrayList<>(menus);
        } else {
            String busquedaLower = busqueda.toLowerCase();
            menusFiltrados = menus.stream()
                .filter(menu -> 
                    (menu.getNombre() != null && menu.getNombre().toLowerCase().contains(busquedaLower)) ||
                    (menu.getDescripcion() != null && menu.getDescripcion().toLowerCase().contains(busquedaLower)))
                .collect(Collectors.toList());
        }
        actualizarVista();
    }
    
    private void actualizarVista() {
        tablaMenus.getItems().clear();
        
        if (menusFiltrados.isEmpty()) {
            mostrarMensajeSinDatos();
            return;
        }
        
        tablaMenus.getItems().addAll(menusFiltrados);
    }
    
    /**
     * Muestra un mensaje indicando que no hay menús disponibles
     */
    private void mostrarMensajeSinDatos() {
        lblNoMenus.setVisible(true);
        tablaMenus.setPlaceholder(new Label("No hay menús disponibles"));
    }
    
    // Método para cerrar la ventana eliminado ya que se implementó la lógica directamente en los bloques Platform.runLater()

    // ... (rest of the code remains the same)

    @FXML
    private void buscarMenus() {
        filtrarMenus(txtBuscar.getText());
    }

    // ... (rest of the code remains the same)
    
    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        filtrarMenus("");
    }
    
    @FXML
    private void nuevoMenu() {
        mostrarFormularioMenu(null);
    }
    
    @FXML
    private void editarMenu(Menu menu) {
        if (menu != null) {
            mostrarFormularioMenu(menu);
        }
    }
    
    public void actualizarTablaMenus() {
        cargarMenus();
    }
    
    @FXML
    private void paginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }
    
    @FXML
    private void paginaSiguiente() {
        paginaActual++;
        cargarDatos();
    }
    
    public void detener() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
