package asedi.model;

import com.google.gson.annotations.SerializedName;

public class Plaza {
    private Long id;
    private String nombre;
    private String direccion;
    private String estado;
    
    @SerializedName("imagen_url")
    private String imagenUrl;
    
    @SerializedName("imagen_public_id")
    private String imagenPublicId;
    
    // Constructor vacío
    public Plaza() {
        this.estado = "activo";
    }
    
    // Constructor con parámetros
    public Plaza(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = "activo";
    }
    
    // Getters y Setters
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
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
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
    
    @Override
    public String toString() {
        return "Plaza{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
