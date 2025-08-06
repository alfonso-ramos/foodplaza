package asedi.model;

import javafx.beans.property.*;

public class DetallePedido {
    private final LongProperty id = new SimpleLongProperty();
    private final ObjectProperty<Producto> producto = new SimpleObjectProperty<>();
    private final IntegerProperty cantidad = new SimpleIntegerProperty();
    private final DoubleProperty precioUnitario = new SimpleDoubleProperty();
    private final StringProperty notas = new SimpleStringProperty();

    public DetallePedido() {
        // Constructor vacío necesario para la deserialización
    }

    public DetallePedido(Producto producto, int cantidad, double precioUnitario) {
        this.producto.set(producto);
        this.cantidad.set(cantidad);
        this.precioUnitario.set(precioUnitario);
    }

    // Getters y setters para propiedades JavaFX
    
    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public Producto getProducto() {
        return producto.get();
    }

    public ObjectProperty<Producto> productoProperty() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto.set(producto);
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad.set(cantidad);
    }

    public double getPrecioUnitario() {
        return precioUnitario.get();
    }

    public DoubleProperty precioUnitarioProperty() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario.set(precioUnitario);
    }

    public String getNotas() {
        return notas.get();
    }

    public StringProperty notasProperty() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas.set(notas);
    }

    // Método de conveniencia para calcular el subtotal
    public double getSubtotal() {
        return getCantidad() * getPrecioUnitario();
    }
}
