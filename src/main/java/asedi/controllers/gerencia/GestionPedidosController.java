package asedi.controllers.gerencia;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import asedi.model.Pedido;
import asedi.services.PedidoService;
import asedi.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

public class GestionPedidosController {
    @FXML private TableView<Pedido> tablaPedidos;
    @FXML private TableColumn<Pedido, Long> colId;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private TableColumn<Pedido, Double> colTotal;
    @FXML private ComboBox<String> comboFiltro;
    @FXML private VBox detallesPedido;
    @FXML private Label lblDetalleId;
    @FXML private Label lblDetalleCliente;
    @FXML private Label lblDetalleEstado;
    @FXML private Label lblDetalleTotal;
    @FXML private TextArea txtDetalleProductos;
    
    private final PedidoService pedidoService = PedidoService.getInstance();
    private final ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private Long localId; // ID del local actual

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarFiltros();
        // La carga de pedidos se hará cuando se establezca el localId
    }
    
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(cellData -> 
            cellData.getValue().getCliente() != null ? 
            new SimpleStringProperty(cellData.getValue().getCliente().getNombre()) : 
            new SimpleStringProperty(""));
        colEstado.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((_obs, _oldVal, newVal) -> {
            if (newVal != null) {
                mostrarDetallesPedido(newVal);
            }
        });
    }
    
    private void cargarPedidos() {
        try {
            List<Pedido> listaPedidos = pedidoService.obtenerPedidosPorLocal(localId);
            pedidos.setAll(listaPedidos);
            tablaPedidos.setItems(pedidos);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            AlertUtils.mostrarError("Error", "No se pudieron cargar los pedidos: " + e.getMessage());
        }
    }
    
    private void configurarFiltros() {
        comboFiltro.getItems().addAll("TODOS", "PENDIENTE", "EN PROCESO", "TERMINADO", "ENTREGADO");
        comboFiltro.setValue("TODOS");
        
        comboFiltro.getSelectionModel().selectedItemProperty().addListener((_obs, _oldVal, newVal) -> {
            if (newVal.equals("TODOS")) {
                tablaPedidos.setItems(pedidos);
            } else {
                List<Pedido> filtrados = pedidos.stream()
                    .filter(p -> p.getEstado().equals(newVal))
                    .collect(Collectors.toList());
                tablaPedidos.setItems(FXCollections.observableArrayList(filtrados));
            }
        });
    }
    
    private void mostrarDetallesPedido(Pedido pedido) {
        lblDetalleId.setText(String.valueOf(pedido.getId()));
        lblDetalleCliente.setText(pedido.getCliente() != null ? pedido.getCliente().getNombre() : "");
        lblDetalleEstado.setText(pedido.getEstado());
        lblDetalleTotal.setText(String.format("$%.2f", pedido.getTotal()));
        
        // Mostrar productos del pedido
        if (pedido.getDetalles() != null) {
            String productos = pedido.getDetalles().stream()
                .map(d -> String.format("%s x%d - $%.2f", 
                    d.getProducto() != null ? d.getProducto().getNombre() : "Producto", 
                    d.getCantidad(), 
                    d.getSubtotal()))
                .collect(Collectors.joining("\n"));
            txtDetalleProductos.setText(productos);
        } else {
            txtDetalleProductos.setText("No hay detalles disponibles");
        }
        detallesPedido.setVisible(true);
    }
    
    @FXML
    private void actualizarEstado() {
        Pedido pedidoSeleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSeleccionado != null) {
            try {
                String nuevoEstado = obtenerSiguienteEstado(pedidoSeleccionado.getEstado());
                pedidoService.actualizarEstadoPedido(pedidoSeleccionado.getId(), nuevoEstado);
                pedidoSeleccionado.setEstado(nuevoEstado);
                tablaPedidos.refresh();
                mostrarDetallesPedido(pedidoSeleccionado);
                AlertUtils.mostrarInformacion("Éxito", "Estado del pedido actualizado correctamente");
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
                AlertUtils.mostrarError("Error", "No se pudo actualizar el estado del pedido: " + e.getMessage());
            }
        } else {
            AlertUtils.mostrarAdvertencia("Advertencia", "Seleccione un pedido para actualizar");
        }
    }
    
    private String obtenerSiguienteEstado(String estadoActual) {
        return switch (estadoActual) {
            case "PENDIENTE" -> "EN PROCESO";
            case "EN PROCESO" -> "TERMINADO";
            case "TERMINADO" -> "ENTREGADO";
            default -> estadoActual;
        };
    }
    
    @FXML
    private void volverAMenu() {
        try {
            Stage stage = (Stage) tablaPedidos.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/views/gerencia/menu_principal.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertUtils.mostrarError("Error", "No se pudo cargar el menú principal");
        }
    }
    
    // Método para establecer el ID del local
    public void setLocalId(Long localId) {
        this.localId = localId;
        cargarPedidos();
    }
}
