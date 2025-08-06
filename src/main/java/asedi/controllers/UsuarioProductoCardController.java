package asedi.controllers;

import asedi.model.Producto;
import asedi.services.CarritoService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UsuarioProductoCardController {

    @FXML
    private VBox root;
    
    @FXML
    public void initialize() {
        // Cargar estilos CSS directamente en el controlador
        String css = """
            .producto-card {
                -fx-background-color: white;
                -fx-background-radius: 12px;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
                -fx-padding: 15px;
                -fx-spacing: 10px;
                -fx-max-width: 250px;
                -fx-min-width: 200px;
            }
            .producto-card:hover {
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4);
            }
            .imagen-producto {
                -fx-background-radius: 8px;
            }
            .nombre-label {
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #2c3e50;
            }
            .descripcion-label {
                -fx-font-size: 12px;
                -fx-text-fill: #7f8c8d;
                -fx-wrap-text: true;
            }
            .precio-label {
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #e74c3c;
            }
            .boton-agregar {
                -fx-background-color: #2ecc71;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 8px 16px;
                -fx-background-radius: 20px;
                -fx-cursor: hand;
            }
            .boton-agregar:hover {
                -fx-background-color: #27ae60;
            }""";
        
        // Aplicar estilos directamente al root
        root.getStylesheets().add("data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(css.getBytes()));
        
        // Aplicar clases de estilo a los elementos
        if (nombreLabel != null) nombreLabel.getStyleClass().add("nombre-label");
        if (descripcionLabel != null) descripcionLabel.getStyleClass().add("descripcion-label");
        if (precioLabel != null) precioLabel.getStyleClass().add("precio-label");
        if (agregarButton != null) agregarButton.getStyleClass().add("boton-agregar");
        if (imagenProducto != null) imagenProducto.getStyleClass().add("imagen-producto");
    }
    
    @FXML
    private ImageView imagenProducto;

    @FXML
    private Label nombreLabel;

    @FXML
    private Label descripcionLabel;

    @FXML
    private Label precioLabel;
    
    @FXML
    private Button agregarButton;
    
    @FXML
    private StackPane cardContainer;

    private Producto producto;
    private boolean isInCart = false;

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
        if (!isInCart) {
            carritoService.agregarProducto(producto);
            isInCart = true;
            
            // Cambiar el texto y estilo del botón
            agregarButton.setText("✓ Agregado");
            agregarButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            
            // Efecto de confirmación
            Label confirmacion = new Label("¡Agregado al carrito!");
            confirmacion.setStyle(
                "-fx-background-color: rgba(40, 167, 69, 0.9); " +
                "-fx-text-fill: white; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 5;"
            );
            
            // Posicionar el mensaje sobre la tarjeta
            StackPane overlay = new StackPane(confirmacion);
            overlay.setStyle("-fx-background-color: transparent;");
            
            // Agregar el overlay a la tarjeta
            if (cardContainer != null) {
                cardContainer.getChildren().add(overlay);
                
                // Configurar animación de desvanecimiento
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), overlay);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> cardContainer.getChildren().remove(overlay));
                
                // Iniciar la animación después de un breve retraso
                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> fadeOut.play())
                );
                timeline.play();
                
                // Restablecer el botón después de 2 segundos
                Timeline resetButton = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        agregarButton.setText("Agregar al Carrito");
                        agregarButton.setStyle("");
                        isInCart = false;
                    })
                );
                resetButton.play();
            }
        }
    }
}