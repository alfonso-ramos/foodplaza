package asedi.services;

import asedi.models.User;
import asedi.models.UsuarioRegistro;
import asedi.utils.HttpClientUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static AuthService instance;
    private final Map<String, User> users = new HashMap<>();
    private User currentUser;

    private AuthService() {
        // Initialize with some test users
        initializeTestUsers();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    private void initializeTestUsers() {
        // Admin user
        users.put("admin@foodplaza.com", new User("admin@foodplaza.com", "admin123", "Administrador Principal", User.UserRole.ADMIN));
        
        // Manager user
        users.put("gerente@foodplaza.com", new User("gerente@foodplaza.com", "gerente123", "Gerente de Plaza", User.UserRole.MANAGER));
        
        // Regular user
        users.put("usuario@foodplaza.com", new User("usuario@foodplaza.com", "usuario123", "Usuario Regular", User.UserRole.USER));
    }

    public boolean login(String email, String password) {
        User user = users.get(email);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
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
        try {
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
            
            // Realizar la petición POST a la API
            HttpClientUtil.HttpResponseWrapper<JsonObject> response = 
                HttpClientUtil.post("/usuarios/", usuario, JsonObject.class);
            
            // Verificar que el código de estado sea 201 (CREATED)
            if (response.getStatusCode() == 201) {
                System.out.println("Usuario registrado exitosamente");
                return true;
            } else {
                // Si el código no es 201, intentar extraer el mensaje de error
                String errorMsg = "Error en el registro: Código de estado inesperado " + response.getStatusCode();
                if (response.getBody() != null) {
                    if (response.getBody().has("detail")) {
                        // Manejar errores de validación
                        if (response.getBody().get("detail").isJsonArray()) {
                            errorMsg = response.getBody().getAsJsonArray("detail")
                                .get(0).getAsJsonObject()
                                .get("msg").getAsString();
                        } else if (response.getBody().has("message")) {
                            errorMsg = response.getBody().get("message").getAsString();
                        }
                    } else if (response.getBody().has("message")) {
                        errorMsg = response.getBody().get("message").getAsString();
                    }
                }
                throw new IOException(errorMsg);
            }
            
        } catch (IOException e) {
            // Si el error es 422 (validación fallida), lanzamos una excepción con el mensaje de error
            if (e.getMessage().contains("422")) {
                try {
                    String errorMessage = "Error en el registro: ";
                    String message = e.getMessage();
                    
                    // Extraer solo la parte del JSON del mensaje de error
                    int jsonStart = message.indexOf('{');
                    if (jsonStart >= 0) {
                        String jsonResponse = message.substring(jsonStart);
                        try {
                            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                            
                            if (jsonObject.has("detail") && jsonObject.get("detail").isJsonArray() 
                                    && jsonObject.getAsJsonArray("detail").size() > 0) {
                                errorMessage += jsonObject.getAsJsonArray("detail")
                                        .get(0).getAsJsonObject()
                                        .get("msg").getAsString();
                                throw new IOException(errorMessage);
                            }
                        } catch (Exception jsonError) {
                            // Si hay error al parsear, continuamos con el mensaje genérico
                            System.err.println("Error al parsear respuesta de error: " + jsonError.getMessage());
                        }
                    }
                    
                    // Mensaje genérico si no se pudo extraer un mensaje más específico
                    throw new IOException(errorMessage + "Datos inválidos o usuario ya existente");
                    
                } catch (Exception jsonError) {
                    // Si hay algún error al procesar el error, lanzamos el error original
                    throw e;
                }
            }
            // Para otros errores, relanzar la excepción
            throw e;
        }
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    public boolean isManager() {
        return currentUser != null && currentUser.getRole() == User.UserRole.MANAGER;
    }

    public boolean isUser() {
        return currentUser != null && currentUser.getRole() == User.UserRole.USER;
    }
}
