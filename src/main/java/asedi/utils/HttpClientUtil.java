package asedi.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    
    @SuppressWarnings("unchecked")
    public static <T> HttpResponseWrapper<T> post(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
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
            System.out.println("==============================\n");
            
            // Configurar el cuerpo de la petición
            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            entity.setContentEncoding("UTF-8");
            request.setEntity(entity);
            
            // Ejecutar la petición
            HttpResponse response = httpClient.execute(request);
            
            // Procesar la respuesta
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
                return new HttpResponseWrapper<>(statusCode, (T) responseBody);
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
}
