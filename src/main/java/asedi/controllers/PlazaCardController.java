package asedi.controllers;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
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

        // Normalizar la URL de la imagen
        String normalizedUrl = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        
        System.out.println("Intentando cargar imagen: " + normalizedUrl);
        
        // 1. Intentar cargar como recurso interno (desde el JAR)
        try (InputStream is = getClass().getResourceAsStream(normalizedUrl)) {
            if (is != null) {
                Image image = new Image(is);
                image.errorProperty().addListener((obs, wasError, isNowError) -> {
                    if (isNowError) {
                        System.err.println("Error al cargar la imagen como recurso: " + normalizedUrl);
                        loadPlaceholderImage();
                    }
                });
                
                if (!image.isError()) {
                    System.out.println("Imagen cargada correctamente desde recursos: " + normalizedUrl);
                    imagenPlaza.setImage(image);
                    return;
                }
            } else {
                System.err.println("No se encontró el recurso: " + normalizedUrl);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen como recurso (" + normalizedUrl + "): " + e.getMessage());
        }
        
        // 2. Si falla, intentar cargar desde el sistema de archivos (para desarrollo)
        try {
            // Construir ruta relativa a la carpeta de recursos
            String filePath = "src/main/resources" + normalizedUrl;
            Image fileImage = new Image("file:" + filePath, true);
            
            fileImage.errorProperty().addListener((obs, wasError, isNowError) -> {
                if (isNowError) {
                    System.err.println("Error al cargar la imagen desde archivo: " + filePath);
                    loadPlaceholderImage();
                }
            });
            
            if (!fileImage.isError()) {
                System.out.println("Imagen cargada correctamente desde archivo: " + filePath);
                imagenPlaza.setImage(fileImage);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen desde archivo: " + e.getMessage());
        }
        
        // 3. Si todo falla, cargar placeholder
        System.err.println("No se pudo cargar la imagen, usando placeholder: " + normalizedUrl);
        loadPlaceholderImage();
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
