package asedi.services;

import asedi.model.Usuario;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class UsuarioService {
    private static final String ENDPOINT = "usuarios";
    private final Gson gson = new Gson();
    
    /**
     * Busca un usuario por su correo electrónico.
     * @param email Correo electrónico del usuario a buscar
     * @return Usuario encontrado o null si no se encuentra
     */
    /**
     * Busca un usuario por su correo electrónico.
     * @param email Correo electrónico del usuario a buscar
     * @return Usuario encontrado o null si no se encuentra
     */
    public Usuario buscarPorEmail(String email) {
        System.out.println("\n=== Iniciando búsqueda de usuario ===");
        System.out.println("Email: " + email);
        
        try {
            // Construir la URL de búsqueda según el formato del ejemplo
            String url = String.format("%s/buscar/?email=%s", ENDPOINT, email);
            System.out.println("\nURL de búsqueda: " + url);
            
            // Hacer la petición GET al endpoint de búsqueda
            System.out.println("\nEnviando solicitud GET...");
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.get(url, String.class);
                
            System.out.println("\n=== Respuesta del servidor ===");
            System.out.println("Código de estado: " + response.getStatusCode());
            
            // Si el endpoint de búsqueda no existe (404), intentamos listar todos los usuarios
            if (response.getStatusCode() == 404) {
                System.out.println("\nEndpoint de búsqueda no encontrado (404), intentando listar todos los usuarios...");
                return buscarEnListaUsuarios(email);
            }
            
            if (response.getStatusCode() != 200) {
                String errorMsg = "Error en la respuesta del servidor. Código: " + response.getStatusCode();
                System.err.println(errorMsg);
                return null;
            }
            
            // Si la respuesta es exitosa, intentar parsear el usuario
            System.out.println("\nProcesando respuesta exitosa...");
            String responseBody = response.getBody();
            System.out.println("Cuerpo de la respuesta: " + responseBody);
            
            try {
                Usuario usuario = gson.fromJson(responseBody, Usuario.class);
                if (usuario != null && usuario.getId() != null) {
                    System.out.println("\n=== Usuario encontrado ===");
                    System.out.println("ID: " + usuario.getId());
                    System.out.println("Email: " + usuario.getEmail());
                    System.out.println("Nombre: " + usuario.getNombreCompleto());
                    return usuario;
                } else {
                    System.out.println("\nEl usuario no fue encontrado o los datos son inválidos");
                    return null;
                }
            } catch (Exception e) {
                System.err.println("\nError al procesar la respuesta del servidor:");
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            String errorMsg = "Error en buscarPorEmail: " + e.getClass().getName() + " - " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Método alternativo para buscar un usuario en la lista de todos los usuarios
     * cuando el endpoint de búsqueda no está disponible.
     */
    private Usuario buscarEnListaUsuarios(String email) {
        System.out.println("Buscando usuario en la lista de todos los usuarios...");
        try {
            // Obtener todos los usuarios
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.get(ENDPOINT, String.class);
                
            if (response.getStatusCode() == 200) {
                // Parsear la lista de usuarios
                Usuario[] usuarios = gson.fromJson(response.getBody(), Usuario[].class);
                if (usuarios != null) {
                    // Buscar el usuario por email (case-insensitive)
                    for (Usuario usuario : usuarios) {
                        if (usuario.getEmail() != null && 
                            usuario.getEmail().equalsIgnoreCase(email)) {
                            System.out.println("Usuario encontrado en la lista: " + usuario.getEmail());
                            return usuario;
                        }
                    }
                }
                System.out.println("Usuario no encontrado en la lista");
                return null;
            } else {
                String errorMsg = "Error al obtener la lista de usuarios. Código: " + 
                                response.getStatusCode() + ", Respuesta: " + response.getBody();
                System.err.println(errorMsg);
                return null;
            }
        } catch (Exception e) {
            String errorMsg = "Error en buscarEnListaUsuarios: " + e.getClass().getName() + " - " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Asigna el rol de gerente a un usuario.
     * @param idUsuario ID del usuario a asignar como gerente
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public boolean asignarRolGerente(Long idUsuario) {
        System.out.println("Iniciando asignación de rol gerente para usuario ID: " + idUsuario);
        try {
            String url = String.format("%s/%d", ENDPOINT, idUsuario);
            System.out.println("URL de la petición: " + url);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("rol", "gerente");
            
            System.out.println("Cuerpo de la petición: " + gson.toJson(requestBody));
            
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.put(url, requestBody, String.class);
                
            System.out.println("Respuesta del servidor - Código: " + response.getStatusCode());
            System.out.println("Cuerpo de la respuesta: " + response.getBody());
                
            boolean exito = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            System.out.println("Asignación de rol " + (exito ? "exitosa" : "fallida"));
            
            return exito;
        } catch (Exception e) {
            String errorMsg = "Error en asignarRolGerente: " + e.getClass().getName() + " - " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return false;
        }
    }
}
