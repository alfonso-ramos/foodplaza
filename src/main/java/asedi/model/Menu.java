package asedi.model;

import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Menu {
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @SerializedName("nombre_menu")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "Debe seleccionar un local")
    @SerializedName("id_local")
    private Long idLocal;
    
    @SerializedName("disponible")
    private boolean disponible = true;
    
    @SerializedName("productos")
    private List<Producto> productos = new ArrayList<>();
    
    // Constructors
    public Menu() {
    }
    
    public Menu(String nombre, String descripcion, Long idLocal) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idLocal = idLocal;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Long getIdLocal() {
        return idLocal;
    }
    
    public void setIdLocal(Long idLocal) {
        this.idLocal = idLocal;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public List<Producto> getProductos() {
        return productos;
    }
    
    public void setProductos(List<Producto> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
    }
    
    public void agregarProducto(Producto producto) {
        if (producto != null && !contieneProducto(producto.getId())) {
            productos.add(producto);
        }
    }
    
    public void quitarProducto(Producto producto) {
        if (producto != null) {
            productos.removeIf(p -> p.getId().equals(producto.getId()));
        }
    }
    
    public boolean contieneProducto(Long productoId) {
        return productos.stream().anyMatch(p -> p.getId().equals(productoId));
    }
    
    public boolean tieneProductos() {
        return !productos.isEmpty();
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return Objects.equals(id, menu.id) &&
               Objects.equals(nombre, menu.nombre) &&
               Objects.equals(descripcion, menu.descripcion) &&
               Objects.equals(idLocal, menu.idLocal);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, descripcion, idLocal);
    }
    
    // toString
    @Override
    public String toString() {
        return "Menu{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               ", descripcion='" + descripcion + '\'' +
               ", idLocal=" + idLocal +
               '}';
    }
    
    // Helper methods
    public boolean isValid() {
        return nombre != null && !nombre.trim().isEmpty() &&
               descripcion != null &&
               idLocal != null && idLocal > 0 &&
               productos != null;
    }
}
