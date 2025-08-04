package asedi.controllers.gerencia;

import asedi.model.Menu;
import asedi.model.Producto;
import asedi.services.MenuService;
import asedi.services.ProductoService;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import java.net.URL;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MenuProductosController implements Initializable {

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private CheckBox chkDisponible;
    @FXML private Button btnQuitar;
    
    private Menu menu;
    private MenuController menuController;
    private final MenuService menuService = new MenuService();
    private final ProductoService productoService = new ProductoService();
    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarEventos();
    }
    
    private void configurarTabla() {
        // Configurar columnas usando PropertyValueFactory
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        // Configurar columna de precio con formateo personalizado
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(column -> new TableCell<Producto, Double>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", precio));
                }
            }
        });
        
        tblProductos.setItems(productos);
    }
    
    private void configurarEventos() {
        // Habilitar/deshabilitar botón de quitar según selección
        tblProductos.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            btnQuitar.setDisable(newVal == null);
        });
    }
    
    public void setMenu(Menu menu) {
        this.menu = menu;
        if (menu != null) {
            cargarDatos();
            chkDisponible.setSelected(menu.isDisponible());
        }
    }
    
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }
    
    private void cargarDatos() {
        if (menu != null) {
            // Cargar los productos del menú
            productos.setAll(menu.getProductos());
            tblProductos.setItems(productos);
            
            // Configurar el estado del checkbox de disponibilidad
            chkDisponible.setSelected(menu.isDisponible());
            
            // Actualizar el estado del botón de quitar producto
            btnQuitar.setDisable(!menu.tieneProductos());
        }
    }
    
    @FXML
    private void agregarProducto() {
        try {
            // Obtener productos que aún no están en el menú
            List<Producto> productosDisponibles = productoService.obtenerTodos()
                .stream()
                .filter(p -> !menu.contieneProducto(p.getId()))
                .collect(Collectors.toList());
                
            if (productosDisponibles.isEmpty()) {
                mostrarMensaje("Información", "No hay productos disponibles para agregar al menú.");
                return;
            }
                
            // Crear diálogo de selección
            ChoiceDialog<Producto> dialog = new ChoiceDialog<>(null, productosDisponibles);
            dialog.setTitle("Agregar Producto");
            dialog.setHeaderText("Seleccione un producto para agregar al menú");
            dialog.setContentText("Producto:");
            
            // Mostrar diálogo y procesar resultado
            Optional<Producto> resultado = dialog.showAndWait();
            resultado.ifPresent(producto -> {
                try {
                    // Agregar el producto al menú en el servidor
                    if (menuService.agregarProductoAMenu(menu.getId(), producto.getId())) {
                        // Actualizar el modelo
                        menu.agregarProducto(producto);
                        
                        // Actualizar la tabla
                        tblProductos.refresh();
                        
                        mostrarMensaje("Éxito", "Producto agregado al menú correctamente.");
                    } else {
                        mostrarError("Error", "No se pudo agregar el producto al menú.");
                    }
                } catch (IOException e) {
                    mostrarError("Error", "No se pudo agregar el producto al menú: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los productos disponibles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void quitarProducto() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try {
                if (menuService.eliminarProductoDeMenu(menu.getId(), seleccionado.getId())) {
                    // Actualizar el modelo
                    menu.quitarProducto(seleccionado);
                    
                    // Actualizar la tabla
                    tblProductos.refresh();
                    
                    mostrarMensaje("Éxito", "Producto eliminado del menú correctamente.");
                } else {
                    mostrarError("Error", "No se pudo eliminar el producto del menú.");
                }
            } catch (IOException e) {
                mostrarError("Error", "No se pudo eliminar el producto del menú: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            mostrarMensaje("Información", "Por favor, seleccione un producto para eliminar.");
        }
    }
    
    @FXML
    private void toggleDisponibilidad() {
        if (menu != null) {
            boolean nuevoEstado = chkDisponible.isSelected();
            actualizarDisponibilidad(nuevoEstado);
        }
    }
    
    /**
     * Actualiza la disponibilidad del menú en el servidor
     * @param disponible nuevo estado de disponibilidad
     */
    private void actualizarDisponibilidad(boolean disponible) {
        if (menu != null) {
            try {
                // Actualizar el estado en el servidor
                if (menuService.actualizarDisponibilidad(menu.getId(), disponible)) {
                    // Actualizar el modelo local
                    menu.setDisponible(disponible);
                    
                    // Mostrar mensaje de éxito
                    String mensaje = disponible ? "El menú ha sido marcado como disponible" : "El menú ha sido marcado como no disponible";
                    mostrarMensaje("Éxito", mensaje);
                    
                    // Notificar al controlador principal para actualizar la vista
                    if (menuController != null) {
                        menuController.actualizarTablaMenus();
                    }
                } else {
                    // Revertir el cambio en el checkbox si falla la actualización
                    chkDisponible.setSelected(!disponible);
                    mostrarError("Error", "No se pudo actualizar la disponibilidad del menú");
                }
            } catch (IOException e) {
                // Revertir el cambio en el checkbox si hay un error
                chkDisponible.setSelected(!disponible);
                mostrarError("Error", "Error al actualizar la disponibilidad: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void guardarCambios() {
        if (menu != null) {
            try {
                // Actualizar disponibilidad si cambió
                if (menu.isDisponible() != chkDisponible.isSelected()) {
                    actualizarDisponibilidad(chkDisponible.isSelected());
                } else {
                    mostrarMensaje("Información", "No hay cambios que guardar");
                }
            } catch (Exception e) {
                mostrarError("Error al guardar", "No se pudieron guardar los cambios: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void cerrar() {
        Stage stage = (Stage) tblProductos.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarMensaje(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    /**
     * Actualiza la lista de productos en la tabla
     */
    private void actualizarListaProductos() {
        if (menu != null) {
            try {
                // Actualizar la lista de productos desde el servidor
                Menu menuActualizado = menuService.obtenerPorId(menu.getId());
                if (menuActualizado != null) {
                    productos.setAll(menuActualizado.getProductos());
                    tblProductos.refresh();
                    
                    // Actualizar el estado del botón de quitar producto
                    btnQuitar.setDisable(!menu.tieneProductos());
                }
            } catch (IOException e) {
                mostrarError("Error", "No se pudieron actualizar los productos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
}
