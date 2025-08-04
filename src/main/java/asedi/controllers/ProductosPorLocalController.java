package asedi.controllers;

import asedi.model.Local;
import asedi.model.Menu;
import asedi.model.Producto;
import asedi.services.MenuService;
import asedi.services.ProductoService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class ProductosPorLocalController {

    @FXML
    private Label localNombreLabel;

    @FXML
    private VBox productosContainer;

    private Local local;

    private final MenuService menuService = new MenuService();
    private final ProductoService productoService = new ProductoService();

    public void setLocal(Local local) {
        this.local = local;
        localNombreLabel.setText("Productos en " + local.getNombre());
        cargarProductos();
    }

    private void cargarProductos() {
        try {
            List<Menu> menus = menuService.obtenerPorLocal(local.getId());
            productosContainer.getChildren().clear();
            for (Menu menu : menus) {
                List<Producto> productos = productoService.obtenerTodos(menu.getId());
                for (Producto producto : productos) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/usuarioProductoCard.fxml"));
                    Parent productoCard = loader.load();
                    UsuarioProductoCardController controller = loader.getController();
                    controller.setProducto(producto);
                    productosContainer.getChildren().add(productoCard);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
