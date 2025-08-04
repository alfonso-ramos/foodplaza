package asedi.controllers;

import asedi.model.Pedido;
import asedi.model.Producto;
import asedi.services.AuthService;
import asedi.services.CarritoService;
import asedi.services.PedidoService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class CarritoController {

    @FXML
    private VBox carritoContainer;

    @FXML
    private Label totalLabel;

    private final CarritoService carritoService = CarritoService.getInstance();
    private final PedidoService pedidoService = new PedidoService();

    @FXML
    public void initialize() {
        cargarCarrito();
    }

    private void cargarCarrito() {
        carritoContainer.getChildren().clear();
        List<Producto> productos = carritoService.getProductos();
        double total = 0;
        for (Producto producto : productos) {
            Label label = new Label(producto.getNombre() + " - $" + producto.getPrecio());
            carritoContainer.getChildren().add(label);
            total += producto.getPrecio();
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    void realizarPedido() throws IOException {
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(AuthService.getInstance().getCurrentUser().getId());
        // TODO: Set the local ID
        pedido.setProductos(carritoService.getProductos());
        pedido.setTotal(carritoService.getProductos().stream().mapToDouble(Producto::getPrecio).sum());

        pedidoService.crearPedido(pedido);
        carritoService.limpiarCarrito();
        cargarCarrito();
    }
}
