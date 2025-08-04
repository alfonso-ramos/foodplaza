package asedi.controllers;

import asedi.model.Local;
import asedi.model.Plaza;
import asedi.services.AuthService;
import asedi.services.PlazaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
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

    @FXML
    public void cargarPlazas() {
        try {
            // Limpiar el contenido actual
            contenidoPane.getChildren().clear();
            
            // Crear un contenedor para las plazas
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));
            
            // Agregar un título
            Label titulo = new Label("Selecciona una Plaza");
            titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");
            mainContainer.getChildren().add(titulo);
            
            // Obtener las plazas
            List<Plaza> plazas = plazaService.obtenerTodas();
            
            if (plazas.isEmpty()) {
                Label mensaje = new Label("No hay plazas disponibles en este momento.");
                mensaje.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20px;");
                mainContainer.getChildren().add(mensaje);
            } else {
                // Crear un GridPane para organizar las tarjetas en un grid
                GridPane grid = new GridPane();
                grid.setHgap(20);
                grid.setVgap(20);
                grid.setPadding(new Insets(10));
                
                int column = 0;
                int row = 0;
                final int MAX_COLUMNS = 3;
                
                for (Plaza plaza : plazas) {
                    try {
                        // Cargar la tarjeta de plaza
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/usuarioPlazaCard.fxml"));
                        VBox card = loader.load();
                        
                        // Configurar el controlador de la tarjeta
                        UsuarioPlazaCardController cardController = loader.getController();
                        cardController.setPlaza(plaza);
                        cardController.setUsuarioDashboardController(this);
                        
                        // Agregar la tarjeta al grid
                        grid.add(card, column, row);
                        
                        // Actualizar contadores de fila/columna
                        column++;
                        if (column >= MAX_COLUMNS) {
                            column = 0;
                            row++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                // Asegurarse de que el grid ocupe todo el ancho disponible
                GridPane.setHgrow(grid, Priority.ALWAYS);
                mainContainer.getChildren().add(grid);
            }
            
            // Agregar el contenedor principal al contenido
            contenidoPane.getChildren().add(mainContainer);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar las plazas: " + e.getMessage());
        }
    }

    public void mostrarLocalesDePlaza(Plaza plaza) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/localesPorPlaza.fxml"));
        Parent root = loader.load();
        LocalesPorPlazaController controller = loader.getController();
        controller.setPlaza(plaza);
        controller.setUsuarioDashboardController(this);
        contenidoPane.getChildren().setAll(root);
    }

    public void mostrarProductosDeLocal(Local local) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/productosPorLocal.fxml"));
        Parent root = loader.load();
        ProductosPorLocalController controller = loader.getController();
        controller.setLocal(local);
        contenidoPane.getChildren().setAll(root);
    }

    @FXML
    public void cargarInicio() {
        cargarPlazas();
    }


    
    @FXML
    private void cargarOrdenes() {
        // Implementación para cargar las órdenes del usuario
        System.out.println("Cargando órdenes...");
        // TODO: Implementar la carga de órdenes
        mostrarMensaje("Próximamente: Visualización de órdenes");
    }
    
    @FXML
    private void cargarPerfil() {
        // Implementación para cargar el perfil del usuario
        System.out.println("Cargando perfil...");
        mostrarMensaje("Próximamente: Gestión de perfil");
    }
    
    @FXML
    private void cargarReservas() {
        // Implementación para cargar las reservas
        System.out.println("Cargando reservas...");
        mostrarMensaje("Próximamente: Gestión de reservas");
    }

    @FXML
    private void cargarCarrito() {
        // Implementación para cargar el carrito
        System.out.println("Cargando carrito...");
        mostrarMensaje("Próximamente: Carrito de compras");
    }

    @FXML
    public void cerrarSesion() {
        try {
            // Cerrar sesión
            AuthService.getInstance().logout();
            
            // Cargar la vista de login
            Stage stage = (Stage) contenidoPane.getScene().getWindow();
            Parent loginView = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            stage.getScene().setRoot(loginView);
            stage.setTitle("Inicio de Sesión - FoodPlaza");
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cerrar sesión");
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