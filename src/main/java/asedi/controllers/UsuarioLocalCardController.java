package asedi.controllers;

import asedi.model.Local;
import javafx.fxml.FXML;
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

    public void setLocal(Local local) {
        this.local = local;
        nombreLabel.setText(local.getNombre());
        descripcionLabel.setText(local.getDescripcion());
        if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
            imagenLocal.setImage(new Image(local.getImagenUrl()));
        }
    }

    @FXML
    void verProductos() throws IOException {
        // TODO: Implementar la navegación a la vista de productos
    }
}
