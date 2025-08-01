package asedi.utils;

import com.google.gson.Gson;
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
    
    public static <T> HttpResponseWrapper<T> post(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_BASE_URL + endpoint);
            
            // Configurar headers
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            
            // Convertir el objeto a JSON
            String jsonBody = gson.toJson(requestBody);
            System.out.println("Enviando petición a " + API_BASE_URL + endpoint);
            System.out.println("Cuerpo de la petición: " + jsonBody);
            
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            
            // Ejecutar la petición
            HttpResponse response = httpClient.execute(request);
            
            // Procesar la respuesta
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();
            
            System.out.println("Respuesta del servidor (" + statusCode + "): " + responseBody);
            
            if (statusCode >= 400) {
                throw new IOException("Error en la petición: " + statusCode + " - " + responseBody);
            }
            
            // Si no se espera un cuerpo de respuesta, retornar null
            if (responseType == Void.class || responseType == void.class) {
                return new HttpResponseWrapper<>(statusCode, null);
            }
            
            // Si el cuerpo está vacío, retornar null
            if (responseBody == null || responseBody.trim().isEmpty()) {
                return new HttpResponseWrapper<>(statusCode, null);
            }
            
            // Convertir la respuesta al tipo esperado
            T responseObj = gson.fromJson(responseBody, responseType);
            return new HttpResponseWrapper<>(statusCode, responseObj);
        }
    }
}
