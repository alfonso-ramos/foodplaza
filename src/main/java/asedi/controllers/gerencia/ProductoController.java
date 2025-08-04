package asedi.controllers.gerencia;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import asedi.controllers.gerencia.components.ProductoCardController;
import asedi.model.Producto;
import asedi.services.ProductoService;
import asedi.utils.AlertUtils;

public class ProductoController implements Initializable {

    // Componentes de la interfaz
    @FXML
    private GridPane gridProductos;
    @FXML private VBox sinProductosPane;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroDisponibilidad;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Pane loadingPane;
    @FXML private Label lblSinResultados;
    
    // Inyectar dependencias
    private final ProductoService productoService;
    
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    
    public ProductoController() {
        this.productoService = new ProductoService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Configurar el grid
            gridProductos.setHgap(20);
            gridProductos.setVgap(20);

            // Configurar los listeners para los controles de búsqueda y filtros
            txtBuscar.textProperty().addListener((_, _, _) -> actualizarVistaProductos());
            cmbFiltroCategoria.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> 
                actualizarVistaProductos()
            );
            cmbFiltroDisponibilidad.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> 
                actualizarVistaProductos()
            );

            // Configurar las opciones de los ComboBox
            cmbFiltroCategoria.getItems().addAll("Todas las categorías", "Entradas", "Platos principales", "Postres", "Bebidas");
            cmbFiltroCategoria.getSelectionModel().select(0);
            cmbFiltroDisponibilidad.getItems().addAll("Todas", "Disponibles", "No disponibles");
            cmbFiltroDisponibilidad.getSelectionModel().select(0);

            // Cargar datos iniciales
            cargarProductos();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "Error al inicializar la vista de productos: " + e.getMessage());
        }
    }

    @FXML
    private void buscarProductos() {
        actualizarVistaProductos();
    }

    private Predicate<Producto> crearPredicadoBusqueda(String busqueda, String categoria, String disponibilidad) {
        return producto -> {
            if (producto == null) return false;
            
            // Filtrar por búsqueda
            boolean coincideBusqueda = busqueda == null || busqueda.isEmpty() ||
                (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(busqueda.toLowerCase())) ||
                (producto.getDescripcion() != null && 
                 producto.getDescripcion().toLowerCase().contains(busqueda.toLowerCase()));

            // Filtrar por categoría
            boolean coincideCategoria = categoria == null || 
                categoria.isEmpty() ||
                categoria.equals("Todas las categorías") ||
                (producto.getCategoria() != null && 
                 producto.getCategoria().equalsIgnoreCase(categoria));

            // Filtrar por disponibilidad
            boolean coincideDisponibilidad = true;
            if (disponibilidad != null && !disponibilidad.isEmpty()) {
                if (disponibilidad.equals("Disponibles")) {
                    coincideDisponibilidad = producto.getDisponible() != null && producto.getDisponible();
                } else if (disponibilidad.equals("No disponibles")) {
                    coincideDisponibilidad = producto.getDisponible() == null || !producto.getDisponible();
                }
            }

            return coincideBusqueda && coincideCategoria && coincideDisponibilidad;
        };
    }

    @FXML
    private void handleAgregarProducto() {
        try {
            // Cargar el formulario de producto
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gerencia/productos/ProductoForm.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador del formulario
            ProductoFormController formController = loader.getController();
            formController.setProducto(new Producto());
            
            // Mostrar el formulario en un nuevo diálogo
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Agregar Producto");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
            // Recargar productos después de cerrar el diálogo
            cargarProductos();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "No se pudo abrir el formulario de producto: " + e.getMessage());
        }
    }

    private void confirmarEliminacion(Producto producto) {
        if (producto == null) return;
        
        // Mostrar diálogo de confirmación
        boolean confirmar = AlertUtils.mostrarConfirmacion(
            "Confirmar eliminación",
            "¿Está seguro de que desea eliminar el producto '" + producto.getNombre() + "'?\n" +
            "Esta acción no se puede deshacer."
        );
        
        if (confirmar) {
            mostrarCargando(true);
            
            Task<Boolean> tareaEliminar = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return productoService.eliminar(producto.getId());
                }
            };
            
            tareaEliminar.setOnSucceeded(_ -> {
                mostrarCargando(false);
                if (tareaEliminar.getValue()) {
                    // Eliminación exitosa, actualizar la vista
                    productos.remove(producto);
                    actualizarVistaProductos();
                    AlertUtils.mostrarInformacion("Éxito", "Producto eliminado correctamente");
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo eliminar el producto");
                }
            });
            
            tareaEliminar.setOnFailed(_ -> {
                mostrarCargando(false);
                AlertUtils.mostrarError("Error", "Error al intentar eliminar el producto: " + 
                    (tareaEliminar.getException() != null ? tareaEliminar.getException().getMessage() : "Error desconocido"));
            });
            
            new Thread(tareaEliminar).start();
        }
    }

    private void editarProducto(Producto producto) {
        if (producto == null) return;
        
        try {
            // Cargar el formulario de producto
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gerencia/productos/ProductoForm.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador del formulario con el producto a editar
            ProductoFormController formController = loader.getController();
            formController.setProducto(producto);
            
            // Mostrar el formulario en un nuevo diálogo
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Editar Producto: " + producto.getNombre());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
            // Actualizar la vista después de editar
            actualizarVistaProductos();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "No se pudo abrir el formulario de edición: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        mostrarCargando(true);

        Task<List<Producto>> tarea = new Task<>() {
            @Override
            protected List<Producto> call() throws Exception {
                return productoService.obtenerTodos();
            }
        };

        tarea.setOnSucceeded(_ -> {
            productos.setAll(tarea.getValue());
            actualizarVistaProductos();
            mostrarCargando(false);
        });

        tarea.setOnFailed(_ -> {
            mostrarCargando(false);
            AlertUtils.mostrarError("Error", "No se pudieron cargar los productos: " +
                (tarea.getException() != null ? tarea.getException().getMessage() : "Error desconocido"));
        });

        new Thread(tarea).start();
    }

    private void mostrarCargando(boolean mostrar) {
        loadingPane.setVisible(mostrar);
        gridProductos.setVisible(!mostrar);
    }

    private void actualizarVistaProductos() {
        try {
            gridProductos.getChildren().clear();
            
            if (productos == null || productos.isEmpty()) {
                lblSinResultados.setVisible(true);
                return;
            }
            
            // Aplicar filtros
            String busqueda = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase() : "";
            String categoria = cmbFiltroCategoria.getSelectionModel().getSelectedItem();
            String disponibilidad = cmbFiltroDisponibilidad.getSelectionModel().getSelectedItem();
            
            List<Producto> productosFiltrados = productos.stream()
                .filter(crearPredicadoBusqueda(busqueda, categoria, disponibilidad))
                .collect(Collectors.toList());
                
            if (productosFiltrados.isEmpty()) {
                lblSinResultados.setVisible(true);
                return;
            }
            
            lblSinResultados.setVisible(false);
            
            // Mostrar productos en el grid
            int column = 0;
            int row = 0;
            final int MAX_COLUMNS = 4;
            
            for (Producto producto : productosFiltrados) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/views/gerencia/productos/components/producto_card.fxml"));
                    VBox card = loader.load();
                    
                    ProductoCardController controller = loader.getController();
                    controller.setProducto(producto);
                    controller.setOnEditarAction(this::editarProducto);
                    controller.setOnEliminarAction(this::confirmarEliminacion);
                    
                    gridProductos.add(card, column, row);
                    GridPane.setMargin(card, new javafx.geometry.Insets(10));
                    
                    column++;
                    if (column >= MAX_COLUMNS) {
                        column = 0;
                        row++;
                    }
                } catch (IOException e) {
                    System.err.println("Error al cargar tarjeta de producto: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error", "Error al actualizar la vista de productos: " + e.getMessage());
        }
    }
}
