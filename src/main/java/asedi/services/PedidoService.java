package asedi.services;

import asedi.model.Pedido;

import java.io.IOException;

public class PedidoService {

    public Pedido crearPedido(Pedido pedido) throws IOException {
        // TODO: Implementar la llamada a la API para crear un pedido
        System.out.println("Pedido creado (simulado): " + pedido);
        return pedido;
    }
}
