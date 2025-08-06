package asedi.controllers;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import asedi.model.Local;
import asedi.model.Plaza;
import asedi.services.LocalService;
import asedi.services.PlazaService;

public class LocalesController {
    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private ComboBox<Plaza> plazasComboBox;
    @FXML private FlowPane localesContainer;
    @FXML private Button agregarLocalBtn;
    @FXML private TextField searchField;
    @FXML private VBox emptyState;
    @FXML private StackPane loadingOverlay;
    @FXML private Label statusLabel;
    @FXML private Button retryButton;
    
    private PlazaService plazaService;
    private LocalService localService;
    private List<Plaza> plazas;
    
    @FXML
    public void initialize() {
        plazaService = new PlazaService();
        localService = new LocalService();
        
        // Configurar el ComboBox
        plazasComboBox.setCellFactory(e -> new ListCell<Plaza>() {
            @Override
            protected void updateItem(Plaza plaza, boolean empty) {
                super.updateItem(plaza, empty);
                setText(empty ? "" : plaza.getNombre());
            }
        });
        
        plazasComboBox.setButtonCell(new ListCell<Plaza>() {
            @Override
            protected void updateItem(Plaza plaza, boolean empty) {
                super.updateItem(plaza, empty);
                setText(empty ? "Seleccione una plaza" : plaza.getNombre());
            }
        });
        
        plazasComboBox.valueProperty().addListener((obs, oldVal, newValue) -> {
            if (newValue != null) {
                mostrarCargando(true, "Cargando locales...");
                cargarLocales(newValue.getId());
            }
        });
        
        // Configurar el campo de búsqueda
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (plazasComboBox.getValue() != null) {
                cargarLocales(plazasComboBox.getValue().getId());
            }
        });
        
        // Configurar el botón de reintento
        retryButton.setOnAction(event -> {
            if (plazasComboBox.getValue() != null) {
                mostrarCargando(true, "Cargando locales...");
                cargarLocales(plazasComboBox.getValue().getId());
            } else {
                cargarPlazas();
            }
        });
        
        // Cargar las plazas
        cargarPlazas();
    }
    
    private void mostrarCargando(boolean mostrar, String mensaje) {
        loadingOverlay.setVisible(mostrar);
        loadingOverlay.setManaged(mostrar);
        statusLabel.setText(mensaje);
        retryButton.setVisible(false);
    }
    
    private void mostrarError(String mensaje) {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
        statusLabel.setText(mensaje);
        retryButton.setVisible(true);
    }
    
    private void cargarPlazas() {
        mostrarCargando(true, "Cargando plazas...");
        
        new Thread(() -> {
            try {
                List<Plaza> plazasCargadas = plazaService.obtenerTodas();
                
                javafx.application.Platform.runLater(() -> {
                    plazas = plazasCargadas;
                    plazasComboBox.getItems().setAll(plazas);
                    
                    if (!plazas.isEmpty()) {
                        plazasComboBox.getSelectionModel().selectFirst();
                    } else {
                        mostrarCargando(false, "");
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al cargar las plazas: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleRetry() {
        if (plazasComboBox.getValue() != null) {
            cargarLocales(plazasComboBox.getValue().getId());
        } else if (!plazas.isEmpty()) {
            plazasComboBox.getSelectionModel().selectFirst();
        }
    }
    
    public void cargarLocales(Long plazaId) {
        String filtroBusqueda = searchField.getText().toLowerCase();
        
        new Thread(() -> {
            try {
                List<Local> localesFiltrados = localService.obtenerLocalesPorPlaza(plazaId);
                
                // Filtrar por búsqueda si es necesario
                if (filtroBusqueda != null && !filtroBusqueda.isEmpty()) {
                    localesFiltrados = localesFiltrados.stream()
                        .filter(local -> local.getNombre().toLowerCase().contains(filtroBusqueda) ||
                                       local.getDescripcion().toLowerCase().contains(filtroBusqueda))
                        .toList();
                }
                
                List<Local> finalLocales = localesFiltrados;
                javafx.application.Platform.runLater(() -> {
                    mostrarLocales(finalLocales);
                    mostrarCargando(false, "");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al cargar los locales: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void mostrarLocales(List<Local> locales) {
        localesContainer.getChildren().clear();
        
        if (locales.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }
        
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        
        for (Local local : locales) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/localCard.fxml"));
                Parent card = loader.load();
                LocalCardController controller = loader.getController();
                controller.setLocal(local);
                controller.setParentController(this); // Establecer el controlador padre
                
                // Configurar el evento de clic para editar el local
                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) { // Doble clic para editar
                        abrirFormularioEditarLocal(local);
                    }
                });
                
                localesContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void refreshLocales() {
        if (plazasComboBox.getValue() != null) {
            mostrarCargando(true, "Actualizando locales...");
            cargarLocales(plazasComboBox.getValue().getId());
        } else {
            cargarPlazas();
        }
    }
    
    @FXML
    private void abrirFormularioAgregarLocal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarLocal.fxml"));
            Parent root = loader.load();
            
            AgregarLocalController controller = loader.getController();
            controller.setPlazas(plazas);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Agregar Nuevo Local");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recargar locales después de cerrar el formulario
            if (plazasComboBox.getValue() != null) {
                cargarLocales(plazasComboBox.getValue().getId());
            }
        } catch (IOException e) {
            mostrarError("Error al abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void abrirFormularioEditarLocal(Local local) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/editarLocal.fxml"));
            Parent root = loader.load();
            
            EditarLocalController controller = loader.getController();
            controller.setLocalesController(this);
            controller.setLocal(local);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Local: " + local.getNombre());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recargar locales después de cerrar el formulario
            if (plazasComboBox.getValue() != null) {
                cargarLocales(plazasComboBox.getValue().getId());
            }
        } catch (IOException e) {
            mostrarError("Error al abrir el formulario de edición: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recarga la lista de locales para la plaza actualmente seleccionada.
     * Este método puede ser llamado desde otros controladores para actualizar la vista.
     */
    public void recargarLocales() {
        if (plazasComboBox.getValue() != null) {
            cargarLocales(plazasComboBox.getValue().getId());
        } else if (!plazas.isEmpty()) {
            plazasComboBox.getSelectionModel().selectFirst();
        } else {
            cargarPlazas();
        }
    }
    
    // El método mostrarError ya está definido más arriba en la clase
}
