package asedi.controllers.gerencia;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import asedi.model.Producto;
import asedi.services.ProductoService;
import asedi.utils.AlertUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class ProductoController implements Initializable {

    // Componentes de la interfaz
    @FXML
    private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, Boolean> colDisponible;
    @FXML private TableColumn<Producto, Void> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Label lblPagina;
    @FXML private StackPane loadingPane;
    
    // Inyectar dependencias
    private final ProductoService productoService;
    
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final int ITEMS_POR_PAGINA = 20;
    private int paginaActual = 0;
    private int totalProductos = 0;
    
    // Cache para imágenes
    private final Map<String, Image> imageCache = new WeakHashMap<>();
    private Long menuId; // ID del menú actual, si es que se está viendo un menú específico
    
    public ProductoController() {
        this.productoService = new ProductoService();
    }
    
    /**
     * Establece el ID del menú del cual se mostrarán los productos.
     * Si es null, se mostrarán todos los productos.
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Configurar las columnas de la tabla
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
            colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
            colDisponible.setCellValueFactory(new PropertyValueFactory<>("disponible"));
            
            // Configurar la columna de acciones
            colAcciones.setCellFactory(_ -> new TableCell<>() {
                private final Button btnEditar = new Button("Editar");
                private final Button btnEliminar = new Button("Eliminar");
                private final HBox pane = new HBox(5, btnEditar, btnEliminar);
                
                {
                    btnEditar.setOnAction(_ -> {
                        Producto producto = getTableView().getItems().get(getIndex());
                        editarProducto(producto);
                    });
                    
                    btnEliminar.setOnAction(_ -> {
                        Producto producto = getTableView().getItems().get(getIndex());
                        confirmarEliminacion(producto);
                    });
                    
                    // Estilos de los botones
                    btnEditar.getStyleClass().add("btn-primary");
                    btnEliminar.getStyleClass().add("btn-danger");
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
                    }
                }
            });
            
            // Configurar el listener de búsqueda
            txtBuscar.textProperty().addListener((_, _, _) -> actualizarVistaProductos());
            
            // Cargar datos iniciales
            cargarProductos();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "Error al inicializar la vista de productos: " + e.getMessage());
        }
    }

    @FXML
    private void buscarProductos() {
        paginaActual = 0; // Resetear a la primera página al realizar una nueva búsqueda
        actualizarVistaProductos();
    }
    
    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        paginaActual = 0;
        actualizarVistaProductos();
    }
    

    
    /**
     * Limpia los recursos cuando el controlador ya no se usa
     */
    public void limpiarRecursos() {
        try {
            imageCache.clear();
            productos.clear();
        } catch (Exception e) {
            System.err.println("Error al limpiar recursos: " + e.getMessage());
        }
    }

    private Predicate<Producto> crearPredicadoBusqueda(String busqueda, String categoria, String disponibilidad) {
        return producto -> {
            if (producto == null) return false;
            
            // Filtrar por búsqueda
            boolean coincideBusqueda = busqueda == null || busqueda.isEmpty() ||
                (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(busqueda.toLowerCase())) ||
                (producto.getDescripcion() != null && 
                 producto.getDescripcion().toLowerCase().contains(busqueda.toLowerCase()));

            // Filtrar por categoría
            boolean coincideCategoria = categoria == null || 
                categoria.isEmpty() ||
                categoria.equals("Todas las categorías") ||
                (producto.getCategoria() != null && 
                 producto.getCategoria().equalsIgnoreCase(categoria));

            // Filtrar por disponibilidad
            boolean coincideDisponibilidad = true;
            if (disponibilidad != null && !disponibilidad.isEmpty()) {
                if (disponibilidad.equals("Disponibles")) {
                    coincideDisponibilidad = producto.getDisponible() != null && producto.getDisponible();
                } else if (disponibilidad.equals("No disponibles")) {
                    coincideDisponibilidad = producto.getDisponible() == null || !producto.getDisponible();
                }
            }

            return coincideBusqueda && coincideCategoria && coincideDisponibilidad;
        };
    }

    @FXML
    public void nuevoProducto() {
        handleAgregarProducto();
    }
    
    @FXML
    private void handleAgregarProducto() {
        try {
            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/gerencia/productos/formulario_producto.fxml")
            );
            
            // Cargar el panel del diálogo
            DialogPane dialogPane = loader.load();
            
            // Obtener el controlador del formulario
            ProductoFormController formController = loader.getController();
            formController.setProductoService(productoService);
            formController.setProducto(new Producto());
            
            // Configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nuevo Producto");
            
            // Configurar el botón OK para que solo se active cuando el formulario sea válido
            ButtonBase okButton = (ButtonBase) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.disableProperty().bind(formController.getFormularioValidoProperty().not());
            
            // Configurar el resultado del diálogo
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return buttonType;
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar el resultado
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Producto producto = formController.getProducto();
                    // Ejecutar en un hilo separado para no bloquear la interfaz
                    Task<Boolean> tareaGuardar = new Task<>() {
                        @Override
                        protected Boolean call() throws Exception {
                            if (producto.getId() == null) {
                                // Crear nuevo producto
                                Producto creado = productoService.crear(producto);
                                return creado != null && creado.getId() != null;
                            } else {
                                // Actualizar producto existente
                                return productoService.actualizar(producto);
                            }
                        }
                    };
                    
                    tareaGuardar.setOnSucceeded(_ -> {
                        cargarProductos(); // Recargar la lista de productos
                        AlertUtils.mostrarInformacion("Éxito", "Producto guardado correctamente");
                    });
                    
                    tareaGuardar.setOnFailed(_ -> {
                        Throwable ex = tareaGuardar.getException();
                        AlertUtils.mostrarError("Error", "No se pudo guardar el producto: " + 
                            (ex != null ? ex.getMessage() : "Error desconocido"));
                    });
                    
                    new Thread(tareaGuardar).start();
                }
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "No se pudo abrir el formulario de producto: " + e.getMessage());
        }
    }

    private void confirmarEliminacion(Producto producto) {
        if (producto == null) return;
        
        // Mostrar diálogo de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro de que desea eliminar el producto '" + 
            producto.getNombre() + "'?\nEsta acción no se puede deshacer.");
        
        // Mostrar diálogo y esperar respuesta
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                mostrarCargando(true);
                
                Task<Boolean> tareaEliminar = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return productoService.eliminar(producto.getId());
                    }
                };
                
