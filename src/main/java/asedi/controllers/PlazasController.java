package asedi.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.ProgressIndicator;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class PlazasController {
    private static final int COLUMNS = 3;
    private static final String PLAZAS_JSON_PATH = "/data/plazas.json";
    private static final int MAX_RETRIES = 3;
    
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane gridPlazas;
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private Button retryButton;
    @FXML private VBox emptyState;
    
    @FXML
    public void initialize() {
        setupLoadingOverlay();
        cargarPlazas();
    }
    
    private void setupLoadingOverlay() {
        // Make sure the loading overlay is on top and initially hidden
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
        loadingOverlay.setMouseTransparent(true);
        
        // Style the status label
        statusLabel.getStyleClass().add("status-label");
        
        // Style the retry button
        retryButton.getStyleClass().add("retry-button");
        retryButton.setOnAction(event -> {
            event.consume();
            cargarPlazas();
        });
    }
    
    @FXML
    public void cargarPlazas() {
        showLoading("Cargando plazas...");
        
        // Run the loading task in a background thread
        Task<Void> loadTask = new Task<>() {
            @Override
            protected void updateMessage(String message) {
                super.updateMessage(message);
                Platform.runLater(() -> {
                    statusLabel.textProperty().unbind();
                    statusLabel.setText(message);
                });
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> showSuccess("Plazas cargadas correctamente"));
            }
            
            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    showError("Error al cargar las plazas: " + 
                            (getException() != null ? getException().getMessage() : "Error desconocido"));
                    retryButton.setVisible(true);
                });
            }
            
            @Override
            protected Void call() throws Exception {
                int attempt = 0;
                Exception lastError = null;
                
                while (attempt < MAX_RETRIES) {
                    // Get the resource URL first for better error reporting
                    java.net.URL resourceUrl = getClass().getResource(PLAZAS_JSON_PATH);
                    if (resourceUrl == null) {
                        throw new IOException("No se pudo encontrar el archivo en la ruta: " + 
                            PLAZAS_JSON_PATH + " (ruta absoluta: " + 
                            new java.io.File("src/main/resources" + PLAZAS_JSON_PATH).getAbsolutePath() + ")");
                    }
                    
                    try (InputStream is = resourceUrl.openStream()) {
                        
                        // Update status
                        updateMessage("Procesando datos...");
                        
                        // Parse JSON
                        JSONParser parser = new JSONParser();
                        JSONObject jsonObject = (JSONObject) parser.parse(
                            new InputStreamReader(is, StandardCharsets.UTF_8));
                        JSONArray plazas = (JSONArray) jsonObject.get("plazas");
                        
                        if (plazas == null || plazas.isEmpty()) {
                            throw new Exception("No se encontraron plazas en el archivo");
                        }
                        
                        // Clear the grid on the JavaFX Application Thread and reset empty state
                        Platform.runLater(() -> {
                            gridPlazas.getChildren().clear();
                            emptyState.setVisible(false);
                            emptyState.setManaged(false);
                        });
                        
                        // Process each plaza
                        AtomicInteger index = new AtomicInteger(0);
                        for (Object plazaObj : plazas) {
                            if (isCancelled()) {
                                return null;
                            }
                            
                            try {
                                JSONObject plaza = (JSONObject) plazaObj;
                                int currentIndex = index.getAndIncrement();
                                
                                // Validate required fields
                                int id = validatePlazaId(plaza.get("id"));
                                String nombre = validateString(plaza.get("nombre"), "Sin nombre");
                                String ubicacion = validateString(plaza.get("ubicacion"), "Ubicación no disponible");
                                String descripcion = validateString(plaza.get("descripcion"), "");
                                String imagen = validateString(plaza.get("imagen"), "/images/placeholder.svg");
                                
                                // Update progress
                                updateMessage(String.format("Cargando plaza %d de %d", 
                                    currentIndex + 1, plazas.size()));
                                
                                // Create and add the plaza card on the JavaFX Application Thread
                                Platform.runLater(() -> {
                                    try {
                                        addPlazaCard(currentIndex, id, nombre, ubicacion, descripcion, imagen);
                                    } catch (Exception e) {
                                        System.err.println("Error al agregar la tarjeta de la plaza " + id + ": " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                });
                                
                            } catch (Exception e) {
                                System.err.println("Error al procesar una plaza: " + e.getMessage());
                                // Continue with next plaza even if one fails
                            }
                        }
                        
                        // If we get here, loading was successful
                        boolean isEmpty = plazas.isEmpty();
                        Platform.runLater(() -> {
                            showSuccess("Plazas cargadas correctamente");
                            emptyState.setVisible(isEmpty);
                            emptyState.setManaged(isEmpty);
                        });
                        return null;
                        
                    } catch (Exception e) {
                        lastError = e;
                        attempt++;
                        if (attempt < MAX_RETRIES) {
                            updateMessage(String.format("Reintentando... (%d/%d)", attempt, MAX_RETRIES));
                            Thread.sleep(1000); // Wait before retry
                        }
                    }
                }
                
                // If we get here, all attempts failed
                throw lastError != null ? lastError : new Exception("Error desconocido al cargar las plazas");
            }
        };
        
        // Bind UI updates to the task's properties
        statusLabel.textProperty().bind(loadTask.messageProperty());
        
        // Start the task in a new thread
        new Thread(loadTask).start();
    }
    
    private int validatePlazaId(Object idObj) {
        if (idObj == null) {
            throw new IllegalArgumentException("El ID de la plaza no puede ser nulo");
        }
        try {
            return ((Number) idObj).intValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("El ID de la plaza debe ser un número");
        }
    }
    
    private String validateString(Object str, String defaultValue) {
        return (str != null && !str.toString().trim().isEmpty()) ? str.toString().trim() : defaultValue;
    }
    
    private void addPlazaCard(int index, int id, String nombre, String ubicacion, String descripcion, String imagen) {
        try {
            System.out.println("Cargando tarjeta para plaza: " + nombre + " (ID: " + id + ")");
            
            // Verificar que el grid no sea nulo
            if (gridPlazas == null) {
                System.err.println("ERROR: gridPlazas es nulo");
                return;
            }
            
            // Cargar el componente de la tarjeta
            String fxmlPath = "/views/components/plazaCard.fxml";
            System.out.println("Cargando FXML desde: " + fxmlPath);
            
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("ERROR: No se pudo encontrar el archivo FXML en: " + fxmlPath);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            VBox card = loader.load();
            
            if (card == null) {
                System.err.println("ERROR: No se pudo cargar el componente de tarjeta");
                return;
            }
            
            // Configurar el controlador
            PlazaCardController controller = loader.getController();
            if (controller == null) {
                System.err.println("ERROR: No se pudo obtener el controlador de la tarjeta");
                return;
            }
            
            controller.setPlazasController(this);
            controller.setPlazaData(id, nombre, ubicacion, descripcion, imagen);
            
            // Agregar al grid
            int col = index % COLUMNS;
            int row = index / COLUMNS;
            System.out.println("Agregando tarjeta en columna: " + col + ", fila: " + row);
            
            // Asegurarse de que el grid tenga suficientes filas
            if (row >= gridPlazas.getRowCount()) {
                gridPlazas.addRow(row);
            }
            
            gridPlazas.add(card, col, row);
            
            // Configurar restricciones de la cuadrícula
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setVgrow(card, Priority.ALWAYS);
            
            // Animación de entrada
            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
            
            System.out.println("Tarjeta agregada exitosamente: " + nombre);
            
        } catch (Exception e) {
            System.err.println("ERROR en addPlazaCard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void cargarAgregarPlaza(ActionEvent event) {
        try {
            // Cargar la vista para agregar plaza
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/agregarPlaza.fxml"));
            Parent agregarPlazaView = loader.load();
            
            // Obtener el nodo que disparó el evento
            Node sourceNode = (Node) event.getSource();
            
            // Obtener el contenido principal del dashboard y limpiarlo
            VBox contenidoPane = (VBox) sourceNode.getScene().lookup("#contenidoPlazas");
            if (contenidoPane != null) {
                contenidoPane.getChildren().setAll(agregarPlazaView);
            } else {
                // Si no se encuentra el contenidoPane, intentar cargar la vista de otra manera
                Stage stage = (Stage) sourceNode.getScene().getWindow();
                Scene scene = new Scene(agregarPlazaView);
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de agregar plaza: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    public void modificarPlaza(int id) {
        // Implementar lógica para modificar la plaza
        showAlert("Modificar Plaza", "Modificando plaza con ID: " + id, AlertType.INFORMATION);
    }
    
    public void eliminarPlaza(int id) {
        // Mostrar confirmación antes de eliminar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro de que desea eliminar esta plaza?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Aquí iría la lógica para eliminar la plaza
                showAlert("Eliminada", "Plaza eliminada correctamente", AlertType.INFORMATION);
                cargarPlazas(); // Recargar la lista de plazas
            }
        });
    }
    
    private void showLoading(String message) {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
        loadingOverlay.setMouseTransparent(false);
        loadingIndicator.setVisible(true);
        statusLabel.setText(message);
        retryButton.setVisible(false);
    }
    
    private void hideLoading() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), loadingOverlay);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            loadingOverlay.setMouseTransparent(true);
        });
        ft.play();
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        loadingIndicator.setVisible(false);
        
        // Auto-hide after a delay
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> hideLoading());
                }
            },
            1500 // 1.5 seconds delay
        );
    }
    
    private void showError(String message) {
        // Usar Platform.runLater para asegurar que se ejecute en el hilo de JavaFX
        Platform.runLater(() -> {
            // Primero, desvincular cualquier binding existente
            statusLabel.textProperty().unbind();
            // Luego establecer el texto
            statusLabel.setText(message);
            loadingIndicator.setVisible(false);
            retryButton.setVisible(true);
        });
    }
    
    @FXML
    public void refreshPlazas() {
        cargarPlazas();
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Style the dialog
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/main.css")).toExternalForm());
            
            alert.showAndWait();
        });
    }
}
