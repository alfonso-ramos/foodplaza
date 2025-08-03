package asedi.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import asedi.model.Plaza;
import asedi.services.PlazaService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class PlazasController {
    private static final int COLUMNS = 3;
    
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane gridPlazas;
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private Button retryButton;
    @FXML private VBox emptyState;
    @FXML private Button btnRefresh;
    @FXML private Button btnAddPlaza;
    
    private final PlazaService plazaService = new PlazaService();
    
    @FXML
    public void initialize() {
        setupLoadingOverlay();
        setupScrollPane();
        setupEventHandlers();
        cargarPlazas();
    }
    
    private void setupLoadingOverlay() {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
        loadingOverlay.setMouseTransparent(true);
        
        // Configurar transición de desvanecimiento
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), loadingOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        loadingOverlay.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                loadingOverlay.setVisible(true);
                loadingOverlay.setManaged(true);
                loadingOverlay.setMouseTransparent(false);
                fadeIn.playFromStart();
            } else {
                loadingOverlay.setMouseTransparent(true);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loadingOverlay);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    loadingOverlay.setVisible(false);
                    loadingOverlay.setManaged(false);
                });
                fadeOut.play();
            }
        });
    }
    
    private void setupScrollPane() {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            System.err.println("Warning: scrollPane is null in setupScrollPane()");
        }
    }
    
    private void setupEventHandlers() {
        if (btnRefresh != null) {
            btnRefresh.setOnAction(event -> cargarPlazas());
        }
        
        if (btnAddPlaza != null) {
            btnAddPlaza.setOnAction(event -> mostrarFormularioAgregarPlaza());
        }
        
        if (retryButton != null) {
            retryButton.setOnAction(event -> cargarPlazas());
        }
    }
    
    public void cargarPlazas() {
        showLoading("Cargando plazas...");
        
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    List<Plaza> plazas = plazaService.obtenerTodas();
                    
                    Platform.runLater(() -> {
                        try {
                            gridPlazas.getChildren().clear();
                            
                            if (plazas.isEmpty()) {
                                emptyState.setVisible(true);
                                emptyState.setManaged(true);
                                return;
                            }
                            
                            int col = 0;
                            int row = 0;
                            
                            for (Plaza plaza : plazas) {
                                try {
                                    FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource("/views/components/plazaCard.fxml"));
                                    VBox card = loader.load();
                                    
                                    PlazaCardController controller = loader.getController();
                                    controller.setPlazaData(
                                        plaza.getId().intValue(),
                                        plaza.getNombre(),
                                        plaza.getDireccion(),
                                        plaza.getEstado(),
                                        plaza.getImagenUrl()
                                    );
                                    controller.setPlazasController(PlazasController.this);
                                    
                                    gridPlazas.add(card, col, row);
                                    
                                    col = (col + 1) % COLUMNS;
                                    if (col == 0) row++;
                                    
                                } catch (Exception e) {
                                    System.err.println("Error al cargar la plaza " + plaza.getNombre() + ": " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                            
                            emptyState.setVisible(plazas.isEmpty());
                            emptyState.setManaged(plazas.isEmpty());
                            
                        } catch (Exception e) {
                            throw new RuntimeException("Error al actualizar la interfaz de usuario", e);
                        }
                    });
                    
                } catch (Exception e) {
                    throw new IOException("Error al cargar las plazas: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                hideLoading();
            }

            @Override
            protected void failed() {
                hideLoading();
                showError("Error al cargar las plazas: " + getException().getMessage());
            }
        };

        new Thread(loadTask).start();
    }
    
    public void mostrarFormularioAgregarPlaza() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarPlaza.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Agregar Nueva Plaza");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recargar las plazas después de cerrar el formulario
            cargarPlazas();
        } catch (Exception e) {
            showError("No se pudo abrir el formulario de agregar plaza: " + e.getMessage());
        }
    }
    
    public void modificarPlaza(int id) {
        try {
            // Primero intentar cargar el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/editarPlaza.fxml"));
            
            // Verificar si el archivo FXML existe
            if (loader.getLocation() == null) {
                showInformation("Función en desarrollo", "La función de edición de plazas estará disponible pronto.");
                return;
            }
            
            Parent root = loader.load();
            
            // Verificar si el controlador existe
            Object controller = loader.getController();
            if (controller == null) {
                showInformation("Función en desarrollo", "La función de edición de plazas estará disponible pronto.");
                return;
            }
            
            // Intentar establecer el ID de la plaza usando reflexión
            try {
                java.lang.reflect.Method setPlazaId = controller.getClass().getMethod("setPlazaId", int.class);
                setPlazaId.invoke(controller, id);
            } catch (Exception e) {
                System.err.println("No se pudo establecer el ID de la plaza: " + e.getMessage());
            }
            
            Stage stage = new Stage();
            stage.setTitle("Editar Plaza");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recargar las plazas después de cerrar el formulario
            cargarPlazas();
        } catch (Exception e) {
            showError("No se pudo abrir el formulario de edición: " + e.getMessage());
        }
    }
    
    public void eliminarPlaza(int id) {
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar eliminación");
        confirmDialog.setHeaderText("¿Está seguro de que desea eliminar esta plaza?");
        confirmDialog.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showLoading("Eliminando plaza...");
            
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return plazaService.eliminarPlaza(id);
                }
                
                @Override
                protected void succeeded() {
                    if (getValue()) {
                        showSuccess("Plaza eliminada correctamente");
                        cargarPlazas();
                    } else {
                        showError("No se pudo eliminar la plaza");
                    }
                }
                
                @Override
                protected void failed() {
                    showError("Error al eliminar la plaza: " + getException().getMessage());
                }
            };
            
            new Thread(deleteTask).start();
        }
    }
    
    private void showLoading(String message) {
        Platform.runLater(() -> {
            loadingOverlay.setVisible(true);
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }
            if (retryButton != null) {
                retryButton.setVisible(false);
            }
        });
    }
    
    private void hideLoading() {
        Platform.runLater(() -> {
            loadingOverlay.setVisible(false);
        });
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showInformation(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    @FXML
    private void handleRefresh(ActionEvent event) {
        cargarPlazas();
    }
    
    @FXML
    public void refreshPlazas() {
        cargarPlazas();
    }
    
    @FXML
    private void handleAddPlaza(ActionEvent event) {
        mostrarFormularioAgregarPlaza();
    }
}
