package asedi.controllers;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import asedi.model.Local;

public class LocalCardController implements Initializable {
    @FXML private VBox cardContainer;
    @FXML private ImageView imagenLocal;
    @FXML private Label nombreLabel;
    @FXML private Label tipoComercioLabel;
    @FXML private Label horarioLabel;
    @FXML private Label descripcionLabel;
    
    private Local local;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar imagen por defecto
        loadPlaceholderImage();
    }
    
    private void loadPlaceholderImage() {
        try (InputStream is = getClass().getResourceAsStream("/images/locales/localDummy.png")) {
            if (is != null) {
                Image placeholder = new Image(is);
                imagenLocal.setImage(placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setLocal(Local local) {
        this.local = local;
        actualizarVista();
    }
    
    private void actualizarVista() {
        if (local != null) {
            // Actualizar texto
            nombreLabel.setText(local.getNombre());
            tipoComercioLabel.setText(local.getTipoComercio());
            horarioLabel.setText(String.format("%s - %s", 
                local.getHorarioApertura() != null ? local.getHorarioApertura() : "", 
                local.getHorarioCierre() != null ? local.getHorarioCierre() : ""
            ));
            
            // Establecer descripción si existe
            if (local.getDescripcion() != null && !local.getDescripcion().isEmpty()) {
                descripcionLabel.setText(local.getDescripcion());
                descripcionLabel.setVisible(true);
            } else {
                descripcionLabel.setVisible(false);
            }
            
            // Cargar imagen si existe
            if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
                try {
                    // Usar un hilo separado para cargar la imagen
                    new Thread(() -> {
                        try {
                            Image image = new Image(local.getImagenUrl(), true); // true para carga en segundo plano
                            image.progressProperty().addListener((_, _, newVal) -> {
                                if (newVal.doubleValue() == 1.0) {
                                    Platform.runLater(() -> imagenLocal.setImage(image));
                                }
                            });
                        } catch (Exception e) {
                            System.err.println("Error al cargar la imagen: " + e.getMessage());
                        }
                    }).start();
                } catch (Exception e) {
                    System.err.println("Error al cargar la imagen: " + e.getMessage());
                }
            }
            
            // Imagen por defecto
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/local-default.png"));
                imagenLocal.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen por defecto: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClick() {
        // TODO: Implementar navegación a la vista de detalles del local
        System.out.println("Mostrando detalles del local: " + local.getNombre());
    }
}
