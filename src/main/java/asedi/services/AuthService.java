package asedi.services;

import asedi.models.Usuario;
import asedi.models.UsuarioRegistro;
import asedi.models.RespuestaLogin;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static AuthService instance;
    private Usuario currentUser;
    private final Gson gson = new Gson();

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Inicia sesión con las credenciales proporcionadas
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return Respuesta del servidor con los datos del usuario
     * @throws IOException Si hay un error de red o las credenciales son inválidas
     */
    @SuppressWarnings("unchecked")
    public RespuestaLogin login(String email, String password) throws IOException {
        try {
            // Validar campos
            if (email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                throw new IOException("Por favor ingrese su correo y contraseña");
            }

            // Crear objeto con credenciales
            Map<String, String> credenciales = new HashMap<>();
            credenciales.put("email", email);
            credenciales.put("password", password);

            System.out.println("=== INICIO DE SOLICITUD DE INICIO DE SESIÓN ===");
            System.out.println("Email: " + email);
            
            // Realizar petición
            System.out.println("Realizando petición a /usuarios/login...");
            String responseBody = null;
            
            try {
                HttpClientUtil.HttpResponseWrapper<String> response = 
                    HttpClientUtil.post("/usuarios/login", credenciales, String.class);
                
                responseBody = response.getBody();
                System.out.println("=== RESPUESTA DEL SERVIDOR ===");
                System.out.println("Código de estado: " + response.getStatusCode());
                System.out.println("Respuesta: " + responseBody);
                
                // Verificar si la respuesta está vacía
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    throw new IOException("El servidor devolvió una respuesta vacía");
                }
                
                // Intentar parsear la respuesta
                return parseLoginResponse(responseBody);
                
            } catch (Exception e) {
                System.err.println("Error en la petición: " + e.getMessage());
                if (responseBody != null) {
                    System.err.println("Respuesta del servidor: " + responseBody);
                }
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error en login: " + e);
            throw new IOException("Error al iniciar sesión: " + e.getMessage(), e);
        }
    }
    
    private RespuestaLogin parseLoginResponse(String responseBody) throws IOException {
        try {
            // Primero intentar parsear como JSON
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            
            // Si es un objeto, intentar extraer la información
            if (jsonElement.isJsonObject()) {
                JsonObject jsonResponse = jsonElement.getAsJsonObject();
                
                // Verificar si hay un mensaje de error
                if (jsonResponse.has("detail")) {
                    String errorMsg = jsonResponse.get("detail").getAsString();
                    throw new IOException(errorMsg);
                }
                
                // Verificar si la respuesta tiene la estructura esperada
                if (jsonResponse.has("mensaje") && jsonResponse.has("usuario")) {
                    JsonElement usuarioElement = jsonResponse.get("usuario");
                    Usuario usuario = null;
                    
                    // Intentar parsear el usuario de diferentes maneras
                    if (usuarioElement.isJsonObject()) {
                        usuario = gson.fromJson(usuarioElement, Usuario.class);
                    } else if (usuarioElement.isJsonPrimitive() && usuarioElement.getAsJsonPrimitive().isString()) {
                        // Si el usuario viene como string JSON
                        String usuarioJson = usuarioElement.getAsString();
                        usuario = gson.fromJson(usuarioJson, Usuario.class);
                    }
                    
                    if (usuario != null) {
                        RespuestaLogin respuesta = new RespuestaLogin();
                        respuesta.setMensaje(jsonResponse.get("mensaje").getAsString());
                        respuesta.setUsuario(usuario);
                        currentUser = usuario;
                        return respuesta;
                    }
                }
                
                // Si llegamos aquí, intentar parsear la respuesta completa como un Usuario
                try {
                    Usuario usuario = gson.fromJson(jsonResponse, Usuario.class);
                    RespuestaLogin respuesta = new RespuestaLogin();
                    respuesta.setMensaje("Inicio de sesión exitoso");
                    respuesta.setUsuario(usuario);
                    currentUser = usuario;
                    return respuesta;
                } catch (Exception e) {
                    // Ignorar y continuar con el siguiente intento
                }
            }
            
            // Si llegamos aquí, no se pudo parsear la respuesta
            throw new IOException("Formato de respuesta no reconocido: " + responseBody);
            
        } catch (Exception e) {
            System.err.println("Error al parsear la respuesta del servidor: " + e.getMessage());
            System.err.println("Respuesta original: " + responseBody);
            throw new IOException("Error al procesar la respuesta del servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * @return El usuario actual o null si no hay sesión activa
     */
    public Usuario getCurrentUser() {
        return currentUser;
    }

    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public boolean estaAutenticado() {
        return currentUser != null;
    }

    /**
     * Verifica si el usuario actual tiene el rol especificado
     * @param rol Rol a verificar (admin, gerente, usuario, etc.)
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean tieneRol(String rol) {
        return estaAutenticado() && 
               rol != null && 
               rol.equalsIgnoreCase(currentUser.getRol());
    }
    
    /**
     * Registra un nuevo usuario en la API
     * @param nombre Nombre completo del usuario
     * @param email Correo electrónico del usuario
     * @param telefono Número de teléfono (opcional)
     * @param password Contraseña del usuario
     * @return true si el registro fue exitoso, false en caso contrario
     * @throws IOException Si ocurre un error en la comunicación con la API
     */
    public boolean registrarUsuario(String nombre, String email, String telefono, String password) throws IOException {
        // Crear el objeto de registro
        UsuarioRegistro usuario = new UsuarioRegistro();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setPassword(password);
        
        // Imprimir datos que se enviarán
        System.out.println("Datos a enviar a la API:");
        System.out.println("Nombre: " + nombre);
        System.out.println("Email: " + email);
        System.out.println("Teléfono: " + telefono);
        
        try {
            // Realizar la petición POST a la API
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.post("/usuarios/", usuario, String.class);
            
            System.out.println("Código de estado: " + response.getStatusCode());
            System.out.println("Respuesta del servidor: " + response.getBody());
            
            // Verificar que el código de estado sea 201 (CREATED)
            if (response.getStatusCode() == 201) {
                System.out.println("Usuario registrado exitosamente");
                return true;
            }
            
            // Si llegamos aquí, hubo un error
            String errorMsg = "Error en el registro: ";
            
            try {
                // Intentar parsear la respuesta como JSON
                JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
                
                if (jsonResponse.has("detail")) {
                    if (jsonResponse.get("detail").isJsonArray()) {
                        // Manejar errores de validación
                        errorMsg = jsonResponse.getAsJsonArray("detail")
                            .get(0).getAsJsonObject()
                            .get("msg").getAsString();
                    } else {
                        errorMsg = jsonResponse.get("detail").getAsString();
                    }
                } else if (jsonResponse.has("message")) {
                    errorMsg = jsonResponse.get("message").getAsString();
                } else {
                    errorMsg += "Error desconocido del servidor";
                }
            } catch (Exception e) {
                // Si no se puede parsear como JSON, usar el cuerpo de la respuesta como mensaje
                errorMsg = response.getBody() != null ? response.getBody() : "Error desconocido";
            }
            
            throw new IOException(errorMsg);
            
        } catch (IOException e) {
            System.err.println("Error en registrarUsuario: " + e.getMessage());
            
            // Mejorar el mensaje de error para el usuario
            String errorMessage = "Error al registrar el usuario. ";
            
            if (e.getMessage().contains("422")) {
                errorMessage += "Los datos proporcionados no son válidos. ";
                
                try {
                    // Extraer el mensaje de validación del detalle del error
                    String message = e.getMessage();
                    int jsonStart = message.indexOf('{');
                    if (jsonStart >= 0) {
                        String jsonResponse = message.substring(jsonStart);
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                        
                        if (jsonObject.has("detail") && jsonObject.get("detail").isJsonArray() 
                                && jsonObject.getAsJsonArray("detail").size() > 0) {
                            errorMessage = jsonObject.getAsJsonArray("detail")
                                    .get(0).getAsJsonObject()
                                    .get("msg").getAsString();
                        }
                    }
                } catch (Exception jsonError) {
                    System.err.println("Error al parsear respuesta de error: " + jsonError.getMessage());
                }
            } else if (e.getMessage().contains("500")) {
                errorMessage = "Error interno del servidor. Por favor, inténtelo de nuevo más tarde.";
            } else if (e.getMessage().contains("409")) {
                errorMessage = "El correo electrónico ya está registrado. Por favor, utilice otro correo o inicie sesión.";
            } else if (e.getMessage().contains("timed out")) {
                errorMessage = "Tiempo de espera agotado. Por favor, verifique su conexión a internet e inténtelo de nuevo.";
            }
            
            throw new IOException(errorMessage, e);
        }
    }

    // Los métodos isAdmin(), isManager() e isUser() han sido eliminados
    // ya que no se utilizan y causan errores de tipo.
    // Se recomienda usar tieneRol("admin"), tieneRol("gerente"), tieneRol("usuario") en su lugar.
}
