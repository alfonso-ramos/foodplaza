package asedi.services;

import asedi.model.Pedido;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PedidoService {
    private static final String API_BASE_URL = "https://foodplazaapi.ponchoramos.com/api";
    private static final Gson gson = new Gson();
    private static PedidoService instance;

    private PedidoService() {}

    public static synchronized PedidoService getInstance() {
        if (instance == null) {
            instance = new PedidoService();
        }
        return instance;
    }

    public List<Pedido> obtenerPedidosPorLocal(Long localId) throws IOException, InterruptedException {
        String url = String.format("%s/pedidos/local/%d", API_BASE_URL, localId);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HttpClientUtil.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type pedidoListType = new TypeToken<List<Pedido>>(){}.getType();
            return gson.fromJson(response.body(), pedidoListType);
        } else {
            throw new IOException("Error al obtener los pedidos: " + response.body());
        }
    }

    public void actualizarEstadoPedido(Long pedidoId, String nuevoEstado) throws IOException, InterruptedException {
        String url = String.format("%s/pedidos/%d", API_BASE_URL, pedidoId);
        
        String requestBody = String.format("{\"estado\":\"%s\"}", nuevoEstado);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HttpClientUtil.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error al actualizar el estado del pedido: " + response.body());
        }
    }

    // Mantener el método existente para retrocompatibilidad
    public Pedido crearPedido(Pedido pedido) throws IOException, InterruptedException {
        String url = API_BASE_URL + "/pedidos";
        String requestBody = gson.toJson(pedido);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HttpClientUtil.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return gson.fromJson(response.body(), Pedido.class);
        } else {
            throw new IOException("Error al crear el pedido: " + response.body());
        }
    }
}
