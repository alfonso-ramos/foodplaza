package asedi.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClientUtil {
    private static final Gson gson = new Gson();
    private static final String API_BASE_URL = "https://foodplazaapi.ponchoramos.com/api/";

    public static class HttpResponseWrapper<T> {
        private final int statusCode;
        private final T body;
        
        public HttpResponseWrapper(int statusCode, T body) {
            this.statusCode = statusCode;
            this.body = body;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public T getBody() {
            return body;
        }
    }
    
    /**
     * Realiza una petición GET a la API.
     * @param endpoint El endpoint de la API (sin la URL base)
     * @param responseType La clase del tipo de respuesta esperada
     * @param <T> El tipo de la respuesta
     * @return Un HttpResponseWrapper con la respuesta
     * @throws IOException Si hay un error en la comunicación
     */
    private static CloseableHttpClient createHttpClient() {
        return HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }
    
    public static <T> HttpResponseWrapper<T> get(String endpoint, Class<T> responseType) throws IOException {
        CloseableHttpClient client = createHttpClient();
        try {
            // Remove any leading slashes to prevent double slashes in URL
            String cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String fullUrl = API_BASE_URL + cleanEndpoint;
            
            // Create and log the request
            org.apache.http.client.methods.HttpGet request = new org.apache.http.client.methods.HttpGet(fullUrl);
            System.out.println("\n=== HTTP GET Request ===");
            System.out.println("URL: " + request.getURI());
            System.out.println("Method: " + request.getMethod());
            System.out.println("Headers: " + java.util.Arrays.toString(request.getAllHeaders()));
            
            // Add required headers
            request.setHeader("Accept", "application/json");
            
            // Execute the request
            try (org.apache.http.client.methods.CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                // Log response details
                System.out.println("\n=== HTTP Response ===");
                System.out.println("Status: " + response.getStatusLine());
                System.out.println("Headers: " + java.util.Arrays.toString(response.getAllHeaders()));
                System.out.println("Body: " + responseBody);
                
                // Parse response
                @SuppressWarnings("unchecked") // Safe cast when responseType is String.class
                T result = responseType == String.class ? 
                    (T) responseBody : 
                    gson.fromJson(responseBody, responseType);
                
                return new HttpResponseWrapper<>(
                    response.getStatusLine().getStatusCode(),
                    result
                );
            }
        } finally {
            client.close();
        }
    }
    
    /**
     * Realiza una petición POST a la API.
     * @param endpoint El endpoint de la API (sin la URL base)
     * @param requestBody El cuerpo de la petición
     * @param responseType La clase del tipo de respuesta esperada
     * @param <T> El tipo de la respuesta
     * @return Un HttpResponseWrapper con la respuesta
     * @throws IOException Si hay un error en la comunicación
     */
    /**
     * Sube un archivo a través de una petición multipart/form-data
     * @param <T> El tipo de la respuesta
     * @param endpoint El endpoint de la API (sin la URL base)
     * @param fileFieldName Nombre del campo del archivo en el formulario
     * @param file Archivo a subir
     * @param formFields Mapa de campos adicionales del formulario
     * @param responseType Clase del tipo de respuesta esperada
     * @return Un HttpResponseWrapper con la respuesta
     * @throws IOException Si hay un error en la comunicación
     */
    public static <T> HttpResponseWrapper<T> uploadFile(
            String endpoint,
            String fileFieldName,
            File file,
            Map<String, String> formFields,
            Class<T> responseType) throws IOException {
            
        // Verificar que el archivo existe y es legible
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IOException("El archivo no existe o no se puede leer: " + (file != null ? file.getAbsolutePath() : "null"));
        }
        
        // Verificar el tamaño del archivo (máximo 5MB)
        long maxFileSize = 5 * 1024 * 1024; // 5MB
        if (file.length() > maxFileSize) {
            throw new IOException("El archivo es demasiado grande. Tamaño máximo permitido: 5MB");
        }
        
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build()) {
                
            String cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String fullUrl = API_BASE_URL + cleanEndpoint;
            
            // Construir el cuerpo multipart
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            
            // Agregar el archivo con el tipo MIME correcto
            String mimeType = java.nio.file.Files.probeContentType(file.toPath());
            if (mimeType == null || !mimeType.startsWith("image/")) {
                // Si no se puede determinar el tipo o no es una imagen, usar un tipo genérico de imagen
                mimeType = "image/jpeg";
                
                // Verificar la extensión del archivo para un mejor manejo de tipos de imagen
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".png")) {
                    mimeType = "image/png";
                } else if (fileName.endsWith(".gif")) {
                    mimeType = "image/gif";
                } else if (fileName.endsWith(".bmp")) {
                    mimeType = "image/bmp";
                } else if (fileName.endsWith(".webp")) {
                    mimeType = "image/webp";
                }
            }
            
            FileBody fileBody = new FileBody(file, ContentType.create(mimeType), file.getName());
            builder.addPart(fileFieldName, fileBody);
            
            // Agregar campos adicionales
            if (formFields != null) {
                for (Map.Entry<String, String> entry : formFields.entrySet()) {
                    builder.addPart(entry.getKey(), 
                        new StringBody(entry.getValue(), ContentType.TEXT_PLAIN));
                }
            }
            
            HttpEntity multipart = builder.build();
            
            // Configurar la petición
            HttpPost request = new HttpPost(fullUrl);
            request.setEntity(multipart);
            
            // Log de depuración
            System.out.println("\n=== DETALLES DE LA PETICIÓN DE SUBIDA ===");
            System.out.println("URL: " + fullUrl);
            System.out.println("Método: POST");
            System.out.println("Tipo de contenido: multipart/form-data");
            System.out.println("Archivo: " + file.getName());
            System.out.println("Tamaño: " + file.length() + " bytes");
            System.out.println("Tipo MIME: " + mimeType);
            System.out.println("Campos adicionales: " + formFields);
            
            // Ejecutar la petición
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();
            
            // Log de la respuesta
            System.out.println("\n=== RESPUESTA DE SUBIDA ===");
            System.out.println("Código de estado: " + statusCode);
            System.out.println("Cuerpo de la respuesta: " + responseBody);
            System.out.println("==========================\n");
            
            // Si hay un error, lanzar excepción con el cuerpo de la respuesta
            if (statusCode >= 400) {
                throw new IOException("Error en el servidor (" + statusCode + "): " + responseBody);
            }
            
            // Si no se espera un cuerpo de respuesta, retornar null
            if (responseType == Void.class || responseType == void.class) {
                return new HttpResponseWrapper<>(statusCode, null);
            }
            
            // Si el cuerpo está vacío, retornar null
            if (responseBody == null || responseBody.trim().isEmpty()) {
                return new HttpResponseWrapper<>(statusCode, null);
            }
            
            try {
                // Intentar parsear la respuesta como JSON
                T responseObj = gson.fromJson(responseBody, responseType);
                return new HttpResponseWrapper<>(statusCode, responseObj);
            } catch (com.google.gson.JsonSyntaxException e) {
                // Si no es JSON válido, retornarlo como String si es lo que se espera
                if (responseType == String.class) {
                    @SuppressWarnings("unchecked")
                    T result = (T) responseBody;
                    return new HttpResponseWrapper<>(statusCode, result);
                }
                // De lo contrario, lanzar excepción con más contexto
                throw new IOException("La respuesta del servidor no es un JSON válido. Respuesta: " + responseBody, e);
            }
        } catch (IOException e) {
            System.err.println("Error en uploadFile: " + e.getMessage());
            throw e; // Relanzar la excepción para que el llamador la maneje
        } catch (Exception e) {
            System.err.println("Error inesperado en uploadFile: " + e.getMessage());
            throw new IOException("Error inesperado al subir el archivo: " + e.getMessage(), e);
        }
    }
    
    public static <T> HttpResponseWrapper<T> put(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        // Configurar el cliente HTTP para seguir redirecciones
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // Sigue redirecciones 301, 302, 303, 307, 308
                .build()) {
            // Eliminar la barra inicial del endpoint si existe para evitar doble barra
            String cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String fullUrl = API_BASE_URL + cleanEndpoint;
            
            org.apache.http.client.methods.HttpPut request = new org.apache.http.client.methods.HttpPut(fullUrl);
            
            // Configurar headers
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            
            // Convertir el objeto a JSON
            String jsonBody = gson.toJson(requestBody);
            
            // Log detallado de la petición
            System.out.println("\n=== DETALLES DE LA PETICIÓN ===");
            System.out.println("URL: " + fullUrl);
            System.out.println("Método: PUT");
            System.out.println("Headers:");
            System.out.println("  Content-Type: application/json");
            System.out.println("  Accept: application/json");
            System.out.println("Cuerpo de la petición (JSON):");
            System.out.println(jsonBody);
            
            // Configuración de redirecciones ya aplicada en la creación del cliente HTTP
            System.out.println("Realizando petición a: " + fullUrl);
            System.out.println("Redirecciones automáticas habilitadas");
            System.out.println("==============================\n");
            
            // Configurar el cuerpo de la petición
            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            entity.setContentEncoding("UTF-8");
            request.setEntity(entity);
            
            // Ejecutar la petición
            HttpResponse response = httpClient.execute(request);
            
            // Procesar la respuesta
            return processResponse(response, responseType);
        }
    }
    
    public static <T> HttpResponseWrapper<T> delete(String endpoint, Class<T> responseType) throws IOException {
        // Configurar el cliente HTTP para seguir redirecciones
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // Sigue redirecciones 301, 302, 303, 307, 308
                .build()) {
            // Eliminar la barra inicial del endpoint si existe para evitar doble barra
            String cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String fullUrl = API_BASE_URL + cleanEndpoint;
            
            org.apache.http.client.methods.HttpDelete request = new org.apache.http.client.methods.HttpDelete(fullUrl);
            
            // Configurar headers
            request.setHeader("Accept", "application/json");
            
            // Log detallado de la petición
            System.out.println("\n=== DETALLES DE LA PETICIÓN ===");
            System.out.println("URL: " + fullUrl);
            System.out.println("Método: DELETE");
            System.out.println("Headers:");
            System.out.println("  Accept: application/json");
            System.out.println("Realizando petición a: " + fullUrl);
            System.out.println("Redirecciones automáticas habilitadas");
            System.out.println("==============================\n");
            
            // Ejecutar la petición
            HttpResponse response = httpClient.execute(request);
            
            // Para peticiones DELETE, manejamos el caso especial de respuesta sin cuerpo
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            
            // Si no hay entidad y es una respuesta exitosa, devolvemos una respuesta exitosa sin cuerpo
            if ((entity == null || entity.getContentLength() == 0) && 
                (statusCode >= 200 && statusCode < 300)) {
                System.out.println("DELETE exitoso sin cuerpo de respuesta");
                return new HttpResponseWrapper<>(statusCode, null);
            }
            
            // Si hay una entidad o es un error, procesar normalmente
            return processResponse(response, responseType);
        }
    }
    
    public static <T> HttpResponseWrapper<T> post(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        // Configurar el cliente HTTP para seguir redirecciones
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // Sigue redirecciones 301, 302, 303, 307, 308
                .build()) {
            // Eliminar la barra inicial del endpoint si existe para evitar doble barra
            String cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String fullUrl = API_BASE_URL + cleanEndpoint;
            
            HttpPost request = new HttpPost(fullUrl);
            
            // Configurar headers
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            
            // Convertir el objeto a JSON
            String jsonBody = gson.toJson(requestBody);
            
            // Log detallado de la petición
            System.out.println("\n=== DETALLES DE LA PETICIÓN ===");
            System.out.println("URL: " + fullUrl);
            System.out.println("Método: POST");
            System.out.println("Headers:");
            System.out.println("  Content-Type: application/json");
            System.out.println("  Accept: application/json");
            System.out.println("Cuerpo de la petición (JSON):");
            System.out.println(jsonBody);
            
            // Configuración de redirecciones ya aplicada en la creación del cliente HTTP
            System.out.println("Realizando petición a: " + fullUrl);
            System.out.println("Redirecciones automáticas habilitadas");
            System.out.println("==============================\n");
            
            // Configurar el cuerpo de la petición
            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            entity.setContentEncoding("UTF-8");
            request.setEntity(entity);
            
            // Ejecutar la petición
            HttpResponse response = httpClient.execute(request);
            
            // Procesar la respuesta
            return processResponse(response, responseType);
        }
    }
    
    /**
     * Procesa la respuesta HTTP y la convierte al tipo especificado
     * @param response La respuesta HTTP
     * @param responseType El tipo de respuesta esperado
     * @param <T> El tipo de la respuesta
     * @return Un HttpResponseWrapper con la respuesta procesada
     * @throws IOException Si hay un error al procesar la respuesta
     */
    private static <T> HttpResponseWrapper<T> processResponse(HttpResponse response, Class<T> responseType) throws IOException {
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        int statusCode = response.getStatusLine().getStatusCode();
        
        // Log detallado de la respuesta
        System.out.println("\n=== DETALLES DE LA RESPUESTA ===");
        System.out.println("Código de estado: " + statusCode);
        System.out.println("Tipo de contenido: " + response.getEntity().getContentType());
        System.out.println("Longitud del contenido: " + responseBody.length() + " caracteres");
        
        // Mostrar el inicio de la respuesta para depuración
        String debugResponse = responseBody.length() > 500 ? 
            responseBody.substring(0, 500) + "..." : responseBody;
        System.out.println("Inicio de la respuesta:\n" + debugResponse);
        System.out.println("==============================\n");
        
        if (statusCode >= 400) {
            // Intentar extraer el mensaje de error del cuerpo de la respuesta
            String errorMessage = "Error en la petición HTTP: " + statusCode;
            try {
                JsonObject errorJson = JsonParser.parseString(responseBody).getAsJsonObject();
                if (errorJson.has("detail")) {
                    errorMessage = errorJson.get("detail").getAsString();
                } else if (errorJson.has("message")) {
                    errorMessage = errorJson.get("message").getAsString();
                } else if (errorJson.has("error")) {
                    errorMessage = errorJson.get("error").getAsString();
                }
            } catch (Exception e) {
                // Si no se puede parsear el error, usar el mensaje genérico
                errorMessage = "Error en la petición: " + statusCode + " - " + 
                    (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);
            }
            throw new IOException(errorMessage);
        }
        
        // Si no se espera un cuerpo de respuesta, retornar null
        if (responseType == Void.class || responseType == void.class) {
            return new HttpResponseWrapper<>(statusCode, null);
        }
        
        // Si el cuerpo está vacío, retornar null
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return new HttpResponseWrapper<>(statusCode, null);
        }
        
        // Si se espera un String, devolver el cuerpo sin procesar
        if (responseType == String.class) {
            @SuppressWarnings("unchecked") // Safe because we checked responseType == String.class
            T result = (T) responseBody;
            return new HttpResponseWrapper<>(statusCode, result);
        }
        
        try {
            // Convertir la respuesta al tipo esperado
            T responseObj = gson.fromJson(responseBody, responseType);
            return new HttpResponseWrapper<>(statusCode, responseObj);
        } catch (Exception e) {
            System.err.println("Error al convertir la respuesta a " + responseType.getSimpleName());
            System.err.println("Respuesta original: " + responseBody);
            throw new IOException("Error al procesar la respuesta del servidor: " + e.getMessage(), e);
        }
    }
}
