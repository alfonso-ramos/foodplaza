package asedi.models;

public class RespuestaLogin {
    private String mensaje;
    private Usuario usuario;

    public RespuestaLogin() {}

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
