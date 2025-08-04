package asedi.controllers.gerencia;

import java.util.function.Function;
import javafx.util.Callback;

/**
 * Fábrica para crear instancias de ProductoController con inyección de dependencias.
 */
public class ProductoControllerFactory implements Callback<Class<?>, Object> {
    
    private final Function<Long, Void> onMenuSelectedCallback;
    private Long menuId;
    
    public ProductoControllerFactory(Function<Long, Void> onMenuSelectedCallback) {
        this.onMenuSelectedCallback = onMenuSelectedCallback;
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
            ProductoController controller = new ProductoController();
            if (menuId != null) {
                controller.setMenuId(menuId);
            }
            return controller;
        }
        
        // Comportamiento predeterminado para otros controladores
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el controlador: " + type, e);
        }
    }
}
