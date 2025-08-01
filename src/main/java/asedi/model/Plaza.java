package asedi.model;

import java.util.List;

public class Plaza {
    private int id;
    private String nombre;
    private String direccion;
    private String descripcion;
    private List<String> imagenes;
    
    // Constructor vacío
    public Plaza() {
    }
    
    // Constructor con parámetros
    public Plaza(int id, String nombre, String direccion, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
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
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<String> getImagenes() {
        return imagenes;
    }
    
    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
