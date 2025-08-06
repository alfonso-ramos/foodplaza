package asedi.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Producto {
    private Long id;
    
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean disponible;
    private String categoria;
    
    @SerializedName("id_menu")
    private Long idMenu;
    
    @SerializedName("imagen_url")
    private String imagenUrl;
    
    @SerializedName("imagen_public_id")
    private String imagenPublicId;
    private Integer stock;
    
    // Constructors
    public Producto() {
        this.disponible = true; // Default value
    }
    
    public Producto(String nombre, String descripcion, Double precio, String categoria, Long idMenu) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.idMenu = idMenu;
        this.disponible = true; // Default value
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
    
    public Double getPrecio() {
        return precio;
    }
    
    public void setPrecio(Double precio) {
        if (precio != null && precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precio = precio;
    }
    
    public Boolean getDisponible() {
        return disponible;
    }
    
    public void setDisponible(Boolean disponible) {
        this.disponible = disponible != null ? disponible : true;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public Long getIdMenu() {
        return idMenu;
    }
    
    public void setIdMenu(Long idMenu) {
        this.idMenu = idMenu;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    
    public String getImagenPublicId() {
        return imagenPublicId;
    }
    
    public void setImagenPublicId(String imagenPublicId) {
        this.imagenPublicId = imagenPublicId;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id) &&
               Objects.equals(nombre, producto.nombre) &&
               Objects.equals(descripcion, producto.descripcion) &&
               Objects.equals(precio, producto.precio) &&
               Objects.equals(disponible, producto.disponible) &&
               Objects.equals(categoria, producto.categoria) &&
               Objects.equals(idMenu, producto.idMenu);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, descripcion, precio, disponible, categoria, idMenu);
    }
    
    // toString
    @Override
    public String toString() {
        return "Producto{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               ", descripcion='" + descripcion + '\'' +
               ", precio=" + precio +
               ", disponible=" + disponible +
               ", categoria='" + categoria + '\'' +
               ", idMenu=" + idMenu +
               ", imagenUrl='" + (imagenUrl != null ? "[SET]" : "null") + '\'' +
               '}';
    }
    
    // Helper methods
    public boolean isValid() {
        return nombre != null && !nombre.trim().isEmpty() &&
               descripcion != null &&
               precio != null && precio >= 0 &&
               categoria != null && !categoria.trim().isEmpty() &&
               idMenu != null && idMenu > 0;
    }
    
    public String getPrecioFormateado() {
        return String.format("$%,.2f", precio != null ? precio : 0);
    }
    
    public boolean tieneImagen() {
        return imagenUrl != null && !imagenUrl.trim().isEmpty();
    }
}
