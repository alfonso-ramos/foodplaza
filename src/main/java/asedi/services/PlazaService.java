package asedi.services;

import asedi.model.ImagenResponse;
import asedi.model.Plaza;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;

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
     * @return true si la plaza se guardó correctamente, false en caso contrario
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
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                return true;
            } else {
                System.err.println("Error al guardar la plaza: " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error en guardarPlaza: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Guarda una nueva plaza en el servidor y devuelve el ID de la plaza creada.
     * @param plaza La plaza a guardar
     * @return El ID de la plaza creada, o null si hubo un error
     * @throws IOException Si hay un error de conexión
     */
    public Long guardarPlazaYDevolverId(Plaza plaza) throws IOException {
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
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                // Parsear la respuesta para obtener el ID de la plaza creada
                JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
                return jsonResponse.get("id").getAsLong();
            } else {
                System.err.println("Error al guardar la plaza: " + response.getBody());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error en guardarPlazaYDevolverId: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Sube una imagen para una plaza específica.
     * @param plazaId ID de la plaza a la que se asociará la imagen
     * @param imagen Archivo de imagen a subir
     * @param nombrePlaza Nombre de la plaza para la descripción de la imagen
     * @return La respuesta del servidor con la URL de la imagen subida
     * @throws IOException Si hay un error de conexión
     */
    public ImagenResponse subirImagenPlaza(Long plazaId, File imagen, String nombrePlaza) throws IOException {
        try {
            // Construir la URL del endpoint de subida
            String endpoint = String.format("plazas/%d/imagen", plazaId);
            
            // Preparar los campos del formulario
            Map<String, String> formFields = new HashMap<>();
            formFields.put("descripcion", "imagen de " + nombrePlaza);
            
            // Realizar la petición de subida
            HttpClientUtil.HttpResponseWrapper<ImagenResponse> response = 
                HttpClientUtil.uploadFile(
                    endpoint,
                    "file",
                    imagen,
                    formFields,
                    ImagenResponse.class
                );
            
            // Verificar si la respuesta es exitosa
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                return response.getBody();
            } else {
                throw new IOException("Error al subir la imagen: " + 
                    (response.getBody() != null ? response.getBody().getMensaje() : "Error desconocido"));
            }
        } catch (Exception e) {
            System.err.println("Error en subirImagenPlaza: " + e.getMessage());
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
