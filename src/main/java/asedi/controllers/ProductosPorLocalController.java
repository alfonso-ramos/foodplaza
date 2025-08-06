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
import javafx.scene.Scene;

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
    
    @FXML
    private VBox root;

    private Local local;
    
    @FXML
    public void initialize() {
        // Aplicar estilos directamente en el código
        String css = """
            .productos-por-local {
                -fx-background-color: #f5f7fa;
                -fx-padding: 0;
            }
            
            .producto-card {
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
                -fx-padding: 15;
                -fx-spacing: 10;
                -fx-min-width: 250;
                -fx-max-width: 300;
            }
            
            .producto-nombre {
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #2c3e50;
            }
            
            .producto-descripcion {
                -fx-font-size: 14px;
                -fx-text-fill: #7f8c8d;
                -fx-wrap-text: true;
            }
            
            .producto-precio {
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #e74c3c;
            }
            
            .boton-agregar {
                -fx-background-color: #2ecc71;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 8 16;
                -fx-background-radius: 4;
                -fx-cursor: hand;
            }
            
            .boton-agregar:hover {
                -fx-background-color: #27ae60;
            }
            
            .no-products-message {
                -fx-font-size: 16px;
                -fx-text-fill: #7f8c8d;
                -fx-padding: 20;
            }
            """;
            
        root.getStylesheets().add("data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(css.getBytes()));
    }

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