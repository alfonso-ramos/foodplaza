package asedi.services;

import asedi.model.Usuario;

/**
 * Servicio para manejar la sesión del usuario actual.
 * Implementa el patrón Singleton para asegurar una única instancia.
 */

/**
 * Servicio para manejar la sesión del usuario actual.
 * Implementa el patrón Singleton para asegurar una única instancia.
 */
public class SessionService {
    private static SessionService instance;
    private Usuario usuarioActual;
    private String authToken;

    private SessionService() {
        // Constructor privado para evitar instanciación directa
    }

    /**
     * Obtiene la instancia única del servicio de sesión.
     * @return La instancia de SessionService
     */
    public static synchronized SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    /**
     * Establece el usuario actual en la sesión.
     * @param usuario El usuario que ha iniciado sesión
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    /**
     * Obtiene el usuario actual de la sesión.
     * @return El usuario actual o null si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public boolean isSesionActiva() {
        return usuarioActual != null && authToken != null && !authToken.isEmpty();
    }
    
    /**
     * Establece el token de autenticación para la sesión actual.
     * @param token El token JWT recibido del servidor
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /**
     * Obtiene el token de autenticación actual.
     * @return El token JWT o null si no hay sesión activa
     */
    public String getAuthToken() {
        return authToken;
    }
    
    /**
     * Cierra la sesión actual, eliminando el usuario y el token.
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
        this.authToken = null;
    }

}
