package asedi.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import asedi.model.Local;

public class LocalService {
    // TODO: Implementar conexión real con la API
    
    public List<Local> obtenerLocalesPorPlaza(int plazaId) {
        // Simulación de datos para pruebas
        List<Local> locales = new ArrayList<>();
        
        // Datos de ejemplo
        if (plazaId == 1) {
            Local local1 = new Local(1, 1, "Restaurante La Esquina", "Comida mexicana tradicional", "Local 12", "09:00", "22:00", "Restaurante");
            Local local2 = new Local(2, 1, "Café Aromas", "Café de especialidad y repostería", "Local 15", "07:00", "21:00", "Cafetería");
            locales.add(local1);
            locales.add(local2);
        }
        
        return locales;
    }
    
    public boolean guardarLocal(Local local, List<File> imagenes) {
        // TODO: Implementar lógica para guardar el local
        System.out.println("Guardando local: " + local.getNombre());
        return true;
    }
    
    public boolean actualizarLocal(Local local, List<File> nuevasImagenes) {
        // TODO: Implementar lógica para actualizar el local
        System.out.println("Actualizando local: " + local.getNombre());
        return true;
    }
    
    public boolean eliminarLocal(int localId) {
        // TODO: Implementar lógica para eliminar el local
        System.out.println("Eliminando local con ID: " + localId);
        return true;
    }
}
