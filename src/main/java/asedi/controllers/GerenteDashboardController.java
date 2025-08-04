package asedi.controllers;

import asedi.model.Usuario;
import asedi.services.AuthService;
import asedi.controllers.gerencia.ProductoControllerFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.function.Function;

import java.io.IOException;

public class GerenteDashboardController {
    @FXML private Label userNameLabel;
    @FXML private StackPane contenidoPane;
    
    private ProductoControllerFactory productoControllerFactory;

    @FXML
    public void initialize() {
        try {
            // Obtener el servicio de autenticación
            AuthService authService = AuthService.getInstance();
            
            // Obtener el usuario actual
            Usuario usuario = authService.getCurrentUser();
            
            if (usuario != null) {
                // Mostrar el nombre del usuario
                String nombreCompleto = usuario.getNombre() != null ? usuario.getNombre() : "Usuario";
                userNameLabel.setText(nombreCompleto);
                
                // Obtener el local asignado del servicio de autenticación
                Usuario.Local local = authService.getLocalAsignado();
                
                // Si no hay local en el servicio, verificar si está en el usuario
                if (local == null && usuario.getLocal() != null) {
                    local = usuario.getLocal();
                    authService.setLocalAsignado(local);
                }
                
                // Verificar si el usuario tiene un local asignado
                if (local != null) {
                    System.out.println("GerenteDashboard - Local asignado: " + 
                                      local.getNombre() + " (ID: " + local.getId() + ")");
                } else {
                    System.err.println("ADVERTENCIA: El gerente no tiene un local asignado.");
                    // Mostrar un mensaje al usuario
                    mostrarError("Configuración requerida", 
                               "No tiene un local asignado. Por favor, contacte al administrador.");
                }
            } else {
                System.err.println("Error: No se pudo obtener la información del usuario actual");
                mostrarError("Error de sesión", "No se pudo cargar la información del usuario");
                cerrarSesion();
            }
        } catch (Exception e) {
            System.err.println("Error en la inicialización del dashboard del gerente: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "Ocurrió un error al cargar el panel de control");
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void cargarPanelPrincipal() {
        cargarVista("gerente/panelPrincipal");
    }

    @FXML
    public void cargarMenus() {
        cargarVista("gerencia/menus/listado_menus");
    }

    @FXML
    public void cargarProductos() {
        // Inicializar la fábrica de controladores si no está inicializada
        if (productoControllerFactory == null) {
            productoControllerFactory = new ProductoControllerFactory(menuId -> {
                // Este callback se ejecutará cuando se establezca el ID del menú
                System.out.println("Menú seleccionado: " + menuId);
                return null;
            });
        }
        
        // Cargar la vista con la fábrica de controladores
        cargarVista("gerencia/productos/listado_productos", loader -> {
            loader.setControllerFactory(productoControllerFactory);
            return null;
        });
    }

    @FXML
    public void cargarLocales() {
        cargarVista("gerente/locales");
    }

    @FXML
    public void cargarReportes() {
        cargarVista("gerente/reportes");
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

    private void cargarVista(String vista) {
        cargarVista(vista, null);
    }
    
    private void cargarVista(String vista, Function<FXMLLoader, Void> configuracionLoader) {
        try {
            // Limpiar el contenido actual
            contenidoPane.getChildren().clear();
            
            // Cargar la vista FXML
            String rutaFxml = "/views/" + vista + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            
            // Aplicar configuración personalizada al loader si se proporciona
            if (configuracionLoader != null) {
                configuracionLoader.apply(loader);
            }
            
            Parent vistaCargada = loader.load();
            
            // Agregar la vista al panel de contenido
            contenidoPane.getChildren().add(vistaCargada);
            
        } catch (IOException e) {
            // Si hay un error, mostrar mensaje de error
            System.err.println("Error al cargar la vista: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar mensaje de error en la interfaz
            Label errorLabel = new Label("Error al cargar la vista: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            contenidoPane.getChildren().add(errorLabel);
        } catch (Exception e) {
            // Manejar cualquier otra excepción
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            
            Label errorLabel = new Label("Error inesperado: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            contenidoPane.getChildren().add(errorLabel);
        }
    }
}
