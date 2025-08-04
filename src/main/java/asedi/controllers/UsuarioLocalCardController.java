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
    private UsuarioDashboardController usuarioDashboardController;


    public void setLocal(Local local) {
        this.local = local;
        nombreLabel.setText(local.getNombre());
        descripcionLabel.setText(local.getDescripcion());
        if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
            imagenLocal.setImage(new Image(local.getImagenUrl()));
        }
    }

    public void setUsuarioDashboardController(UsuarioDashboardController usuarioDashboardController) {
        this.usuarioDashboardController = usuarioDashboardController;
    }

    @FXML
    void verProductos() throws IOException {
        if (usuarioDashboardController != null) {
            usuarioDashboardController.mostrarProductosDeLocal(local);
        }
    }
}
