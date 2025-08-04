package asedi.model;

public class Usuario {
    // Clase interna Local para la relación con el local asignado
    public static class Local {
        private Long id;
        private String nombre;
        private String direccion;

        public Local() {}

        public Local(Long id, String nombre, String direccion) {
            this.id = id;
            this.nombre = nombre;
            this.direccion = direccion;
        }

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
    }

    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String rol;
    private String imagenUrl;
    private Long idLocalAsignado; // Mantenido para compatibilidad
    private Local local; // Referencia al objeto Local asignado
    private Long plazaId; // ID de la plaza asignada

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        actualizarNombreCompleto();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
        actualizarNombreCompleto();
    }

    private void actualizarNombreCompleto() {
        if (nombre != null && apellido != null) {
            this.nombreCompleto = nombre + " " + apellido;
        } else if (nombre != null) {
            this.nombreCompleto = nombre;
        } else if (apellido != null) {
            this.nombreCompleto = apellido;
        } else {
            this.nombreCompleto = "";
        }
    }

    public String getNombreCompleto() {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            actualizarNombreCompleto();
        }
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
        // Si se establece directamente el nombre completo, intentamos dividirlo
        if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
            String[] partes = nombreCompleto.trim().split("\\s+", 2);
            this.nombre = partes[0];
            this.apellido = partes.length > 1 ? partes[1] : "";
        }
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Long getIdLocalAsignado() {
        return local != null ? local.getId() : idLocalAsignado;
    }

    public void setIdLocalAsignado(Long idLocalAsignado) {
        this.idLocalAsignado = idLocalAsignado;
        if (local == null) {
            local = new Local();
        }
        local.setId(idLocalAsignado);
    }
    
    public Local getLocal() {
        if (local == null && idLocalAsignado != null) {
            local = new Local(idLocalAsignado, null, null);
        }
        return local;
    }
    
    public void setLocal(Local local) {
        this.local = local;
        if (local != null) {
            this.idLocalAsignado = local.getId();
        }
    }
    
    public Long getPlazaId() {
        return plazaId;
    }
    
    public void setPlazaId(Long plazaId) {
        this.plazaId = plazaId;
    }
}
