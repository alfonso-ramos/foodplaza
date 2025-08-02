package asedi.models;

public class Plaza {
    private String nombre;
    private String direccion;
    private String estado;

    public Plaza() {
        this.estado = "activo";
    }

    public Plaza(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = "activo";
    }

    // Getters y Setters
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
}
