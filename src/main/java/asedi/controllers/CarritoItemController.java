package asedi.controllers;

import asedi.model.Producto;
import asedi.services.CarritoService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CarritoItemController {
    @FXML private ImageView imagenProducto;
    @FXML private Label nombreLabel;
    @FXML private Label descripcionLabel;
    @FXML private Label precioUnitarioLabel;
    @FXML private Label cantidadLabel;
    @FXML private Label subtotalLabel;
    
    private Producto producto;
    private int cantidad = 1;
    private CarritoService carritoService = CarritoService.getInstance();
    private Runnable onUpdateCallback;
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        actualizarVista();
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        actualizarVista();
    }
    
    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }
    
    private void actualizarVista() {
        if (producto != null) {
            nombreLabel.setText(producto.getNombre());
            descripcionLabel.setText(producto.getDescripcion());
            precioUnitarioLabel.setText(String.format("$%.2f", producto.getPrecio()));
            cantidadLabel.setText(String.valueOf(cantidad));
            actualizarSubtotal();
            
            if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
                imagenProducto.setImage(new Image(producto.getImagenUrl()));
            }
        }
    }
    
    private void actualizarSubtotal() {
        double subtotal = producto.getPrecio() * cantidad;
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }
    
    @FXML
    private void incrementarCantidad() {
        cantidad++;
        actualizarVista();
        carritoService.actualizarCantidad(producto, cantidad);
    }
    
    @FXML
    private void decrementarCantidad() {
        if (cantidad > 1) {
            cantidad--;
            actualizarVista();
            carritoService.actualizarCantidad(producto, cantidad);
        }
    }
    
    @FXML
    private void eliminarDelCarrito() {
        carritoService.removerProducto(producto);
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }
}
