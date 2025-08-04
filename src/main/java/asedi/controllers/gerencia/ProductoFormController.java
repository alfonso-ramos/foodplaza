package asedi.controllers.gerencia;

import asedi.model.Producto;
import asedi.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.event.ActionEvent;

import java.io.File;

public class ProductoFormController {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private CheckBox chkDisponible;
    @FXML private ImageView imgPreview;
    
    private Producto producto;
    private boolean guardado = false;
    private File archivoImagen;
    
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
        });
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
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
    
    public boolean validarFormulario() {
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
                Image imagen = new Image(archivoImagen.toURI().toString());
                imgPreview.setImage(imagen);
                
                // Aquí podrías subir la imagen al servidor si es necesario
                // y guardar la URL en el producto
                // producto.setImagenUrl(urlDeLaImagenSubida);
                
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
        return guardado;
    }
    
    @FXML
    private void guardar() {
        if (validarFormulario()) {
            guardado = true;
            // Cerrar el diálogo
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.close();
        }
    }
    
    @FXML
    private void cancelar() {
        guardado = false;
        // Cerrar el diálogo
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}
