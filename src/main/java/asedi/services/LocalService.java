package asedi.services;

import asedi.model.ImagenResponse;
import asedi.model.Local;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalService {
    private static final String ENDPOINT = "locales";
    private final Gson gson = new Gson();
    
    /**
     * Obtiene los locales por ID de plaza
     * @param plazaId ID de la plaza
     * @return Lista de locales
     */
    public List<Local> obtenerLocalesPorPlaza(Long plazaId) {
        try {
            String url = ENDPOINT + "?plaza_id=" + plazaId;
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.get(url, String.class);
                
            if (response.getStatusCode() == 200) {
                // Parsear la respuesta JSON a una lista de locales
                return gson.fromJson(response.getBody(), 
                    new com.google.gson.reflect.TypeToken<List<Local>>(){}.getType());
            }
        } catch (Exception e) {
            System.err.println("Error al obtener locales por plaza: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Guarda un nuevo local en el servidor
     * @param local El local a guardar
     * @param imagenArchivo Archivo de imagen a subir (opcional)
     * @return true si se guardó correctamente, false en caso contrario
     */
    public boolean guardarLocal(Local local, File imagenArchivo) {
        try {
            // 1. Si hay una imagen para subir, la subimos primero
            if (imagenArchivo != null && imagenArchivo.exists()) {
                try {
                    ImagenResponse respuesta = subirImagenLocal(null, imagenArchivo, local.getNombre());
                    if (respuesta != null && respuesta.getUrl() != null) {
                        local.setImagenUrl(respuesta.getUrl());
                        local.setImagenPublicId(respuesta.getPublic_id());
                    }
                } catch (Exception e) {
                    System.err.println("Advertencia: No se pudo subir la imagen " + imagenArchivo.getName() + 
                                   ". Error: " + e.getMessage());
                    // Continuamos aun si falla la subida de la imagen
                }
            }
            
            // 2. Guardar el local con la URL de la imagen
            return guardarLocalYDevolverId(local) != null;
        } catch (Exception e) {
            System.err.println("Error al guardar el local: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Guarda un nuevo local y devuelve su ID
     * @param local El local a guardar
     * @return El ID del local guardado, o null si hubo un error
     * @throws IOException Si hay un error de conexión
     */
    private Long guardarLocalYDevolverId(Local local) throws IOException {
        // Crear el cuerpo de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", local.getNombre());
        requestBody.put("descripcion", local.getDescripcion());
        requestBody.put("direccion", local.getDireccion());
        requestBody.put("horario_apertura", local.getHorarioApertura());
        requestBody.put("horario_cierre", local.getHorarioCierre());
        requestBody.put("tipo_comercio", local.getTipoComercio());
        requestBody.put("estado", local.getEstado() != null ? local.getEstado() : "activo");
        requestBody.put("plaza_id", local.getPlazaId());
        requestBody.put("id_gerente", local.getIdGerente()); // Puede ser null
        
        // Incluir la URL de la imagen si existe
        if (local.getImagenUrl() != null && !local.getImagenUrl().isEmpty()) {
            requestBody.put("imagen_url", local.getImagenUrl());
        }
        if (local.getImagenPublicId() != null && !local.getImagenPublicId().isEmpty()) {
            requestBody.put("imagen_public_id", local.getImagenPublicId());
        }
        
        // Realizar la petición POST
        HttpClientUtil.HttpResponseWrapper<String> response = 
            HttpClientUtil.post(ENDPOINT, requestBody, String.class);
            
        // Verificar si la respuesta es exitosa (código 2xx)
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            // Parsear la respuesta para obtener el ID del local creado
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            return jsonResponse.get("id").getAsLong();
        } else {
            System.err.println("Error al guardar el local: " + response.getBody());
            return null;
        }
    }
    
    /**
     * Sube una imagen para un local específico
     * @param localId ID del local al que se asociará la imagen
     * @param imagen Archivo de imagen a subir
     * @param nombreLocal Nombre del local para la descripción de la imagen
     * @return La respuesta del servidor con la URL de la imagen subida
     * @throws IOException Si hay un error al subir la imagen
     */
    private ImagenResponse subirImagenLocal(Long localId, File imagen, String nombreLocal) throws IOException {
        if (imagen == null) {
            throw new IOException("No se ha seleccionado ninguna imagen para subir");
        }
        
        System.out.println("\n=== INICIANDO SUBIDA DE IMAGEN ===");
        System.out.println("Local ID: " + localId);
        System.out.println("Archivo: " + imagen.getAbsolutePath());
        System.out.println("Tamaño: " + (imagen.length() / 1024) + " KB");
        System.out.println("Tipo MIME: " + java.nio.file.Files.probeContentType(imagen.toPath()));
        
        try {
            // Construir la URL del endpoint de subida
            String endpoint = String.format("locales/%d/imagen", localId);
            
            // Preparar los campos del formulario
            Map<String, String> formFields = new HashMap<>();
            formFields.put("descripcion", "imagen de " + nombreLocal);
            
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
                ImagenResponse imagenResponse = response.getBody();
                if (imagenResponse != null) {
                    System.out.println("Imagen subida exitosamente. URL: " + 
                        (imagenResponse.getUrl() != null ? imagenResponse.getUrl() : "No disponible"));
                } else {
                    System.out.println("Advertencia: La respuesta del servidor no contiene datos de imagen");
                }
                return imagenResponse;
            } else {
                String errorMsg = "Error al subir la imagen. Código: " + response.getStatusCode();
                if (response.getBody() != null && response.getBody().getMensaje() != null) {
                    errorMsg += ", Mensaje: " + response.getBody().getMensaje();
                }
                throw new IOException(errorMsg);
            }
        } catch (IOException e) {
            System.err.println("Error en subirImagenLocal: " + e.getMessage());
            throw e; // Relanzar para manejo en el método llamador
        } catch (Exception e) {
            String errorMsg = "Error inesperado al subir la imagen: " + e.getMessage();
            System.err.println(errorMsg);
            throw new IOException(errorMsg, e);
        } finally {
            System.out.println("=== FIN DE SUBIDA DE IMAGEN ===\n");
        }
    }
    
    /**
     * Actualiza un local existente
     * @param local El local con los datos actualizados
     * @param nuevasImagenes Lista de nuevas imágenes a subir (puede ser null o vacía)
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarLocal(Local local, List<File> nuevasImagenes) {
        try {
            // 1. Actualizar los datos del local
            boolean exito = actualizarDatosLocal(local);
            
            if (!exito) {
                return false;
            }
            
            // 2. Subir las nuevas imágenes si hay alguna
            if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
                for (File imagen : nuevasImagenes) {
                    subirImagenLocal(local.getId(), imagen, local.getNombre());
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar el local: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Actualiza los datos de un local existente
     * @param local El local con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws IOException Si hay un error de conexión
     */
    private boolean actualizarDatosLocal(Local local) throws IOException {
        // Crear el cuerpo de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", local.getNombre());
        requestBody.put("descripcion", local.getDescripcion());
        requestBody.put("direccion", local.getDireccion());
        requestBody.put("horario_apertura", local.getHorarioApertura());
        requestBody.put("horario_cierre", local.getHorarioCierre());
        requestBody.put("tipo_comercio", local.getTipoComercio());
        requestBody.put("estado", local.getEstado());
        
        // Realizar la petición PUT
        String url = ENDPOINT + "/" + local.getId();
        HttpClientUtil.HttpResponseWrapper<String> response = 
            HttpClientUtil.put(url, requestBody, String.class);
            
        // Verificar si la respuesta es exitosa (código 2xx)
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }
    
    /**
     * Elimina un local existente
     * @param localId ID del local a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminarLocal(Long localId) {
        try {
            String url = ENDPOINT + "/" + localId;
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.delete(url, String.class);
                
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (Exception e) {
            System.err.println("Error al eliminar el local: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
