package asedi.controllers.gerencia;

import asedi.model.Producto;
import asedi.services.ProductoService;
import asedi.utils.AlertUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;


public class ProductoFormController {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private CheckBox chkDisponible;
    @FXML private ImageView imgPreview;
    @FXML private Text txtTitulo;
    
    private Producto producto;
    private ProductoService productoService;
    private File archivoImagen;
    private final BooleanProperty formularioValido = new SimpleBooleanProperty(false);
    
    public void setProductoService(ProductoService productoService) {
        this.productoService = productoService;
    }
    
    @FXML
    public void initialize() {
        // Inicializar el ComboBox de categorías
        cmbCategoria.getItems().addAll(
            "Entrada", "Plato Principal", "Postre", "Bebida", "Acompañamiento"
        );
        
        // Configurar el campo de precio para aceptar solo números
        txtPrecio.textProperty().addListener((_, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtPrecio.setText(oldValue);
            }
            actualizarValidezFormulario();
        });
        
        // Agregar listeners a los campos para validar el formulario
        txtNombre.textProperty().addListener((_, _, _) -> actualizarValidezFormulario());
        cmbCategoria.valueProperty().addListener((_, _, _) -> actualizarValidezFormulario());
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        
        // Establecer el título del formulario
        if (producto == null || producto.getId() == null) {
            txtTitulo.setText("Nuevo Producto");
            this.producto = new Producto();
        } else {
            txtTitulo.setText("Editar Producto");
            // Cargar los datos del producto en el formulario
            txtNombre.setText(producto.getNombre());
            txtDescripcion.setText(producto.getDescripcion());
            txtPrecio.setText(String.valueOf(producto.getPrecio()));
            cmbCategoria.setValue(producto.getCategoria());
            chkDisponible.setSelected(Boolean.TRUE.equals(producto.getDisponible()));
            
            // Cargar la imagen si existe
            if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
                try {
                    Image imagen = new Image(producto.getImagenUrl(), true);
                    imgPreview.setImage(imagen);
                } catch (Exception e) {
                    System.err.println("Error al cargar la imagen: " + e.getMessage());
                }
            }
        }
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
    
    private void actualizarValidezFormulario() {
        boolean valido = true;
        
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            valido = false;
        }
        
        if (cmbCategoria.getValue() == null || cmbCategoria.getValue().isEmpty()) {
            valido = false;
        }
        
        try {
            if (txtPrecio.getText() == null || txtPrecio.getText().trim().isEmpty()) {
                valido = false;
            } else {
                Double.parseDouble(txtPrecio.getText());
            }
        } catch (NumberFormatException e) {
            valido = false;
        }
        
        formularioValido.set(valido);
    }
    
    public BooleanProperty getFormularioValidoProperty() {
        return formularioValido;
    }
    
    public boolean validarFormulario() {
        actualizarValidezFormulario();
        
        if (!formularioValido.get()) {
            AlertUtils.mostrarError("Error de validación", 
                "Por favor complete todos los campos obligatorios correctamente.");
            return false;
        }
        
        StringBuilder errores = new StringBuilder();
        
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }
        
        if (txtPrecio.getText().trim().isEmpty()) {
            errores.append("• El precio es obligatorio\n");
        } else {
            try {
                double precio = Double.parseDouble(txtPrecio.getText());
                if (precio <= 0) {
                    errores.append("• El precio debe ser mayor a cero\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• El precio debe ser un número válido\n");
            }
        }
        
        if (cmbCategoria.getValue() == null || cmbCategoria.getValue().isEmpty()) {
            errores.append("• La categoría es obligatoria\n");
        }
        
        if (errores.length() > 0) {
            AlertUtils.mostrarError("Error de validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void seleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Producto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        Window stage = ((Node) event.getSource()).getScene().getWindow();
        archivoImagen = fileChooser.showOpenDialog(stage);
        
        if (archivoImagen != null) {
            try {
                // Cargar la imagen para previsualización
                Image imagen = new Image(archivoImagen.toURI().toString(), 150, 150, true, true);
                imgPreview.setImage(imagen);
            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "No se pudo cargar la imagen: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void eliminarImagen(ActionEvent event) {
        imgPreview.setImage(null);
        archivoImagen = null;
        if (producto != null) {
            producto.setImagenUrl(null);
            producto.setImagenPublicId(null);
        }
    }
    
    public boolean isGuardado() {
        return producto != null && producto.getId() != null;
    }
    
    @FXML
    public void guardar() {
        if (validarFormulario()) {
            try {
                // Obtener los datos del formulario
                Producto producto = getProducto();
                
                // Subir imagen si se seleccionó una
                if (archivoImagen != null) {
                    try {
                        String urlImagen = productoService.subirImagen(archivoImagen);
                        producto.setImagenUrl(urlImagen);
                    } catch (IOException e) {
                        AlertUtils.mostrarError("Error", "No se pudo subir la imagen: " + e.getMessage());
                        return;
                    }
                }
                
                // Guardar el producto
                if (producto.getId() == null) {
                    // Crear nuevo producto
                    productoService.crear(producto);
                } else {
                    // Actualizar producto existente
                    productoService.actualizar(producto);
                }
                
                // Cerrar el diálogo
                Stage stage = (Stage) txtNombre.getScene().getWindow();
                stage.close();
                
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.mostrarError("Error", "No se pudo guardar el producto: " + e.getMessage());
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
            
            if (confirmado) {
                // Cerrar el diálogo
                Stage stage = (Stage) txtNombre.getScene().getWindow();
                stage.close();
            }
        } else {
            // Cerrar el diálogo directamente si no hay cambios
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.close();
        }
    }
    
    private boolean hayCambiosSinGuardar() {
        // Verificar si hay cambios en los campos del formulario
        if (producto == null) return false;
        
        boolean nombreCambiado = !txtNombre.getText().equals(producto.getNombre() != null ? producto.getNombre() : "");
        boolean descripcionCambiada = !txtDescripcion.getText().equals(producto.getDescripcion() != null ? producto.getDescripcion() : "");
        boolean precioCambiado = !txtPrecio.getText().equals(String.valueOf(producto.getPrecio() != null ? producto.getPrecio() : ""));
        boolean categoriaCambiada = !cmbCategoria.getValue().equals(producto.getCategoria() != null ? producto.getCategoria() : "");
        boolean disponibleCambiado = chkDisponible.isSelected() != (producto.getDisponible() != null && producto.getDisponible());
        
        return nombreCambiado || descripcionCambiada || precioCambiado || categoriaCambiada || 
               disponibleCambiado || archivoImagen != null;
    }
}
