package asedi.controllers;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import asedi.model.Local;

public class LocalCardController {
    @FXML private ImageView imagenLocal;
    @FXML private Label nombreLabel;
    @FXML private Label tipoComercioLabel;
    @FXML private Label horarioLabel;
    
    private Local local;
    
    @FXML
    public void initialize() {
        // Configuración inicial si es necesaria
    }
    
    public void setLocal(Local local) {
        this.local = local;
        actualizarVista();
    }
    
    private void actualizarVista() {
        if (local != null) {
            nombreLabel.setText(local.getNombre());
            tipoComercioLabel.setText(local.getTipoComercio());
            horarioLabel.setText(String.format("%s - %s", 
                local.getHorarioApertura(), 
                local.getHorarioCierre()
            ));
            
            // Cargar imagen si existe
            if (local.getImagenes() != null && !local.getImagenes().isEmpty()) {
                try {
                    File file = new File(local.getImagenes().get(0));
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        imagenLocal.setImage(image);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