tareaEliminar.setOnSucceeded(_ -> {
                    mostrarCargando(false);
                    if (tareaEliminar.getValue()) {
                        // Eliminación exitosa, actualizar la vista
                        Platform.runLater(() -> {
                            productos.remove(producto);
                            actualizarVistaProductos();
                            AlertUtils.mostrarInformacion("Éxito", "Producto eliminado correctamente");
                        });
                    } else {
                        Platform.runLater(() -> 
                            AlertUtils.mostrarError("Error", "No se pudo eliminar el producto")
                        );
                    }
                });
                
tareaEliminar.setOnFailed(_ -> {
                    mostrarCargando(false);
                    Platform.runLater(() -> 
                        AlertUtils.mostrarError("Error", "Error al intentar eliminar el producto: " + 
                            (tareaEliminar.getException() != null ? 
                                tareaEliminar.getException().getMessage() : "Error desconocido"))
                    );
                });
                
                new Thread(tareaEliminar).start();
            }
        });
    }

    private void editarProducto(Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gerencia/productos/formulario_producto.fxml"));
            Parent root = loader.load();
            
            ProductoFormController formController = loader.getController();
            formController.setProductoService(productoService);
            formController.setProducto(producto);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Editar Producto: " + producto.getNombre());
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Guardar");
            
            BooleanProperty formularioValido = formController.getFormularioValidoProperty();
            if (formularioValido != null) {
                okButton.disableProperty().bind(formularioValido.not());
            }
            
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Task<Boolean> tareaActualizar = new Task<>() {
                        @Override
                        protected Boolean call() throws Exception {
                            formController.guardar();
                            return true;
                        }
                    };
                    
tareaActualizar.setOnSucceeded(_ -> {
                        cargarProductos(); // Recargar la lista de productos
                    });
                    
tareaActualizar.setOnFailed(_ -> {
                        Throwable ex = tareaActualizar.getException();
                        AlertUtils.mostrarError("Error", "No se pudo actualizar el producto: " + 
                            (ex != null ? ex.getMessage() : "Error desconocido"));
                    });
                    
                    new Thread(tareaActualizar).start();
                }
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", 
                "No se pudo abrir el formulario de edición: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        mostrarCargando(true);
        
        Task<List<Producto>> tarea = new Task<>() {
            @Override
            protected List<Producto> call() throws Exception {
                if (menuId != null) {
                    // Cargar productos del menú específico
                    return productoService.obtenerTodos(menuId);
                } else {
                    // Cargar todos los productos
                    return productoService.obtenerTodos();
                }
            }
            
            @Override
            protected void succeeded() {
                try {
                    List<Producto> resultado = get();
                    if (resultado != null) {
                        productos.setAll(resultado);
                        totalProductos = productos.size();
                        actualizarVistaProductos();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.mostrarError("Error", "Error al cargar los productos: " + e.getMessage());
                } finally {
                    mostrarCargando(false);
                }
            }
            
            @Override
            protected void failed() {
                Throwable e = getException();
                e.printStackTrace();
                AlertUtils.mostrarError("Error", "Error al cargar los productos: " + e.getMessage());
                mostrarCargando(false);
            }
        };
        
        new Thread(tarea).start();
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    private void mostrarCargando(boolean mostrar) {
        if (loadingPane != null) {
            loadingPane.setVisible(mostrar);
            loadingPane.setManaged(mostrar);
        }
    }

    /**
     * Actualiza los controles de paginación
     */
    private void actualizarControlesPaginacion() {
        if (lblPagina != null) {
            int totalPaginas = (int) Math.ceil((double) productos.size() / ITEMS_POR_PAGINA);
            int paginaMostrada = totalProductos > 0 ? paginaActual + 1 : 0;
            lblPagina.setText(String.format("Página %d de %d", paginaMostrada, Math.max(1, totalPaginas)));
        }
    }

    /**
     * Navega a la página anterior
     */
    @FXML
    private void paginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            actualizarVistaProductos();
        }
    }

    /**
     * Navega a la página siguiente
     */
    @FXML
    private void paginaSiguiente() {
        int totalPaginas = (int) Math.ceil((double) productos.size() / ITEMS_POR_PAGINA);
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            actualizarVistaProductos();
        }
    }

    /**
     * Actualiza la vista de productos con los datos actuales
     */
    private void actualizarVistaProductos() {
        try {
            // Mostrar indicador de carga
            mostrarCargando(true);
            
            // Obtener los parámetros de búsqueda
            String busqueda = txtBuscar.getText();
            
            // Crear predicado de búsqueda
            Predicate<Producto> predicado = crearPredicadoBusqueda(
                busqueda != null ? busqueda.trim().toLowerCase() : "", 
                "", 
                ""
            );
            
            // Filtrar y paginar productos
            List<Producto> productosFiltrados = productos.stream()
                .filter(predicado)
                .skip(paginaActual * ITEMS_POR_PAGINA)
                .limit(ITEMS_POR_PAGINA)
                .collect(Collectors.toList());
            
            // Actualizar la tabla
            Platform.runLater(() -> {
                try {
                    tblProductos.getItems().setAll(productosFiltrados);
                    actualizarControlesPaginacion();
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.mostrarError("Error", "Error al actualizar la tabla: " + e.getMessage());
                } finally {
                    mostrarCargando(false);
                }
            });
            
            // Forzar recolección de basura para liberar memoria
            System.gc();
            
        } catch (Exception e) {
            String mensaje = "Error al actualizar la vista de productos";
            if (e.getMessage() != null) {
                mensaje += ": " + e.getMessage();
            }
            AlertUtils.mostrarError("Error", mensaje);
        }
    }
}
