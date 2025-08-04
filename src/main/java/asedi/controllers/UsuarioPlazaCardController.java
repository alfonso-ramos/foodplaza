package asedi.controllers;

import asedi.model.Plaza;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class UsuarioPlazaCardController {

    @FXML
    private VBox cardContainer;

    @FXML
    private ImageView imagenPlaza;

    @FXML
    private Label nombreLabel;

    @FXML
    private Label ubicacionLabel;

    @FXML
    private Label descripcionLabel;

    private Plaza plaza;

    private UsuarioDashboardController usuarioDashboardController;

    public void setPlaza(Plaza plaza) {
        this.plaza = plaza;
        nombreLabel.setText(plaza.getNombre());
        ubicacionLabel.setText(plaza.getDireccion());
        descripcionLabel.setText(plaza.getDireccion());
        if (plaza.getImagenUrl() != null && !plaza.getImagenUrl().isEmpty()) {
            imagenPlaza.setImage(new Image(plaza.getImagenUrl()));
        }
    }

    public void setUsuarioDashboardController(UsuarioDashboardController usuarioDashboardController) {
        this.usuarioDashboardController = usuarioDashboardController;
    }

    @FXML
    void onPlazaClicked(MouseEvent event) throws IOException {
        usuarioDashboardController.mostrarLocalesDePlaza(plaza);
    }
}
