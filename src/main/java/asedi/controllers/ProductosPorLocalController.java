package asedi.controllers;

import asedi.model.Local;
import asedi.model.Producto;
import asedi.services.ProductoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductosPorLocalController {

    @FXML
    private Label localNombreLabel;

    @FXML
    private FlowPane productosContainer;
    
    @FXML
    private StackPane loadingContainer;

    private Local local;

    private final ProductoService productoService = new ProductoService();

    public void setLocal(Local local) {
        this.local = local;
        localNombreLabel.setText("Productos en " + local.getNombre());
        cargarProductos();
    }

    private void cargarProductos() {
        // Mostrar el indicador de carga
        showLoading(true);
        
        // Ejecutar en un hilo separado para no bloquear la interfaz de usuario
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                // Obtener los productos del servicio
                List<Producto> productos = productoService.obtenerProductosPorLocal(local.getId());
                
                // Actualizar la interfaz de usuario en el hilo de JavaFX
                Platform.runLater(() -> {
                    try {
                        productosContainer.getChildren().clear();
                        
                        if (productos.isEmpty()) {
                            // Mostrar mensaje si no hay productos
                            Label noProductsLabel = new Label("No hay productos disponibles en este local.");
                            noProductsLabel.getStyleClass().add("no-products-message");
                            productosContainer.getChildren().add(noProductsLabel);
                        } else {
                            // Cargar y mostrar las tarjetas de productos
                            for (Producto producto : productos) {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/usuarioProductoCard.fxml"));
                                    Parent productoCard = loader.load();
                                    UsuarioProductoCardController controller = loader.getController();
                                    controller.setProducto(producto);
                                    // Aplicar márgenes a cada tarjeta
                                    VBox.setMargin(productoCard, new Insets(0, 0, 20, 0));
                                    productosContainer.getChildren().add(productoCard);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showError("Error al cargar un producto: " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Error al cargar los productos: " + e.getMessage());
                    } finally {
                        // Ocultar el indicador de carga cuando termine la carga
                        showLoading(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Error al obtener los productos: " + e.getMessage());
                    showLoading(false);
                });
            }
        });
        
        // Cerrar el executor cuando ya no sea necesario
        executor.shutdown();
    }
    
    private void showLoading(boolean show) {
        loadingContainer.setVisible(show);
        loadingContainer.setManaged(show);
        productosContainer.setVisible(!show);
        productosContainer.setManaged(!show);
    }
    
    private void showError(String message) {
        productosContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-message");
        productosContainer.getChildren().add(errorLabel);
    }
}