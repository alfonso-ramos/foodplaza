package asedi.controllers.gerencia;

import asedi.model.Producto;
import asedi.services.ProductoService;
import asedi.utils.AlertUtils;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductoFormController {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private CheckBox chkDisponible;
    @FXML private ImageView imgPreview;
    @FXML private Label txtTitulo;
    @FXML private Button saveButton;
    
    private Producto producto;
    private ProductoService productoService;
    private Long menuId;
    private File archivoImagen;
    private final BooleanProperty formularioValido = new SimpleBooleanProperty(false);
    // Aumentar el tiempo de debounce para reducir la frecuencia de validaciones
    private final PauseTransition validationTimer = new PauseTransition(Duration.millis(700));
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isValidating = false;
    private volatile boolean isLoading = false;
    private final Object lock = new Object();
    private String lastValidationState = ""; // Cache del último estado de validación
    
    /**
     * Establece el servicio de productos.
     */
    public void setProductoService(ProductoService productoService) {
        this.productoService = productoService;
    }
    
    public BooleanProperty getFormularioValidoProperty() {
        return formularioValido;
    }
    
    private void cargarImagenEnSegundoPlano(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        Task<Image> imageLoadTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try {
                    return new Image(imageUrl, true);
                } catch (Exception e) {
                    System.err.println("Error al cargar la imagen: " + e.getMessage());
                    return null;
                }
            }
        };

        imageLoadTask.setOnSucceeded(event -> {
            Image image = imageLoadTask.getValue();
            if (image != null && !image.isError()) {
                Platform.runLater(() -> {
                    imgPreview.setImage(image);
                });
            }
        });

        new Thread(imageLoadTask).start();
    }
    
    @FXML
    public void initialize() {
        // Configurar el validador de formato de precio
        TextFormatter<String> precioFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*\\.?\\d{0,2}")) {
                return change;
            }
            return null;
        });
        txtPrecio.setTextFormatter(precioFormatter);
        
        // Configurar el validador de nombre (solo letras, números y espacios)
        TextFormatter<String> nombreFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[a-zA-Z0-9áéíóúÁÉÍÓÚüÜñÑ\\s]*")) {
                return change;
            }
            return null;
        });
        txtNombre.setTextFormatter(nombreFormatter);
        
        // Cargar las categorías
        cmbCategoria.getItems().addAll(
            "Entrada", "Plato Principal", "Postre", "Bebida", "Acompañamiento"
        );
        
        // Deshabilitar la validación en tiempo real
        validationTimer.setOnFinished(null);
        
        // Desvincular la validación del botón de guardar
        saveButton.disableProperty().unbind();
        saveButton.setDisable(false);
    }
    
    /**
     * Establece el ID del menú al que pertenece el producto.
     * @param menuId ID del menú
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    
    /**
     * Establece el producto a editar.
     */
    public void setProducto(Producto producto) {
        System.out.println("setProducto() - Inicio");
        synchronized (lock) {
            this.producto = producto;
            
            // Establecer el título del formulario
            if (producto == null || producto.getId() == null) {
                Platform.runLater(() -> txtTitulo.setText("Nuevo Producto"));
                System.out.println("Modo: Nuevo Producto");
            } else {
                Platform.runLater(() -> txtTitulo.setText("Editar Producto"));
                System.out.println("Modo: Editar Producto - ID: " + producto.getId());
            }
        }
        
        if (producto != null) {
            cargarDatosProducto();
        } else {
            // Limpiar el formulario de manera segura
            Platform.runLater(() -> {
                try {
                    boolean oldValidating = isValidating;
                    isValidating = true;
                    
                    txtNombre.clear();
                    txtDescripcion.clear();
                    txtPrecio.clear();
                    cmbCategoria.getSelectionModel().clearSelection();
                    chkDisponible.setSelected(false);
                    imgPreview.setImage(null);
                    archivoImagen = null;
                    
                    isValidating = oldValidating;
                } catch (Exception e) {
                    System.err.println("Error al limpiar el formulario: " + e.getMessage());
                    isValidating = false;
                }
            });
        }
    }
    
    /**
     * Carga los datos del producto en el formulario de manera segura
     */
    private void cargarDatosProducto() {
        if (producto == null) return;
        
        System.out.println("Iniciando carga de datos del producto...");
        synchronized (lock) {
            if (isLoading) {
                System.out.println("Ya se está cargando un producto, ignorando solicitud");
                return;
            }
            isLoading = true;
            isValidating = true; // Evitar validaciones durante la carga
        }
        
        // Preparar los datos fuera del hilo de UI
        String nombre = producto.getNombre() != null ? producto.getNombre() : "";
        String descripcion = producto.getDescripcion() != null ? producto.getDescripcion() : "";
        String precio = producto.getPrecio() != null ? String.format("%.2f", producto.getPrecio()) : "";
        String categoria = producto.getCategoria();
        Boolean disponible = producto.getDisponible() != null && producto.getDisponible();
        String imagenUrl = producto.getImagenUrl();
        
        // Agrupar todas las actualizaciones de UI en un solo Platform.runLater
        Platform.runLater(() -> {
            try {
                // Desactivar validación temporalmente
                synchronized (lock) {
                    isValidating = true;
                }
                
                // Deshabilitar la actualización de la escena durante la carga
                boolean wasCache = imgPreview.isCache();
                imgPreview.setCache(true);
                
                // Actualizar todos los controles de una sola vez
                txtNombre.setText(nombre);
                txtDescripcion.setText(descripcion);
                txtPrecio.setText(precio);
                
                if (categoria != null && !categoria.isEmpty()) {
                    cmbCategoria.setValue(categoria);
                } else {
                    cmbCategoria.getSelectionModel().clearSelection();
                }
                
                chkDisponible.setSelected(disponible);
                
                // Cargar la imagen en segundo plano solo si es necesario
                if (imagenUrl != null && !imagenUrl.isEmpty()) {
                    cargarImagenEnSegundoPlano(imagenUrl);
                } else {
                    imgPreview.setImage(null);
                }
                
                imgPreview.setCache(wasCache);
                
                // Restaurar estado y forzar validación en el siguiente ciclo de eventos
                Platform.runLater(() -> {
                    synchronized (lock) {
                        isValidating = false;
                        isLoading = false;
                    }
                });
            } catch (Exception e) {
                System.err.println("Error al cargar datos del producto: " + e.getMessage());
                e.printStackTrace();
                isValidating = false;
                isLoading = false;
            }
        });
    }
    
    public Producto getProducto() {
        if (producto == null) {
            producto = new Producto();
        }
        
        // Actualizar los datos del producto con los valores del formulario
        producto.setNombre(txtNombre.getText().trim());
        producto.setDescripcion(txtDescripcion.getText().trim());
        
        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            producto.setPrecio(precio);
        } catch (NumberFormatException e) {
            producto.setPrecio(0.0);
        }
        
        producto.setCategoria(cmbCategoria.getValue());
        producto.setDisponible(chkDisponible.isSelected());
        
        return producto;
    }
    
    public boolean validarFormulario() {
        System.out.println("=== Iniciando validación del formulario ===");
        // Validación síncrona para asegurar los valores actuales
        String nombre = txtNombre != null ? txtNombre.getText().trim() : "";
        String categoria = cmbCategoria != null ? cmbCategoria.getValue() : null;
        String precio = txtPrecio != null ? txtPrecio.getText().trim() : "";
        
        System.out.println("Valores a validar:");
        System.out.println("- Nombre: '" + nombre + "'");
        System.out.println("- Categoría: '" + categoria + "'");
        System.out.println("- Precio: '" + precio + "'");
        
        // Verificar si hay algún error
        boolean tieneErrores = false;
        StringBuilder errores = new StringBuilder("Por favor complete los siguientes campos correctamente:\n\n");
        
        // Validar nombre
        if (nombre.isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
            tieneErrores = true;
        }
        
        // Validar categoría
        if (categoria == null || categoria.isEmpty()) {
            errores.append("• La categoría es obligatoria\n");
            tieneErrores = true;
        }
        
        // Validar precio
        if (precio.isEmpty()) {
            errores.append("• El precio es obligatorio\n");
            tieneErrores = true;
        } else {
            try {
                double precioValue = Double.parseDouble(precio);
                if (precioValue <= 0) {
                    errores.append("• El precio debe ser mayor a cero\n");
                    tieneErrores = true;
                }
            } catch (NumberFormatException e) {
                errores.append("• El precio debe ser un número válido\n");
                tieneErrores = true;
            }
        }
        
        // Mostrar errores si los hay
        if (tieneErrores) {
            String mensajeError = errores.toString();
            System.out.println("Errores de validación encontrados: " + mensajeError);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de validación");
                alert.setHeaderText(null);
                alert.setContentText(mensajeError);
                alert.showAndWait();
            });
            return false;
        }
        
        System.out.println("Validación exitosa");
        return true;
    }
    
    @FXML
    public void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen del producto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        Stage stage = (Stage) imgPreview.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                // Verificar tamaño del archivo (máximo 5MB)
                long fileSizeInMB = file.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    AlertUtils.mostrarError("Error", "La imagen no puede pesar más de 5MB");
                    return;
                }
                
                // Cargar la imagen en segundo plano
                cargarImagenEnSegundoPlano(file.toURI().toString());
                archivoImagen = file;
                
            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "No se pudo cargar la imagen: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void eliminarImagen() {
        if (archivoImagen != null) {
            archivoImagen = null;
            // Restaurar la imagen por defecto
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/placeholder-product.png"));
            imgPreview.setImage(defaultImage);
        }
    }
    
    @FXML
    public boolean guardar() {
        System.out.println("=== Iniciando método guardar() ===");
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        
        try {
            System.out.println("Validando formulario...");
            if (!validarFormulario()) {
                System.out.println("Validación fallida, deteniendo guardado");
                if (saveButton != null) {
                    saveButton.setDisable(false);
                }
                return false;
            }
            
            System.out.println("Validación exitosa, procediendo con el guardado");
            
            setControlesHabilitados(false);
            
            if (producto == null) {
                producto = new Producto();
            }
            
            // Establecer todos los campos requeridos
            producto.setNombre(txtNombre.getText().trim());
            producto.setCategoria(cmbCategoria.getValue());
            producto.setDisponible(chkDisponible.isSelected());
            
            // Asegurarse de que la descripción no sea nula
            String descripcion = txtDescripcion != null ? txtDescripcion.getText() : "";
            producto.setDescripcion(descripcion);
            
            // Asegurarse de que las URLs de imagen sean null si están vacías
            if (producto.getImagenUrl() != null && producto.getImagenUrl().trim().isEmpty()) {
                producto.setImagenUrl(null);
            }
            if (producto.getImagenPublicId() != null && producto.getImagenPublicId().trim().isEmpty()) {
                producto.setImagenPublicId(null);
            }
            
            // Establecer el ID del menú
            if (menuId != null) {
                producto.setIdMenu(menuId);
                System.out.println("ID de menú establecido: " + menuId);
            } else {
                // Si no se proporcionó un menuId, usar 1L como valor por defecto
                // (esto debería manejarse mejor en producción)
                producto.setIdMenu(1L);
                System.err.println("Advertencia: No se proporcionó un ID de menú, usando valor por defecto: 1");
            }
            
            try {
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                producto.setPrecio(precio);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Formato de precio inválido: " + e.getMessage(), e);
            }
            
            // Validar que el producto cumpla con todos los requisitos antes de guardar
            if (!producto.isValid()) {
                throw new IllegalArgumentException("El producto no es válido. Por favor verifica todos los campos requeridos.");
            }
            
            System.out.println("Datos del producto a guardar: " + producto);
            
            // Guardar el producto
            if (producto.getId() == null) {
                System.out.println("Creando nuevo producto...");
                Producto productoCreado = productoService.crear(producto);
                if (productoCreado == null) {
                    throw new RuntimeException("No se pudo crear el producto: respuesta nula del servidor");
                }
                // Actualizar el producto con los datos del servidor
                producto = productoCreado;
            } else {
                System.out.println("Actualizando producto existente...");
                boolean exito = productoService.actualizar(producto);
                if (!exito) {
                    throw new RuntimeException("No se pudo actualizar el producto");
                }
            }
            
            // Si hay una imagen seleccionada, subirla
            if (archivoImagen != null) {
                System.out.println("Procesando imagen: " + archivoImagen.getName());
                // Aquí iría la lógica para subir la imagen
                // productoService.subirImagen(producto.getId(), archivoImagen, "Imagen de " + producto.getNombre());
            }
            
            System.out.println("Producto guardado exitosamente con ID: " + producto.getId());
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            String mensajeError = "Error al guardar el producto: " + e.getMessage();
            System.err.println(mensajeError);
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al guardar el producto");
                alert.setContentText(mensajeError);
                alert.showAndWait();
            });
            return false;
        } finally {
            // Asegurarse de que los controles se reactiven
            setControlesHabilitados(true);
            
            // Reactivar el botón de guardar
            if (saveButton != null) {
                saveButton.setDisable(false);
            }
        }
    }
    
    @FXML
    private void cancelar() {
        // Mostrar confirmación si hay cambios sin guardar
        if (hayCambiosSinGuardar()) {
            boolean confirmado = AlertUtils.mostrarConfirmacion(
                "Confirmar cancelación",
                "¿Está seguro de que desea salir? Se perderán los cambios no guardados."
            );
            
            if (!confirmado) {
                return;
            }
        }
        
        // Cerrar el diálogo
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Habilita o deshabilita los controles del formulario
     */
    private void setControlesHabilitados(boolean habilitado) {
        if (txtNombre != null) txtNombre.setDisable(!habilitado);
        if (txtDescripcion != null) txtDescripcion.setDisable(!habilitado);
        if (txtPrecio != null) txtPrecio.setDisable(!habilitado);
        if (cmbCategoria != null) cmbCategoria.setDisable(!habilitado);
        if (chkDisponible != null) chkDisponible.setDisable(!habilitado);
    }
    
    private boolean hayCambiosSinGuardar() {
        // Verificar si hay cambios en los campos del formulario
        if (producto == null) return false;
        
        boolean nombreCambiado = !txtNombre.getText().equals(producto.getNombre() != null ? producto.getNombre() : "");
        boolean descripcionCambiada = !txtDescripcion.getText().equals(producto.getDescripcion() != null ? producto.getDescripcion() : "");
        
        String precioActual = producto.getPrecio() != null ? String.valueOf(producto.getPrecio()) : "";
        boolean precioCambiado = !txtPrecio.getText().equals(precioActual);
        
        String categoriaActual = producto.getCategoria() != null ? producto.getCategoria() : "";
        boolean categoriaCambiada = !categoriaActual.equals(cmbCategoria.getValue() != null ? cmbCategoria.getValue() : "");
        
        boolean disponibleCambiado = chkDisponible.isSelected() != (producto.getDisponible() != null && producto.getDisponible());
        
        return nombreCambiado || descripcionCambiada || precioCambiado || categoriaCambiada || 
               disponibleCambiado || archivoImagen != null;
    }
}
