package asedi.controllers.gerencia;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.ArrayList;

import javax.inject.Inject;

import asedi.model.Menu;
import asedi.model.Producto;
import asedi.model.Usuario;
import asedi.services.AuthService;
import asedi.services.MenuService;
import asedi.services.ProductoService;
import asedi.utils.AlertUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.DialogPane;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductoController implements Initializable {
    
    

    // Componentes de la interfaz
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Boolean> colDisponible;
    @FXML private TableColumn<Producto, Void> colAcciones;
    
    // Campos del formulario
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Menu> cmbMenu;
    @FXML private Label lblPagina;
    @FXML private StackPane loadingPane;
    @FXML private Text txtTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private ImageView imgVistaPrevia;
    @FXML private Label lblNombreArchivo;
    @FXML private TextField txtImagenUrl;
    @FXML private TextField txtImagenPublicId;
    @FXML private Label lblErrorNombre;
    @FXML private Label lblErrorDescripcion;
    @FXML private Label lblErrorPrecio;
    @FXML private Label lblErrorStock;
    @FXML private Label lblErrorImagen;
    @FXML private CheckBox chkDisponible;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private Button btnAgregar;
    
    // Inyectar dependencias
    @Inject
    private ProductoService productoService;
    
    @Inject
    private MenuService menuService;
    
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final int ITEMS_POR_PAGINA = 20;
    private int paginaActual = 0;
    private int totalProductos = 0;
    
    // Cache para imágenes
    private final Map<String, Image> imageCache = new WeakHashMap<>();
    private Long menuId; // ID del menú actual, si es que se está viendo un menú específico
    private File imagenSeleccionada = null;
    
    /**
     * Constructor por defecto.
     * Se recomienda usar la inyección de dependencias en su lugar.
     */
    public ProductoController() {
        // Inicialización por compatibilidad
        this.productoService = new ProductoService();
        this.menuService = new MenuService();
    }
    
    /**
     * Constructor con inyección de dependencias.
     */
    @Inject
    public ProductoController(ProductoService productoService, MenuService menuService) {
        this.productoService = productoService;
        this.menuService = menuService;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialize table columns
            if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
            if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
            if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
            if (colDisponible != null) colDisponible.setCellValueFactory(new PropertyValueFactory<>("disponible"));
            
            // Set up search listener
            if (txtBuscar != null) {
                txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> buscarProductos());
            }
            
            // Configure action buttons
            if (btnAgregar != null) {
                btnAgregar.setOnAction(event -> handleAgregarProducto(event));
            }
            
            
            // Configure menus combobox
            configurarComboMenus();
            
            // Add listener for menu selection changes
            if (cmbMenu != null) {
                cmbMenu.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        this.menuId = newVal.getId();
                        cargarProductos();
                    }
                });
            }
            
            // Load initial data
            cargarProductos();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "Error al inicializar el controlador: " + e.getMessage());
        }
    }
    
    /**
     * Establece el ID del menú del cual se mostrarán los productos.
     * Si es null, se mostrarán todos los productos.
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void handleAgregarProducto(ActionEvent event) {
        try {
            // Cargar el formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gerencia/productos/formulario_producto_simple.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador
            ProductoFormController controller = loader.getController();
            controller.setProductoService(productoService);
            
            // Pasar el ID del menú seleccionado
            Menu menuSeleccionado = cmbMenu.getSelectionModel().getSelectedItem();
            if (menuSeleccionado != null) {
                controller.setMenuId(menuSeleccionado.getId());
            }

            // Crear el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nuevo Producto");
            
            // Configurar el diálogo
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().setMinSize(900, 600);
            dialog.getDialogPane().setPrefSize(900, 700);
            
            // Habilitar el desplazamiento en el diálogo
            dialog.getDialogPane().setStyle("-fx-padding: 0;");
            dialog.getDialogPane().getStyleClass().add("scroll-pane");
            
            // Obtener el botón de guardar del controlador
            Button guardarButton = controller.saveButton;
            
            // Configurar la validación del formulario
            guardarButton.setOnAction(e -> {
                if (!controller.validarFormulario()) {
                    e.consume(); // Prevenir el cierre del diálogo
                } else {
                    // Mostrar indicador de carga
                    mostrarCargando(true);
                    
                    // Ejecutar la operación en segundo plano
                    Task<ProductoFormController.ResultadoGuardado> tarea = new Task<>() {
                        @Override
                        protected ProductoFormController.ResultadoGuardado call() throws Exception {
                            return controller.guardar();
                        }
                    };

                    // Manejar el resultado exitoso
                    tarea.setOnSucceeded(evt -> {
                        mostrarCargando(false);
                        ProductoFormController.ResultadoGuardado resultado = tarea.getValue();
                        if (resultado.isExito()) {
                            // Recargar la lista de productos
                            cargarProductos(); 
                            
                            // Limpiar el formulario
                            controller.limpiarFormulario();
                            
                            // Cerrar el diálogo de producto
                            dialog.close();
                            
                            // Mostrar mensaje de éxito
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Operación Exitosa");
                                alert.setHeaderText("¡Producto guardado exitosamente!");
                                alert.setContentText(resultado.getMensaje());
                                
                                // Estilo personalizado para el diálogo
                                DialogPane dialogPane = alert.getDialogPane();
                                dialogPane.getStylesheets().add(
                                    getClass().getResource("/styles/agregarPlaza.css").toExternalForm()
                                );
                                dialogPane.getStyleClass().add("my-dialog");
                                
                                // Mostrar el diálogo
                                alert.showAndWait();
                            });
                        } else {
                            mostrarError("Error", resultado.getMensaje());
                        }
                    });

                    // Manejar errores
                    tarea.setOnFailed(evt -> {
                        mostrarCargando(false);
                        mostrarError("Error", "Error al guardar el producto: " + 
                            (tarea.getException() != null ? tarea.getException().getMessage() : "Error desconocido"));
                    });

                    // Ejecutar la tarea en un hilo separado
                    new Thread(tarea).start();
                    
                    // Prevenir que el diálogo se cierre hasta que la operación termine
                    e.consume();
                }
            });
            
            // Mostrar el diálogo sin botones adicionales
            dialog.getDialogPane().getButtonTypes().clear(); // Eliminar cualquier botón por defecto
            dialog.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar el formulario de producto: " + e.getMessage());
        }
    }
    
    public void configurarParaNuevoProducto() {
        // Limpiar campos
        txtTitulo.setText("Nuevo Producto");
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();
        chkDisponible.setSelected(true);
        cmbCategoria.getSelectionModel().clearSelection();
        cmbMenu.getSelectionModel().clearSelection();
        
        // Restablecer la imagen
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/placeholder-product.png");
            if (is != null) {
                Image placeholder = new Image(is);
                if (!placeholder.isError()) {
                    imgVistaPrevia.setImage(placeholder);
                } else {
                    imgVistaPrevia.setImage(null);
                }
            } else {
                // Si no se encuentra el recurso, usar un ImageView vacío
                imgVistaPrevia.setImage(null);
            }
        } catch (Exception e) {
            // En caso de cualquier error, limpiar la imagen
            imgVistaPrevia.setImage(null);
        }
        lblNombreArchivo.setText("Ninguna imagen seleccionada");
        txtImagenUrl.clear();
        txtImagenPublicId.clear();
        
        // Limpiar mensajes de error
        limpiarErrores();
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void seleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen del producto");
        
        // Configurar filtros de extensión
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "Archivos de imagen", "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // Mostrar diálogo de selección de archivo
        File archivo = fileChooser.showOpenDialog(imgVistaPrevia.getScene().getWindow());
        
        if (archivo != null) {
            try {
                // Verificar tamaño del archivo (máx 5MB)
                long fileSizeInMB = archivo.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    mostrarError("Error", "El archivo es demasiado grande. Tamaño máximo permitido: 5MB");
                    return;
                }
                
                // Cargar la imagen para previsualización
                Image imagen = new Image(archivo.toURI().toString());
                imgVistaPrevia.setImage(imagen);
                lblNombreArchivo.setText(archivo.getName());
                
                // Guardar referencia al archivo para subirlo más tarde
                imagenSeleccionada = archivo;
                
                // Limpiar campos de URL de imagen existente
                txtImagenUrl.clear();
                txtImagenPublicId.clear();
                
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Error", "No se pudo cargar la imagen seleccionada");
            }
        }
    }
    
    private void subirImagen() {
        if (imagenSeleccionada == null) {
            return; // No hay imagen para subir
        }
        
        try {
            // Aquí iría la lógica para subir la imagen a tu servicio de almacenamiento
            // Por ejemplo, usando Cloudinary, AWS S3, o tu propio servidor
            
            // Ejemplo de cómo podría ser la implementación:
            /*
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "tu_cloud_name",
                "api_key", "tu_api_key",
                "api_secret", "tu_api_secret"
            ));
            
            Map uploadResult = cloudinary.uploader().upload(imagenSeleccionada, 
                ObjectUtils.asMap("folder", "productos"));
                
            // Guardar la URL y el public_id de la imagen
            String imagenUrl = (String) uploadResult.get("url");
            String publicId = (String) uploadResult.get("public_id");
            
            txtImagenUrl.setText(imagenUrl);
            txtImagenPublicId.setText(publicId);
            */
            
            // Por ahora, simulamos una URL de imagen
            txtImagenUrl.setText("https://ejemplo.com/imagenes/" + imagenSeleccionada.getName());
            txtImagenPublicId.setText("productos/" + imagenSeleccionada.getName().replace(".", "_"));
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al subir la imagen: " + e.getMessage());
        }
    }
    
    private void limpiarErrores() {
        if (lblErrorNombre != null) lblErrorNombre.setText("");
        if (lblErrorDescripcion != null) lblErrorDescripcion.setText("");
        if (lblErrorPrecio != null) lblErrorPrecio.setText("");
        if (lblErrorStock != null) lblErrorStock.setText("");
        if (lblErrorImagen != null) lblErrorImagen.setText("");
    }
    
    private boolean validarFormulario() {
        boolean valido = true;
        limpiarErrores();
        
        // Validar nombre
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            if (lblErrorNombre != null) lblErrorNombre.setText("El nombre es requerido");
            valido = false;
        }
        
        // Validar descripción
        if (txtDescripcion.getText() == null || txtDescripcion.getText().trim().isEmpty()) {
            if (lblErrorDescripcion != null) lblErrorDescripcion.setText("La descripción es requerida");
            valido = false;
        }
        
        // Validar precio
        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            if (precio <= 0) {
                if (lblErrorPrecio != null) lblErrorPrecio.setText("El precio debe ser mayor a cero");
                valido = false;
            }
        } catch (NumberFormatException e) {
            if (lblErrorPrecio != null) lblErrorPrecio.setText("Ingrese un precio válido");
            valido = false;
        }
        
        // Validar stock
        try {
            int stock = Integer.parseInt(txtStock.getText());
            if (stock < 0) {
                if (lblErrorStock != null) lblErrorStock.setText("El stock no puede ser negativo");
                valido = false;
            }
        } catch (NumberFormatException e) {
            if (lblErrorStock != null) lblErrorStock.setText("Ingrese una cantidad válida");
            valido = false;
        }
        
        return valido;
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void guardarProducto(ActionEvent event) {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            // Subir la imagen si se seleccionó una nueva
            if (imagenSeleccionada != null) {
                subirImagen();
            }
            
            // Crear el objeto Producto
            Producto producto = new Producto();
            producto.setNombre(txtNombre.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setPrecio(Double.parseDouble(txtPrecio.getText()));
            producto.setStock(Integer.parseInt(txtStock.getText()));
            producto.setDisponible(chkDisponible.isSelected());
            
            // Si hay una categoría seleccionada, asignarla
            if (cmbCategoria.getValue() != null) {
                producto.setCategoria(cmbCategoria.getValue());
            }
            
            // Si hay un menú seleccionado, asignarlo
            if (cmbMenu != null && cmbMenu.getValue() != null) {
                producto.setIdMenu(cmbMenu.getValue().getId());
            }
            
            // Asignar URL de la imagen si existe
            if (txtImagenUrl.getText() != null && !txtImagenUrl.getText().isEmpty()) {
                producto.setImagenUrl(txtImagenUrl.getText());
                producto.setImagenPublicId(txtImagenPublicId.getText());
            }
            
            // Aquí iría la lógica para guardar el producto en la base de datos
            // Por ejemplo: productoService.guardarProducto(producto);
            
            // Mostrar mensaje de éxito
            mostrarMensaje("Éxito", "Producto guardado correctamente");
            
            // Cerrar la ventana
            ((Node) event.getSource()).getScene().getWindow().hide();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo guardar el producto: " + e.getMessage());
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
        if (cmbMenu != null && cmbMenu.getItems() != null && !cmbMenu.getItems().isEmpty()) {
            cmbMenu.getSelectionModel().select(0); // Seleccionar "Todos los menús"
        }
        cargarProductos();
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

    @SuppressWarnings("unused")
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
        handleAgregarProducto((ActionEvent) null);
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void handleAgregarProducto() {
        try {
            // Cargar el FXML simplificado
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/gerencia/productos/formulario_producto_simple.fxml")
            );
            
            // Cargar el panel del diálogo
            DialogPane dialogPane = loader.load();
            
            // Obtener referencias a los campos del formulario
            TextField txtNombre = (TextField) dialogPane.lookup("#txtNombre");
            
            // Configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nuevo Producto");
            
            // Configurar el botón de guardar
            ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
            
            // Validar antes de cerrar
            dialog.setResultConverter(buttonType -> {
                if (buttonType == guardarButtonType) {
                    // Validación simple del formulario
                    if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("El nombre del producto es obligatorio");
                        alert.showAndWait();
                        return null;
                    }
                    return buttonType;
                }
                return buttonType;
            });
            
            // Mostrar el diálogo y procesar el resultado
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == guardarButtonType) {
                // Crear y guardar el nuevo producto
                Producto producto = new Producto();
                producto.setNombre(txtNombre.getText().trim());
                
                // Ejecutar en un hilo separado
                Task<Boolean> tareaGuardar = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        Producto creado = productoService.crear(producto);
                        return creado != null && creado.getId() != null;
                    }
                };
                
                tareaGuardar.setOnSucceeded(evt -> {
                    if (tareaGuardar.getValue()) {
                        Platform.runLater((Runnable)() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Éxito");
                            alert.setHeaderText(null);
                            alert.setContentText("Producto guardado correctamente");
                            alert.showAndWait();
                            cargarProductos();
                        });
                    }
                });
                
                tareaGuardar.setOnFailed(_evt -> {
                    Platform.runLater((Runnable)() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Error al guardar el producto: " + 
                            (tareaGuardar.getException() != null ? 
                                tareaGuardar.getException().getMessage() : "Error desconocido"));
                        alert.showAndWait();
                    });
                });
                
                new Thread(tareaGuardar).start();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo cargar el formulario: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @SuppressWarnings("unused")
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
                
                tareaEliminar.setOnSucceeded(e -> {
                    mostrarCargando(false);
                    if (tareaEliminar.getValue()) {
                        // Eliminación exitosa, actualizar la vista
                        Platform.runLater((Runnable)() -> {
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
                
                tareaEliminar.setOnFailed(e -> {
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

    @SuppressWarnings("unused")
    private void editarProducto(Producto producto) {
        try {
            // Cargar el formulario simplificado
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gerencia/productos/formulario_producto_simple.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador
            ProductoFormController controller = loader.getController();
            controller.setProductoService(productoService);
            controller.setProducto(producto);
            
            // Configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(producto.getId() == null ? "Nuevo Producto" : "Editar Producto: " + producto.getNombre());
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Configurar el botón de guardar
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Guardar");
            
            // Deshabilitar el botón de guardar si el formulario no es válido
            BooleanProperty formularioValido = controller.getFormularioValidoProperty();
            if (formularioValido != null) {
                okButton.disableProperty().bind(formularioValido.not());
            }
            
            // Mostrar el diálogo y procesar el resultado
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Ejecutar la actualización en un hilo separado
                Task<ProductoFormController.ResultadoGuardado> tareaActualizar = new Task<ProductoFormController.ResultadoGuardado>() {
                    @Override
                    protected ProductoFormController.ResultadoGuardado call() throws Exception {
                        return controller.guardar();
                    }
                };
                
                // Mostrar indicador de carga
                ProgressIndicator progressIndicator = new ProgressIndicator();
                progressIndicator.setMaxSize(30, 30);
                
                // Crear diálogo de carga
                Dialog<Void> loadingDialog = new Dialog<>();
                loadingDialog.initModality(Modality.APPLICATION_MODAL);
                loadingDialog.setTitle("Guardando producto");
                loadingDialog.setHeaderText("Por favor espere...");
                loadingDialog.setGraphic(progressIndicator);
                loadingDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                
                // Configurar comportamiento del botón cancelar
                Button cancelButton = (Button) loadingDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                cancelButton.setText("Cancelar");
                cancelButton.setOnAction(e -> {
                    tareaActualizar.cancel(true);
                    loadingDialog.close();
                    if (controller.saveButton != null) {
                        controller.saveButton.setDisable(false);
                    }
                });
                
                // Configurar diálogo de carga con barra de progreso y estilo mejorado
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(350);
                progressBar.setProgress(-1); // Indicador indeterminado
                progressBar.setStyle("-fx-accent: #4CAF50;");
                
                Label loadingLabel = new Label("Guardando producto...");
                loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                
                VBox loadingContent = new VBox(15);
                loadingContent.setAlignment(Pos.CENTER);
                loadingContent.setPadding(new Insets(25));
                loadingContent.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
                loadingContent.getChildren().addAll(
                    loadingLabel,
                    progressBar
                );
                
                // Configurar el diálogo de carga
                loadingDialog.initModality(Modality.APPLICATION_MODAL);
                loadingDialog.setTitle("Procesando...");
                loadingDialog.getDialogPane().setContent(loadingContent);
                loadingDialog.getDialogPane().getButtonTypes().clear();
                
                // Mostrar diálogo de carga en el hilo de la interfaz
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.show();
                        // Centrar el diálogo en la pantalla
                        Stage stage = (Stage) loadingDialog.getDialogPane().getScene().getWindow();
                        stage.centerOnScreen();
                    }
                });
                
                tareaActualizar.setOnSucceeded(e -> {
                    Platform.runLater((Runnable)() -> {
                        try {
                            // Cerrar diálogo de carga
                            if (loadingDialog.isShowing()) {
                                loadingDialog.close();
                            }
                            
                            ProductoFormController.ResultadoGuardado resultado = tareaActualizar.getValue();
                            
                            if (resultado.isExito()) {
                                // Construir mensaje de éxito con formato mejorado
                                String titulo = "¡Operación Exitosa! 🎉";
                                StringBuilder mensaje = new StringBuilder();
                                mensaje.append("El producto se ha guardado correctamente.\n\n");
                                
                                // Agregar detalles adicionales
                                mensaje.append("• ").append(resultado.getMensaje()).append("\n");
                                if (resultado.isTieneImagen()) {
                                    mensaje.append("• ✅ Imagen cargada exitosamente\n");
                                }
                                
                                // Mostrar notificación emergente no bloqueante
                                mostrarNotificacionEmergente("✅ Operación exitosa", "Los cambios se han guardado correctamente");
                                
                                // Crear diálogo de éxito con opciones
                                Alert alertaExito = new Alert(Alert.AlertType.INFORMATION);
                                alertaExito.setTitle(titulo);
                                alertaExito.setHeaderText("Operación completada con éxito");
                                alertaExito.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                                
                                // Configurar botones personalizados
                                ButtonType verProductoBtn = new ButtonType("Ver Producto", ButtonBar.ButtonData.YES);
                                ButtonType nuevoProductoBtn = new ButtonType("Nuevo Producto", ButtonBar.ButtonData.APPLY);
                                ButtonType continuarBtn = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
                                alertaExito.getButtonTypes().setAll(verProductoBtn, nuevoProductoBtn, continuarBtn);
                                
                                // Configurar el contenido del diálogo
                                Label contenido = new Label(mensaje.toString());
                                contenido.setStyle("-fx-font-size: 14px; -fx-padding: 10 0 15 0;");
                                contenido.setWrapText(true);
                                contenido.setMaxWidth(400);
                                alertaExito.getDialogPane().setContent(contenido);
                                
                                // Manejar la respuesta del usuario
                                alertaExito.showAndWait().ifPresent(buttonType -> {
                                    if (buttonType == verProductoBtn) {
                                        // Navegar al producto recién creado/actualizado
                                        // Aquí iría la lógica para mostrar el producto
                                        System.out.println("Navegando al producto...");
                                    } else if (buttonType == nuevoProductoBtn) {
                                        // Crear un nuevo producto
                                        System.out.println("Creando nuevo producto...");
                                        // Aquí iría la lógica para crear un nuevo producto
                                    }
                                });
                                
                                // Cerrar el diálogo actual y recargar la lista de productos
                                dialog.close();
                                cargarProductos();
                                
                            } else {
                                // Mostrar mensaje de error con formato mejorado
                                String titulo = "⚠️ Error al guardar";
                                String mensajeError = resultado.getMensaje();
                                
                                // Crear diálogo de error con más detalles
                                Alert alertaError = new Alert(Alert.AlertType.ERROR);
                                alertaError.setTitle(titulo);
                                alertaError.setHeaderText("No se pudo completar la operación");
                                
                                // Crear área de texto para el mensaje de error
                                TextArea textArea = new TextArea(mensajeError);
                                textArea.setEditable(false);
                                textArea.setWrapText(true);
                                textArea.setMaxWidth(Double.MAX_VALUE);
                                textArea.setMaxHeight(Double.MAX_VALUE);
                                
                                // Crear contenedor para el mensaje
                                VBox content = new VBox(10);
                                content.setPadding(new Insets(10));
                                content.getChildren().addAll(
                                    new Label("Se produjo el siguiente error:"),
                                    textArea,
                                    new Label("Por favor, verifica los datos e inténtalo nuevamente.")
                                );
                                
                                // Configurar el diálogo
                                alertaError.getDialogPane().setContent(content);
                                alertaError.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                                alertaError.getButtonTypes().setAll(ButtonType.OK);
                                
                                // Mostrar diálogo
                                alertaError.showAndWait();
                                
                                // Reactivar el botón de guardar
                                if (controller.saveButton != null) {
                                    controller.saveButton.setDisable(false);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            loadingDialog.close();
                            AlertUtils.mostrarError("Error inesperado", "Ocurrió un error inesperado al procesar la respuesta.");
                            if (controller.saveButton != null) {
                                controller.saveButton.setDisable(false);
                            }
                        }
                    });
                });
                
                tareaActualizar.setOnFailed(e -> {
                    Platform.runLater((Runnable)() -> {
                        try {
                            loadingDialog.close();
                            Throwable ex = tareaActualizar.getException();
                            String errorMsg = ex != null ? ex.getMessage() : "Error desconocido al procesar la solicitud";
                            
                            // Mostrar error detallado
                            Alert alertaError = new Alert(Alert.AlertType.ERROR);
                            alertaError.setTitle("❌ Error crítico");
                            alertaError.setHeaderText("No se pudo completar la operación");
                            
                            // Mensaje más descriptivo según el tipo de error
                            String mensajeDetallado = "Se produjo un error al intentar guardar el producto.\n\n";
                            if (ex != null && ex.getCause() != null) {
                                mensajeDetallado += "Causa: " + ex.getCause().getMessage();
                            } else {
                                mensajeDetallado += "Detalles: " + errorMsg;
                            }
                            
                            alertaError.setContentText(mensajeDetallado);
                            
                            // Añadir área de texto para el stack trace en desarrollo
                            if (ex != null) {
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                ex.printStackTrace(pw);
                                String exceptionText = sw.toString();
                                
                                TextArea textArea = new TextArea(exceptionText);
                                textArea.setEditable(false);
                                textArea.setWrapText(true);
                                textArea.setMaxWidth(Double.MAX_VALUE);
                                textArea.setMaxHeight(Double.MAX_VALUE);
                                
                                GridPane.setVgrow(textArea, Priority.ALWAYS);
                                GridPane.setHgrow(textArea, Priority.ALWAYS);
                                
                                GridPane expContent = new GridPane();
                                expContent.setMaxWidth(Double.MAX_VALUE);
                                expContent.add(new Label("Detalles del error:"), 0, 0);
                                expContent.add(textArea, 0, 1);
                                
                                alertaError.getDialogPane().setExpandableContent(expContent);
                            }
                            
                            alertaError.showAndWait();
                            
                            // Reproducir sonido de error
                            playSound("/sounds/error.wav");
                            
                        } finally {
                            // Asegurarse de que el diálogo de carga se cierre
                            if (loadingDialog.isShowing()) {
                                loadingDialog.close();
                            }
                            
                            // Reactivar el botón de guardar
                            if (controller.saveButton != null) {
                                controller.saveButton.setDisable(false);
                            }
                        }
                    });
                });
                
                new Thread(tareaActualizar).start();
                
                // No cerrar el diálogo aquí, esperar a que termine la operación
                return;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", 
                "No se pudo abrir el formulario de edición: " + e.getMessage());
        }
    }

    /**
     * Muestra una notificación emergente en la esquina superior derecha de la pantalla.
     * @param titulo Título de la notificación
     * @param mensaje Mensaje a mostrar
     */
    /**
     * Reproduce un sonido desde la ruta especificada.
     * @param soundPath Ruta del archivo de sonido relativa a la carpeta de recursos
     */
    @SuppressWarnings("unused")
    private void playSound(String soundPath) {
        try {
            // Obtener la URL del recurso
            String soundUrl = getClass().getResource(soundPath).toExternalForm();
            
            // Crear y reproducir el sonido
            Media sound = new Media(soundUrl);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
            
            // Liberar recursos cuando termine de reproducirse
            mediaPlayer.setOnEndOfMedia(mediaPlayer::dispose);
        } catch (Exception e) {
            System.err.println("No se pudo reproducir el sonido: " + e.getMessage());
            // No es crítico, continuar sin sonido
        }
    }
    
    /**
     * Muestra una notificación emergente en la esquina superior derecha de la pantalla.
     * @param titulo Título de la notificación
     * @param mensaje Mensaje a mostrar
     */
    private void mostrarNotificacionEmergente(String titulo, String mensaje) {
        // Crear una alerta personalizada
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        // Configurar posición
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setX(stage.getX() + 20);
        stage.setY(20);
        
        // Mostrar y ocultar automáticamente después de 3 segundos
        alert.show();
        
        // Cerrar automáticamente después de 3 segundos
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(alert::close);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    @SuppressWarnings("unused")
    private void configurarComboMenus() {
        // Si no hay un ComboBox de menú, no hacemos nada
        if (cmbMenu == null) {
            return;
        }
        
        // Obtener el ID del local actual del usuario logueado
        final Long idLocal;
        Usuario usuarioActual = AuthService.getInstance().getCurrentUser();
        if (usuarioActual != null) {
            idLocal = usuarioActual.getIdLocalAsignado();
        } else {
            idLocal = null;
        }
        
        Task<List<Menu>> tarea = new Task<>() {
            @Override
            protected List<Menu> call() throws Exception {
                // Agregar una opción para mostrar todos los menús
                Menu todos = new Menu();
                todos.setId(null);
                todos.setNombre("Todos los menús");
                
                List<Menu> menus = new ArrayList<>();
                menus.add(todos);
                
                if (idLocal != null) {
                    // Obtener solo los menús del local actual
                    menus.addAll(menuService.obtenerPorLocal(idLocal));
                } else {
                    // Si no hay local asignado, obtener todos los menús
                    menus.addAll(menuService.obtenerTodos());
                }
                
                return menus;
            }
        };

        tarea.setOnSucceeded(e -> {
            try {
                List<Menu> menus = tarea.getValue();
                if (cmbMenu != null) {
                    cmbMenu.getItems().setAll(menus);
                    
                    // Seleccionar la opción por defecto (todos los menús)
                    if (!menus.isEmpty()) {
                        cmbMenu.getSelectionModel().select(0);
                    }
                }
            } catch (Exception ex) {
                mostrarError("Error", "No se pudieron cargar los menús: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        tarea.setOnFailed(e -> {
            if (cmbMenu != null) {
                mostrarError("Error", "Error al cargar los menús: " + tarea.getException().getMessage());
            }
            tarea.getException().printStackTrace();
        });

        new Thread(tarea).start();
    }
    
    private void cargarProductos() {
        mostrarCargando(true);
        
        Task<List<Producto>> tarea = new Task<>() {
            @Override
            protected List<Producto> call() throws Exception {
                if (menuId != null) {
                    return productoService.obtenerProductosPorMenu(menuId);
                } else {
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
    private void mostrarError(String titulo, String mensaje) {
        Platform.runLater((Runnable)() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    /**
     * Muestra un mensaje informativo al usuario
     * @param titulo El título del mensaje
     * @param mensaje El contenido del mensaje
     */
    private void mostrarMensaje(String titulo, String mensaje) {
        Platform.runLater((Runnable)() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
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
            int totalPaginas = (int) Math.ceil((double) totalProductos / ITEMS_POR_PAGINA);
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
            actualizarControlesPaginacion();
        }
    }
    
    /**
     * Navega a la página siguiente
     */
    @FXML
    private void paginaSiguiente() {
        int totalPaginas = (int) Math.ceil((double) totalProductos / ITEMS_POR_PAGINA);
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            actualizarVistaProductos();
            actualizarControlesPaginacion();
        }
    }
    
    /**
     * Actualiza la vista de productos aplicando los filtros de búsqueda y paginación
     */
    private void actualizarVistaProductos() {
        try {
            // Obtener el texto de búsqueda de forma segura
            String textoBusqueda = txtBuscar != null ? txtBuscar.getText() : "";
            
            // Filtrar productos basados en el texto de búsqueda
            List<Producto> productosFiltrados = productos.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getNombre() != null && 
                           p.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase()))
                .collect(Collectors.toList());
            
            // Actualizar el contador total de productos
            totalProductos = productosFiltrados.size();
            
            // Calcular la paginación
            int fromIndex = paginaActual * ITEMS_POR_PAGINA;
            int toIndex = Math.min(fromIndex + ITEMS_POR_PAGINA, totalProductos);
            
            // Obtener la sublista para la página actual
            List<Producto> productosPagina = productosFiltrados.subList(
                fromIndex, 
                Math.min(toIndex, productosFiltrados.size())
            );
            
            // Actualizar la tabla en el hilo de la aplicación JavaFX
            Platform.runLater(() -> {
                try {
                    if (tblProductos != null) {
                        // Limpiar elementos existentes y agregar los filtrados
                        tblProductos.getItems().clear();
                        if (!productosPagina.isEmpty()) {
                            tblProductos.getItems().addAll(productosPagina);
                        }
                    }
                    // Actualizar controles de paginación
                    actualizarControlesPaginacion();
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.mostrarError("Error", "Error al actualizar la tabla: " + e.getMessage());
                } finally {
                    mostrarCargando(false);
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "Error al filtrar productos: " + e.getMessage());
            mostrarCargando(false);
        }
    }
}
