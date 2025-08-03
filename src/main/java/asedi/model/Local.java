package asedi.model;

import com.google.gson.annotations.SerializedName;

public class Local {
    private Long id;
    
    @SerializedName("plaza_id")
    private Long plazaId;
    
    private String nombre;
    private String descripcion;
    private String direccion;
    
    @SerializedName("horario_apertura")
    private String horarioApertura;
    
    @SerializedName("horario_cierre")
    private String horarioCierre;
    
    @SerializedName("tipo_comercio")
    private String tipoComercio;
    
    private String estado;
    
    @SerializedName("imagen_url")
    private String imagenUrl;
    
    @SerializedName("imagen_public_id")
    private String imagenPublicId;
    
    @SerializedName("id_gerente")
    private Long idGerente;
    
    // Constructor vacío
    public Local() {
    }
    
    // Constructor de copia
    public Local(Local otro) {
        if (otro != null) {
            this.id = otro.id;
            this.plazaId = otro.plazaId;
            this.nombre = otro.nombre;
            this.descripcion = otro.descripcion;
            this.direccion = otro.direccion;
            this.horarioApertura = otro.horarioApertura;
            this.horarioCierre = otro.horarioCierre;
            this.tipoComercio = otro.tipoComercio;
            this.estado = otro.estado;
            this.imagenUrl = otro.imagenUrl;
            this.imagenPublicId = otro.imagenPublicId;
            this.idGerente = otro.idGerente;
        }
    }
    
    // Constructor con parámetros básicos (para compatibilidad)
    public Local(Long id, Long plazaId, String nombre, String descripcion, String direccion, 
                String horarioApertura, String horarioCierre, String tipoComercio) {
        this(id, plazaId, nombre, descripcion, direccion, horarioApertura, horarioCierre, 
             tipoComercio, "activo", null, null, null);
    }
    
    // Constructor completo
    public Local(Long id, Long plazaId, String nombre, String descripcion, String direccion, 
                String horarioApertura, String horarioCierre, String tipoComercio, 
                String estado, String imagenUrl, String imagenPublicId, Long idGerente) {
        this.id = id;
        this.plazaId = plazaId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.horarioApertura = horarioApertura;
        this.horarioCierre = horarioCierre;
        this.tipoComercio = tipoComercio;
        this.estado = estado != null ? estado : "activo";
        this.imagenUrl = imagenUrl;
        this.imagenPublicId = imagenPublicId;
        this.idGerente = idGerente;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPlazaId() {
        return plazaId;
    }
    
    public void setPlazaId(Long plazaId) {
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
    
    public String getEstado() {
        return estado != null ? estado : "activo";
    }

    public void setEstado(String estado) {
        this.estado = estado != null ? estado : "activo";
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

    public Long getIdGerente() {
        return idGerente;
    }

    public void setIdGerente(Long idGerente) {
        this.idGerente = idGerente;
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
    
    // Estado methods are already implemented above
    
    @Override
    public String toString() {
        return nombre;
    }
}
