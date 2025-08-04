package asedi.controllers;

import asedi.model.Local;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class UsuarioLocalCardController {

    @FXML
    private ImageView imagenLocal;

    @FXML
    private Label nombreLabel;

    @FXML
    private Label descripcionLabel;

    private Local local;
    private UsuarioDashboardController usuarioDashboardController;


    public void setLocal(Local local) {
        this.local = local;
        nombreLabel.setText(local.getNombre());
        descripcionLabel.setText(local.getDescripcion() != null ? local.getDescripcion() : "Sin descripción");
        
        try {
            if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
                // Intentar cargar la imagen desde la URL proporcionada
                Image image = new Image(local.getImagenUrl(), true);
                // Configurar un manejador para errores de carga de imagen
                image.errorProperty().addListener((ignored1, ignored2, isNowError) -> {
                    if (isNowError) {
                        // Si hay un error al cargar la imagen, usar la imagen por defecto
                        cargarImagenPorDefecto();
                    }
                });
                imagenLocal.setImage(image);
            } else {
                // Si no hay URL de imagen, usar la imagen por defecto
                cargarImagenPorDefecto();
            }
        } catch (Exception e) {
            // En caso de cualquier error, usar la imagen por defecto
            cargarImagenPorDefecto();
        }
    }

    public void setUsuarioDashboardController(UsuarioDashboardController usuarioDashboardController) {
        this.usuarioDashboardController = usuarioDashboardController;
    }

    private void cargarImagenPorDefecto() {
        try {
            // Cargar la imagen por defecto desde los recursos
            Image defaultImage = new Image(
                getClass().getResourceAsStream("/images/icons/store.png"),
                250, 150, true, true
            );
            imagenLocal.setImage(defaultImage);
        } catch (Exception e) {
            // Si no se puede cargar la imagen por defecto, establecer un color de fondo
            imagenLocal.setImage(null);
            imagenLocal.setStyle("-fx-background-color: #e0e0e0;");
        }
    }
    
    @FXML
    void verProductos() {
        if (usuarioDashboardController != null && local != null) {
            try {
                usuarioDashboardController.mostrarProductosDeLocal(local);
            } catch (IOException e) {
                e.printStackTrace();
                // Mostrar mensaje de error al usuario
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No se pudo cargar los productos");
                alert.setContentText("Ha ocurrido un error al intentar cargar los productos del local. Por favor, intente nuevamente.");
                alert.showAndWait();
            }
        }
    }
}
