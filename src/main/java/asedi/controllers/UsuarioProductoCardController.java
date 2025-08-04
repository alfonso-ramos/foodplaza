package asedi.controllers;

import asedi.model.Producto;
import asedi.services.CarritoService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class UsuarioProductoCardController {

    @FXML
    private ImageView imagenProducto;

    @FXML
    private Label nombreLabel;

    @FXML
    private Label descripcionLabel;

    @FXML
    private Label precioLabel;

    private Producto producto;

    private final CarritoService carritoService = CarritoService.getInstance();

    public void setProducto(Producto producto) {
        this.producto = producto;
        nombreLabel.setText(producto.getNombre());
        descripcionLabel.setText(producto.getDescripcion());
        precioLabel.setText(String.format("$%.2f", producto.getPrecio()));
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
            imagenProducto.setImage(new Image(producto.getImagenUrl()));
        }
    }

    @FXML
    void agregarAlCarrito() {
        carritoService.agregarProducto(producto);
        // Opcional: mostrar una confirmación al usuario
    }
}