package asedi.services;

import asedi.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class CarritoService {

    private static CarritoService instance;
    private final List<Producto> productos = new ArrayList<>();

    private CarritoService() {}

    public static CarritoService getInstance() {
        if (instance == null) {
            instance = new CarritoService();
        }
        return instance;
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public void removerProducto(Producto producto) {
        productos.remove(producto);
    }

    public void limpiarCarrito() {
        productos.clear();
    }

    public double getTotal() {
        return productos.stream().mapToDouble(Producto::getPrecio).sum();
    }
}