package asedi.services;

import asedi.model.Plaza;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlazaService {
    private static final String ENDPOINT = "plazas";
    private final Gson gson = new Gson();
    
    /**
     * Obtiene todas las plazas disponibles desde la API.
     * @return Lista de todas las plazas
     * @throws IOException Si hay un error de conexión
     */
    public List<Plaza> obtenerTodas() throws IOException {
        try {
            // Realizar la petición GET a la API
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.get(ENDPOINT, String.class);
            
            // Verificar si la respuesta es exitosa
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                // Parsear la respuesta JSON a una lista de plazas
                Type listType = new TypeToken<ArrayList<Plaza>>(){}.getType();
                return gson.fromJson(response.getBody(), listType);
            } else {
                throw new IOException("Error al obtener las plazas: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerTodas: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * @deprecated Usar obtenerTodas() en su lugar
     */
    @Deprecated
    public List<Plaza> obtenerTodasLasPlazas() {
        try {
            return obtenerTodas();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene una plaza por su ID.
     * @param id ID de la plaza a buscar
     * @return La plaza encontrada o null si no existe
     * @throws IOException Si hay un error de conexión
     */
    public Plaza obtenerPlazaPorId(Long id) throws IOException {
        try {
            String url = ENDPOINT + "/" + id;
            HttpClientUtil.HttpResponseWrapper<Plaza> response = 
                HttpClientUtil.get(url, Plaza.class);
                
            if (response.getStatusCode() == 200) {
                return response.getBody();
            } else if (response.getStatusCode() == 404) {
                return null; // Plaza no encontrada
            } else {
                throw new IOException("Error al obtener la plaza: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerPlazaPorId: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Guarda una nueva plaza en el servidor.
     * @param plaza La plaza a guardar
     * @return true si se guardó correctamente, false en caso contrario
     * @throws IOException Si hay un error de conexión
     */
    public boolean guardarPlaza(Plaza plaza) throws IOException {
        try {
            // Crear el cuerpo de la petición
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("nombre", plaza.getNombre());
            requestBody.put("direccion", plaza.getDireccion());
            requestBody.put("estado", plaza.getEstado());
            
            // Realizar la petición POST
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.post(ENDPOINT, requestBody, String.class);
                
            // Verificar si la respuesta es exitosa (código 2xx)
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (Exception e) {
            System.err.println("Error en guardarPlaza: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Actualiza una plaza existente en el servidor.
     * @param plaza La plaza con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws IOException Si hay un error de conexión
     */
    public boolean actualizarPlaza(Plaza plaza) throws IOException {
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
