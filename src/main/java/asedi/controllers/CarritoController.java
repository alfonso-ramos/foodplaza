package asedi.controllers;

import asedi.model.Producto;
import asedi.services.CarritoService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class CarritoController {

    @FXML
    private ListView<Producto> productosListView;

    @FXML
    private Label totalLabel;

    private final CarritoService carritoService = CarritoService.getInstance();

    @FXML
    public void initialize() {
        actualizarCarrito();
    }

    private void actualizarCarrito() {
        productosListView.getItems().setAll(carritoService.getProductos());
        totalLabel.setText(String.format("Total: $%.2f", carritoService.getTotal()));
    }

    @FXML
    void removerProducto() {
        Producto seleccionado = productosListView.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            carritoService.removerProducto(seleccionado);
            actualizarCarrito();
        }
    }

    @FXML
    void realizarPedido() {
        // TODO: Implementar la lógica para realizar el pedido
    }
}