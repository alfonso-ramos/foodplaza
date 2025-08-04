package asedi.controllers.gerencia.components;

import java.io.IOException;
import java.util.function.Consumer;

import asedi.model.Producto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ProductoCardController extends VBox {
    
    @FXML private ImageView imgProducto;
    @FXML private Label lblNombre;
    @FXML private Label lblPrecio;
    @FXML private Label lblCategoria;
    @FXML private Label lblDescripcion;
    @FXML private Label lblDisponible;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    
    private Producto producto;
    private Consumer<Producto> onEditarAction;
    private Consumer<Producto> onEliminarAction;
    
    public ProductoCardController() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/views/gerencia/productos/components/producto_card.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Error loading ProductoCard", e);
        }
    }
    
    @FXML
    private void initialize() {
        // Configuración adicional si es necesaria
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        actualizarVista();
    }
    
    private void actualizarVista() {
        if (producto == null) return;
        
        lblNombre.setText(producto.getNombre());
        lblPrecio.setText(String.format("S/%.2f", producto.getPrecio() != null ? producto.getPrecio() : 0));
        lblCategoria.setText(producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
        lblDescripcion.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "");
        
        // Configurar disponibilidad
        boolean disponible = producto.getDisponible() != null && producto.getDisponible();
        lblDisponible.setText(disponible ? "Disponible" : "No disponible");
        
        if (!disponible) {
            getStyleClass().add("unavailable");
        } else {
            getStyleClass().remove("unavailable");
        }
        
        // Cargar imagen si existe
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
            try {
                Image image = new Image(producto.getImagenUrl(), true);
                imgProducto.setImage(image);
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + e.getMessage());
                // Set a default image if available
                try {
                    Image defaultImage = new Image("/images/default-product.png");
                    imgProducto.setImage(defaultImage);
                } catch (Exception ex) {
                    // Ignore if default image fails to load
                }
            }
        } else {
            // Set a default image if no image URL is provided
            try {
                Image defaultImage = new Image("/images/default-product.png");
                imgProducto.setImage(defaultImage);
            } catch (Exception ex) {
                // Ignore if default image fails to load
            }
        }
    }
    
    public void setOnEditarAction(Consumer<Producto> action) {
        this.onEditarAction = action;
    }
    
    public void setOnEliminarAction(Consumer<Producto> action) {
        this.onEliminarAction = action;
    }
    
    /**
     * Establece la imagen del producto directamente
     * @param imagen La imagen a mostrar
     */
    public void setImagenProducto(Image imagen) {
        if (imagen != null) {
            imgProducto.setImage(imagen);
        } else {
            // Set a default image if no image is provided
            try {
                Image defaultImage = new Image("/images/default-product.png");
                imgProducto.setImage(defaultImage);
            } catch (Exception ex) {
                // Ignore if default image fails to load
            }
        }
    }
    
    @FXML
    private void editarProducto() {
        if (onEditarAction != null) {
            onEditarAction.accept(producto);
        }
    }
    
    @FXML
    private void eliminarProducto() {
        if (onEliminarAction != null) {
            onEliminarAction.accept(producto);
        }
    }
    
    public Producto getProducto() {
        return producto;
    }
}
