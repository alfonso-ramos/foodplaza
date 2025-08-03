package asedi.controllers;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class PlazaCardController implements Initializable {
    @FXML private VBox cardContainer;
    @FXML private ImageView imagenPlaza;
    @FXML private Label nombreLabel;
    @FXML private Label ubicacionLabel;
    @FXML private Label descripcionLabel;
    
    private int plazaId;
    private PlazasController plazasController;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set default placeholder image
        loadPlaceholderImage();
        
        // Add hover effect - now handled by CSS
        // The cursor is now managed by the CSS class 'plaza-card'
        // which has cursor: pointer (standard) and -fx-cursor: hand (JavaFX)
    }
    
    private void loadPlaceholderImage() {
        try (InputStream is = getClass().getResourceAsStream("/images/placeholder.svg")) {
            if (is != null) {
                Image placeholder = new Image(is);
                imagenPlaza.setImage(placeholder);
            } else {
                System.err.println("No se pudo encontrar la imagen de placeholder");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen de placeholder: " + e.getMessage());
        }
    }
    
    public void setPlazaData(int id, String nombre, String ubicacion, String descripcion, String imagenUrl) {
        if (nombre == null || ubicacion == null || descripcion == null) {
            throw new IllegalArgumentException("Los datos de la plaza no pueden ser nulos");
        }
        
        this.plazaId = id;
        
        // Set labels with validation
        nombreLabel.setText(nombre);
        ubicacionLabel.setText(ubicacion);
        descripcionLabel.setText(descripcion);
        
        // Load image if URL is provided
        if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
            loadImage(imagenUrl);
        }
    }
    
    private void loadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            loadPlaceholderImage();
            return;
        }

        // Configurar el ImageView para que la imagen se ajuste correctamente
        imagenPlaza.setPreserveRatio(true);  // Mantener relación de aspecto
        imagenPlaza.setSmooth(true);
        imagenPlaza.setCache(true);
        // Tamaño máximo para la imagen
        imagenPlaza.setFitWidth(280);  // Reducir un poco el ancho para dar espacio al padding
        imagenPlaza.setFitHeight(180); // Reducir un poco el alto para dar espacio al padding
        
        // Asegurar que la imagen no se salga de su contenedor
        imagenPlaza.setStyle(
            "-fx-background-color: #f5f5f5;" +  // Fondo por si la imagen tiene transparencia
            "-fx-padding: 0;" +                // Sin padding adicional
            "-fx-border-radius: 4px;" +        // Bordes redondeados
            "-fx-background-radius: 4px;"       // Bordes redondeados para el fondo
        );
        
        // Verificar si es una URL remota (http/https)
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            loadRemoteImage(imageUrl);
            return;
        }

        // Si no es una URL remota, tratar como recurso local
        String normalizedUrl = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        
        // 1. Intentar cargar como recurso interno (desde el JAR)
        try (InputStream is = getClass().getResourceAsStream(normalizedUrl)) {
            if (is != null) {
                // Para recursos internos, cargamos primero sin escalar
                Image image = new Image(is);
                setupImageErrorHandling(image, "recurso: " + normalizedUrl);
                if (!image.isError()) {
                    imagenPlaza.setImage(image);
                    return;
                }
            } else {
                System.err.println("No se encontró el recurso local: " + normalizedUrl);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen como recurso (" + normalizedUrl + "): " + e.getMessage());
        }
        
        // 2. Si falla, intentar cargar desde el sistema de archivos (para desarrollo)
        try {
            String filePath = "src/main/resources" + normalizedUrl;
            Image fileImage = new Image("file:" + filePath, false);
            setupImageErrorHandling(fileImage, "archivo: " + filePath);
            if (!fileImage.isError()) {
                imagenPlaza.setImage(fileImage);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen desde archivo: " + e.getMessage());
        }
        
        // 3. Si todo falla, cargar placeholder
        System.err.println("No se pudo cargar la imagen, usando placeholder: " + imageUrl);
        loadPlaceholderImage();
    }
    
    private void loadRemoteImage(String imageUrl) {
        try {
            System.out.println("Cargando imagen remota: " + imageUrl);
            // Cargar la imagen con carga en segundo plano
            Image remoteImage = new Image(imageUrl, 280, 180, true, true, true);
            
            // Configurar el manejador de errores
            remoteImage.errorProperty().addListener((_obs, _wasError, isNowError) -> {
                if (isNowError) {
                    System.err.println("Error al cargar la imagen remota: " + imageUrl);
                    Platform.runLater(this::loadPlaceholderImage);
                } else {
                    System.out.println("Imagen remota cargada correctamente: " + imageUrl);
                }
            });
            
            // Si la imagen se cargó correctamente, mostrarla
            if (!remoteImage.isError()) {
                Platform.runLater(() -> {
                    imagenPlaza.setImage(remoteImage);
                    // Asegurar que la imagen mantenga su relación de aspecto
                    imagenPlaza.setPreserveRatio(true);
                });
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen remota (" + imageUrl + "): " + e.getMessage());
            Platform.runLater(this::loadPlaceholderImage);
        }
    }
    
    private void setupImageErrorHandling(Image image, String source) {
        image.errorProperty().addListener((_obs, _wasError, isNowError) -> {
            if (isNowError) {
                System.err.println("Error al cargar la imagen " + source);
                loadPlaceholderImage();
            } else {
                System.out.println("Imagen cargada correctamente desde " + source);
            }
        });
    }
    
    public void setPlazasController(PlazasController plazasController) {
        this.plazasController = plazasController;
    }
    
    @FXML
    private void onModificar() {
        if (plazasController != null) {
            try {
                plazasController.modificarPlaza(plazaId);
            } catch (Exception e) {
                showError("Error al modificar la plaza", e.getMessage());
            }
        }
    }
    
    @FXML
    private void onEliminar() {
        if (plazasController != null) {
            try {
                plazasController.eliminarPlaza(plazaId);
            } catch (Exception e) {
                showError("Error al eliminar la plaza", e.getMessage());
            }
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
