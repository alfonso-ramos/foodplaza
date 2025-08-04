package asedi.controllers;

import asedi.model.Local;
import asedi.model.Plaza;
import asedi.services.LocalService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class LocalesPorPlazaController {

    @FXML
    private Label plazaNombreLabel;

    @FXML
    private VBox mainContainer;
    
    @FXML
    private HBox headerContainer;
    
    @FXML
    private VBox contentContainer;
    
    @FXML
    private GridPane localesGrid;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @FXML
    private Button backButton;

    private Plaza plaza;
    private UsuarioDashboardController usuarioDashboardController;

    private final LocalService localService = new LocalService();

    public void setPlaza(Plaza plaza) {
        this.plaza = plaza;
        if (plazaNombreLabel != null) {
            plazaNombreLabel.setText("Locales en " + plaza.getNombre());
        }
        cargarLocales();
    }

    public void setUsuarioDashboardController(UsuarioDashboardController usuarioDashboardController) {
        this.usuarioDashboardController = usuarioDashboardController;
    }

    @FXML
    private void initialize() {
        // Configurar el grid de locales
        localesGrid.setHgap(20);
        localesGrid.setVgap(20);
        localesGrid.setPadding(new Insets(10));
        
        // Configurar el botón de regreso
        if (backButton != null) {
            backButton.setOnAction(ignored -> {
                if (usuarioDashboardController != null) {
                    usuarioDashboardController.cargarInicio();
                }
            });
        }
    }
    
    private void cargarLocales() {
        // Mostrar indicador de carga
        loadingIndicator.setVisible(true);
        contentContainer.setVisible(false);
        
        // Ejecutar en un hilo separado para no bloquear la interfaz de usuario
        new Thread(() -> {
            try {
                List<Local> locales = localService.obtenerLocalesPorPlaza(plaza.getId());
                
                // Actualizar la interfaz de usuario en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    try {
                        localesGrid.getChildren().clear();
                        
                        if (locales.isEmpty()) {
                            Label noLocalesLabel = new Label("No hay locales disponibles en esta plaza.");
                            noLocalesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20px;");
                            localesGrid.add(noLocalesLabel, 0, 0);
                        } else {
                            int column = 0;
                            int row = 0;
                            final int MAX_COLUMNS = 3;
                            
                            for (Local local : locales) {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/usuarioLocalCard.fxml"));
                                Parent localCard = loader.load();
                                UsuarioLocalCardController controller = loader.getController();
                                controller.setLocal(local);
                                controller.setUsuarioDashboardController(usuarioDashboardController);
                                
                                localesGrid.add(localCard, column, row);
                                
                                column++;
                                if (column >= MAX_COLUMNS) {
                                    column = 0;
                                    row++;
                                }
                            }
                        }
                        
                        // Asegurarse de que el grid ocupe todo el ancho disponible
                        GridPane.setHgrow(localesGrid, Priority.ALWAYS);
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        mostrarError("Error al cargar la interfaz de locales");
                    } finally {
                        // Ocultar indicador de carga y mostrar contenido
                        loadingIndicator.setVisible(false);
                        contentContainer.setVisible(true);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al cargar los locales: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                    contentContainer.setVisible(true);
                });
            }
        }).start();
    }
    
    private void mostrarError(String mensaje) {
        // Mostrar mensaje de error en la interfaz
        Label errorLabel = new Label(mensaje);
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-padding: 10px;");
        
        // Limpiar y mostrar el mensaje de error
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(errorLabel);
    }
}
