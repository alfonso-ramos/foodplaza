package asedi.services;

import java.util.ArrayList;
import java.util.List;
import asedi.model.Plaza;

public class PlazaService {
    // TODO: Implementar conexión real con la API
    
    /**
     * Obtiene todas las plazas disponibles.
     * @return Lista de todas las plazas
     */
    public List<Plaza> obtenerTodas() {
        return obtenerTodasLasPlazas();
    }
    
    /**
     * @deprecated Usar obtenerTodas() en su lugar
     */
    public List<Plaza> obtenerTodasLasPlazas() {
        // Simulación de datos para pruebas
        List<Plaza> plazas = new ArrayList<>();
        
        // Datos de ejemplo
        plazas.add(new Plaza(1L, "Plaza Central", "Av. Principal #123", "Plaza principal de la ciudad"));
        plazas.add(new Plaza(2L, "Plaza del Sol", "Calle del Sol #456", "Plaza comercial con variedad de locales"));
        plazas.add(new Plaza(3L, "Centro Comercial Luna", "Boulevard Luna #789", "Centro comercial moderno con estacionamiento"));
        
        return plazas;
    }
    
    public Plaza obtenerPlazaPorId(int id) {
        // Simulación de búsqueda por ID
        for (Plaza plaza : obtenerTodasLasPlazas()) {
            if (plaza.getId() == id) {
                return plaza;
            }
        }
        return null;
    }
    
    public boolean guardarPlaza(Plaza plaza) {
        // TODO: Implementar lógica para guardar la plaza
        System.out.println("Guardando plaza: " + plaza.getNombre());
        return true;
    }
    
    public boolean actualizarPlaza(Plaza plaza) {
        // TODO: Implementar lógica para actualizar la plaza
        System.out.println("Actualizando plaza: " + plaza.getNombre());
        return true;
    }
    
    public boolean eliminarPlaza(int id) {
        // TODO: Implementar lógica para eliminar la plaza
        System.out.println("Eliminando plaza con ID: " + id);
        return true;
    }
}
