package asedi.controllers;

import asedi.model.Producto;
import asedi.services.CarritoService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class NuevoCarritoController {

    @FXML private VBox itemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label ivaLabel;
    @FXML private Label totalLabel;
    @FXML private VBox emptyCartContainer;
    
    private Stage stage;
    
    private final CarritoService carritoService = CarritoService.getInstance();
    private Consumer<Void> onCartUpdate;
    
    public void setOnCartUpdate(Consumer<Void> callback) {
        this.onCartUpdate = callback;
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    public void initialize() {
        cargarCarrito();
    }

    private void cargarCarrito() {
        itemsContainer.getChildren().clear();
        Map<Producto, Integer> items = carritoService.getItems();
        
        if (items.isEmpty()) {
            mostrarCarritoVacio(true);
        } else {
            mostrarCarritoVacio(false);
            for (Map.Entry<Producto, Integer> entry : items.entrySet()) {
                itemsContainer.getChildren().add(crearItemCarrito(entry.getKey(), entry.getValue()));
            }
        }
        actualizarVista();
    }
    
    private Node crearItemCarrito(Producto producto, int cantidad) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15;");
        item.getStyleClass().add("cart-item");
        
        // Imagen del producto
        ImageView imagen = new ImageView();
        try {
            Image img = new Image(producto.getImagenUrl(), 80, 80, true, true);
            imagen.setImage(img);
            imagen.setFitWidth(80);
            imagen.setFitHeight(80);
            imagen.setPreserveRatio(true);
        } catch (Exception e) {
            // Imagen por defecto si hay un error
            imagen.setFitWidth(80);
            imagen.setFitHeight(80);
            imagen.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 20;");
        }
        
        // Información del producto
        VBox infoContainer = new VBox(5);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.setPrefWidth(300);
        
        Label nombreLabel = new Label(producto.getNombre());
        nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        Label descripcionLabel = new Label(producto.getDescripcion());
        descripcionLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
        descripcionLabel.setWrapText(true);
        
        infoContainer.getChildren().addAll(nombreLabel, descripcionLabel);
        
        // Controles de cantidad
        HBox cantidadContainer = new HBox(10);
        cantidadContainer.setAlignment(Pos.CENTER);
        
        Button btnMenos = new Button("-");
        btnMenos.getStyleClass().add("quantity-button");
        btnMenos.setOnAction(event -> {
            int cantidadActual = carritoService.getItems().getOrDefault(producto, 0);
            if (cantidadActual > 1) {
                // Si hay más de un ítem, solo quitamos uno
                carritoService.actualizarCantidad(producto, cantidadActual - 1);
            } else {
                // Si solo hay uno, lo eliminamos completamente
                carritoService.removerProducto(producto);
            }
            actualizarVista();
        });
        
        Label cantidadLabel = new Label(String.valueOf(cantidad));
        cantidadLabel.getStyleClass().add("quantity-label");
        
        Button btnMas = new Button("+");
        btnMas.getStyleClass().add("quantity-button");
        btnMas.setOnAction(event -> {
            carritoService.agregarProducto(producto);
            actualizarVista();
        });
        
        cantidadContainer.getChildren().addAll(btnMenos, cantidadLabel, btnMas);
        
        // Precio y botón de eliminar
        VBox precioContainer = new VBox(10);
        precioContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Label precioLabel = new Label(String.format("$%.2f", producto.getPrecio() * cantidad));
        precioLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #e74c3c;");
        
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("delete-button");
        btnEliminar.setOnAction(event -> {
            carritoService.removerProducto(producto);
            cargarCarrito();
        });
        
        precioContainer.getChildren().addAll(precioLabel, btnEliminar);
        
        // Añadir todo al item principal
        HBox.setHgrow(infoContainer, Priority.ALWAYS);
        item.getChildren().addAll(imagen, infoContainer, cantidadContainer, precioContainer);
        
        return item;
    }
    
    private void mostrarCarritoVacio(boolean mostrar) {
        emptyCartContainer.setVisible(mostrar);
        emptyCartContainer.setManaged(mostrar);
    }
    
    private void actualizarVista() {
        double subtotal = carritoService.getSubtotal();
        double iva = subtotal * 0.16; // 16% de IVA
        double total = subtotal + iva;
        
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        ivaLabel.setText(String.format("$%.2f", iva));
        totalLabel.setText(String.format("$%.2f", total));
        
        // Notificar a los observadores (si existen)
        if (onCartUpdate != null) {
            onCartUpdate.accept(null);
        }
    }
    
    @FXML
    private void procederAlPago() {
        try {
            if (carritoService.estaVacio()) {
                mostrarError("El carrito está vacío");
                return;
            }

            // Obtener el ID del menú (que está asociado al local) del primer producto en el carrito
            Long idLocal = carritoService.getItems().keySet().stream()
                    .findFirst()
                    .map(Producto::getIdMenu)
                    .orElse(null);
                    
            if (idLocal == null) {
                mostrarError("No se pudo determinar el local del pedido");
                return;
            }
                
            // Mostrar diálogo para instrucciones especiales
            TextInputDialog instruccionesDialog = new TextInputDialog();
            instruccionesDialog.setTitle("Instrucciones Especiales");
            instruccionesDialog.setHeaderText("¿Tienes alguna instrucción especial para tu pedido?");
            instruccionesDialog.setContentText("Instrucciones (opcional):");

            Optional<String> instruccionesResult = instruccionesDialog.showAndWait();
            if (!instruccionesResult.isPresent()) {
                return; // El usuario canceló
            }

            // Enviar el pedido al servidor
            carritoService.enviarPedido(idLocal, instruccionesResult.orElse(""));
            
            // Limpiar el carrito después de un pedido exitoso
            carritoService.limpiarCarrito();
            
            // Mostrar mensaje de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText("Pedido realizado");
            alert.setContentText("Tu pedido ha sido registrado correctamente.");
            alert.showAndWait();
            
            // Actualizar la vista
            cargarCarrito();
            
            // Notificar al dashboard para actualizar el contador
            if (onCartUpdate != null) {
                onCartUpdate.accept(null);
            }
            
            // Cerrar la ventana del carrito si es necesario
            if (stage != null) {
                stage.close();
            }
            
        } catch (java.net.ConnectException e) {
            mostrarError("No se pudo conectar al servidor. Por favor, verifica que el servidor esté en ejecución.");
        } catch (java.net.http.HttpTimeoutException e) {
            mostrarError("Tiempo de espera agotado. El servidor no respondió a tiempo.");
        } catch (java.net.UnknownHostException e) {
            mostrarError("No se pudo encontrar el servidor. Verifica tu conexión a internet.");
        } catch (IOException e) {
            mostrarError("Error al procesar el pedido: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado al procesar el pedido: " + e.getMessage());
        }
    }
    
    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
}
