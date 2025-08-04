package asedi.controllers;

import asedi.model.Plaza;
import asedi.services.AuthService;
import asedi.services.PlazaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class UsuarioDashboardController {
    @FXML private Label userNameLabel;
    @FXML private StackPane contenidoPane;
    @FXML private VBox plazasContainer;

    private final PlazaService plazaService = new PlazaService();

    @FXML
    public void initialize() {
        // Mostrar el nombre del usuario actual
        AuthService authService = AuthService.getInstance();
        if (authService.getCurrentUser() != null) {
            userNameLabel.setText(authService.getCurrentUser().getNombre());
        }
        cargarPlazas();
    }

    private void cargarPlazas() {
        try {
            List<Plaza> plazas = plazaService.obtenerTodas();
            plazasContainer.getChildren().clear();
            for (Plaza plaza : plazas) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/usuarioPlazaCard.fxml"));
                Parent plazaCard = loader.load();
                UsuarioPlazaCardController controller = loader.getController();
                controller.setPlaza(plaza);
                controller.setUsuarioDashboardController(this);
                plazasContainer.getChildren().add(plazaCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarLocalesDePlaza(Plaza plaza) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/localesPorPlaza.fxml"));
        Parent root = loader.load();
        LocalesPorPlazaController controller = loader.getController();
        controller.setPlaza(plaza);
        contenidoPane.getChildren().setAll(root);
    }

    @FXML
    public void cargarInicio() {
        cargarPlazas();
    }

    @FXML
    public void cargarPerfil() {
        mostrarMensaje("Perfil de Usuario\n(En desarrollo)");
    }

    @FXML
    public void cargarReservas() {
        mostrarMensaje("Mis Reservas\n(En desarrollo)");
    }

    @FXML
    public void cargarCarrito() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/carrito.fxml"));
        Parent root = loader.load();
        contenidoPane.getChildren().setAll(root);
    }

    @FXML
    public void cerrarSesion() {
        try {
            // Cerrar sesión
            AuthService.getInstance().logout();
            
            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) contenidoPane.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Inicio de Sesión - FoodPlaza");
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(String mensaje) {
        contenidoPane.getChildren().clear();
        
        // Crear un mensaje simple
        javafx.scene.control.Label label = new javafx.scene.control.Label(mensaje);
        label.setStyle("-fx-font-size: 16px; -fx-alignment: center; -fx-text-alignment: center;");
        
        contenidoPane.getChildren().add(label);
    }
}