package asedi.services;

import asedi.model.Producto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class CarritoService {
    private static CarritoService instance;
    private final Map<Producto, Integer> items = new HashMap<>();

    private CarritoService() {}

    public static CarritoService getInstance() {
        if (instance == null) {
            instance = new CarritoService();
        }
        return instance;
    }

    public void agregarProducto(Producto producto) {
        items.merge(producto, 1, Integer::sum);
    }

    public Map<Producto, Integer> getItems() {
        return new HashMap<>(items);
    }

    public void actualizarCantidad(Producto producto, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            items.remove(producto);
        } else {
            items.put(producto, nuevaCantidad);
        }
    }

    public void removerProducto(Producto producto) {
        items.remove(producto);
    }

    public void limpiarCarrito() {
        items.clear();
    }

    public double getSubtotal() {
        return items.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrecio() * entry.getValue())
                .sum();
    }

    public double getTotal() {
        return getSubtotal() * 1.16; // Incluye IVA del 16%
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public int getCantidadTotal() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public boolean enviarPedido(Long idLocal, String instrucciones) throws IOException {
        if (items.isEmpty()) {
            return false;
        }
        
        try {
            // Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();
            
            // Construir el cuerpo de la petición
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id_local", idLocal);
            requestBody.put("instrucciones_especiales", instrucciones);
            
            // Crear lista de items
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (Map.Entry<Producto, Integer> entry : items.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id_producto", entry.getKey().getId());
                item.put("cantidad", entry.getValue());
                item.put("precio_unitario", entry.getKey().getPrecio());
                item.put("instrucciones_especiales", "");
                itemsList.add(item);
            }
            requestBody.put("items", itemsList);
            
            // Convertir a JSON
            String requestBodyJson = new Gson().toJson(requestBody);
            
            // Crear y enviar la petición
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pedidos/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            // Enviar y obtener respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Si la petición fue exitosa, limpiar el carrito
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                limpiarCarrito();
                return true;
            }
            
            throw new IOException("Error en el servidor: " + response.statusCode() + " - " + response.body());
            
        } catch (Exception e) {
            throw new IOException("Error al enviar el pedido: " + e.getMessage(), e);
        }
    }
}