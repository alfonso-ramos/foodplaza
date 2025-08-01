package asedi.model;

public class Usuario {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String rol;
    private Long idLocalAsignado; // ID del local asignado como gerente

    // Constructor vacío
    public Usuario() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getIdLocalAsignado() {
        return idLocalAsignado;
    }

    public void setIdLocalAsignado(Long idLocalAsignado) {
        this.idLocalAsignado = idLocalAsignado;
    }
}
