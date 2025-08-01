package asedi.model;

import java.util.List;

public class Local {
    private int id;
    private int plazaId;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String horarioApertura;
    private String horarioCierre;
    private String tipoComercio;
    private List<String> imagenes;
    
    // Constructor vacío
    public Local() {
    }
    
    // Constructor con parámetros
    public Local(int id, int plazaId, String nombre, String descripcion, String direccion, 
                String horarioApertura, String horarioCierre, String tipoComercio) {
        this.id = id;
        this.plazaId = plazaId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.horarioApertura = horarioApertura;
        this.horarioCierre = horarioCierre;
        this.tipoComercio = tipoComercio;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getPlazaId() {
        return plazaId;
    }
    
    public void setPlazaId(int plazaId) {
        this.plazaId = plazaId;
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
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getHorarioApertura() {
        return horarioApertura;
    }
    
    public void setHorarioApertura(String horarioApertura) {
        this.horarioApertura = horarioApertura;
    }
    
    public String getHorarioCierre() {
        return horarioCierre;
    }
    
    public void setHorarioCierre(String horarioCierre) {
        this.horarioCierre = horarioCierre;
    }
    
    public String getTipoComercio() {
        return tipoComercio;
    }
    
    public void setTipoComercio(String tipoComercio) {
        this.tipoComercio = tipoComercio;
    }
    
    public List<String> getImagenes() {
        return imagenes;
    }
    
    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }
    
    // Métodos adicionales para compatibilidad con el controlador
    public String getHorario() {
        return getHorarioApertura() + " - " + getHorarioCierre();
    }
    
    public void setHorario(String horario) {
        // Asumimos que el formato es "HH:mm - HH:mm"
        if (horario != null && horario.contains(" - ")) {
            String[] partes = horario.split(" - ");
            if (partes.length == 2) {
                setHorarioApertura(partes[0].trim());
                setHorarioCierre(partes[1].trim());
            }
        }
    }
    
    public String getUbicacion() {
        return getDireccion();
    }
    
    public void setUbicacion(String ubicacion) {
        setDireccion(ubicacion);
    }
    
    public String getEstado() {
        // Implementación por defecto, ajustar según sea necesario
        return "Activo";
    }
    
    public void setEstado(String estado) {
        // Implementación por defecto, ajustar según sea necesario
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
