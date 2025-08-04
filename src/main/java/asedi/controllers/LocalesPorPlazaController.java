package asedi.controllers;

import asedi.model.Local;
import asedi.model.Plaza;
import asedi.services.LocalService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class LocalesPorPlazaController {

    @FXML
    private Label plazaNombreLabel;

    @FXML
    private VBox localesContainer;

    private Plaza plaza;

    private final LocalService localService = new LocalService();

    public void setPlaza(Plaza plaza) {
        this.plaza = plaza;
        plazaNombreLabel.setText("Locales en " + plaza.getNombre());
        cargarLocales();
    }

    private void cargarLocales() {
        try {
            List<Local> locales = localService.obtenerLocalesPorPlaza(plaza.getId());
            localesContainer.getChildren().clear();
            for (Local local : locales) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/usuarioLocalCard.fxml"));
                Parent localCard = loader.load();
                UsuarioLocalCardController controller = loader.getController();
                controller.setLocal(local);
                localesContainer.getChildren().add(localCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
