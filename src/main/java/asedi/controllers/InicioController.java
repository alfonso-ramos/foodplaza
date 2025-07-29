package asedi.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InicioController {

    @FXML
    private Label saludoLabel;

    @FXML
    public void initialize() {
        saludoLabel.setText("¡Hola desde JavaFX 21!");
    }
}
