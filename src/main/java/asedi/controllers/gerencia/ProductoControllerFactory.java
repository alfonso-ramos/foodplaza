package asedi.controllers.gerencia;

import java.util.function.Function;
import javafx.util.Callback;
import asedi.services.ProductoService;
import asedi.services.MenuService;

/**
 * Fábrica para crear instancias de ProductoController con inyección de dependencias.
 */
public class ProductoControllerFactory implements Callback<Class<?>, Object> {
    
    private final Function<Long, Void> onMenuSelectedCallback;
    private final ProductoService productoService;
    private final MenuService menuService;
    private Long menuId;
    
    public ProductoControllerFactory(Function<Long, Void> onMenuSelectedCallback, 
                                   ProductoService productoService,
                                   MenuService menuService) {
        this.onMenuSelectedCallback = onMenuSelectedCallback;
        this.productoService = productoService;
        this.menuService = menuService;
    }
    
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
        if (onMenuSelectedCallback != null) {
            onMenuSelectedCallback.apply(menuId);
        }
    }
    
    @Override
    public Object call(Class<?> type) {
        if (type == ProductoController.class) {
            // Create controller with required services
            ProductoController controller = new ProductoController(productoService, menuService);
            if (menuId != null) {
                controller.setMenuId(menuId);
            }
            return controller;
        }
        
        // Default behavior for other controllers
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el controlador: " + type, e);
        }
    }
}
