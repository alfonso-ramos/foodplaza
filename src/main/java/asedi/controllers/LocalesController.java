package asedi.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LocalesController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void initialize() {
        welcomeText.setText("Módulo de Locales (En desarrollo)");
    }
}
