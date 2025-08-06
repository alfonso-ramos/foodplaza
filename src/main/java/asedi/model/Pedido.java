package asedi.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private final LongProperty id = new SimpleLongProperty();
    private final ObjectProperty<Usuario> cliente = new SimpleObjectProperty<>();
    private final ObjectProperty<Local> local = new SimpleObjectProperty<>();
    private final ListProperty<DetallePedido> detalles = new SimpleListProperty<>();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> fechaHora = new SimpleObjectProperty<>();
    private final StringProperty estado = new SimpleStringProperty();
    private final StringProperty direccionEntrega = new SimpleStringProperty();
    private final StringProperty notas = new SimpleStringProperty();

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

    public Usuario getCliente() {
        return cliente.get();
    }

    public ObjectProperty<Usuario> clienteObjectProperty() {
        return cliente;
    }
    
    public String getClienteNombre() {
        return cliente.get() != null ? cliente.get().getNombre() : "";
    }
    
    public StringProperty clienteProperty() {
        return new SimpleStringProperty(getClienteNombre());
    }

    public void setCliente(Usuario cliente) {
        this.cliente.set(cliente);
    }

    public Local getLocal() {
        return local.get();
    }

    public ObjectProperty<Local> localProperty() {
        return local;
    }

    public void setLocal(Local local) {
        this.local.set(local);
    }

    public List<DetallePedido> getDetalles() {
        return detalles.get();
    }

    public ListProperty<DetallePedido> detallesProperty() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles.setAll(detalles);
    }

    public double getTotal() {
        return total.get();
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public LocalDateTime getFechaHora() {
        return fechaHora.get();
    }

    public ObjectProperty<LocalDateTime> fechaHoraProperty() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora.set(fechaHora);
    }

    public String getEstado() {
        return estado.get();
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public String getDireccionEntrega() {
        return direccionEntrega.get();
    }

    public StringProperty direccionEntregaProperty() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega.set(direccionEntrega);
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
    
    // Métodos de conveniencia
    public void agregarDetalle(DetallePedido detalle) {
        this.detalles.add(detalle);
        this.total.set(this.total.get() + detalle.getSubtotal());
    }
    
    public void eliminarDetalle(DetallePedido detalle) {
        if (this.detalles.remove(detalle)) {
            this.total.set(this.total.get() - detalle.getSubtotal());
        }
    }
}
