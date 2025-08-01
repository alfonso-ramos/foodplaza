package asedi.models;

public class UsuarioRegistro {
    private String nombre;
    private String email;
    private String telefono;
    private String password;
    private String rol = "usuario";
    private String imagen_url = null;
    private String imagen_public_id = null;

    // Constructor vacío necesario para la deserialización
    public UsuarioRegistro() {}

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public String getImagen_url() {
        return imagen_url;
    }

    public String getImagen_public_id() {
        return imagen_public_id;
    }
}
