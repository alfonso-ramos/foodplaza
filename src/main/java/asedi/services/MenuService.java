package asedi.services;

import asedi.model.Menu;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MenuService {
    private static final String ENDPOINT = "menus";
    private final Gson gson = new Gson();
    
    // Simple in-memory cache
    private final Map<Long, Menu> cache = new ConcurrentHashMap<>();
    private List<Menu> allMenusCache = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Obtiene todos los menús disponibles.
     * @return Lista de menús
     * @throws IOException Si hay un error de conexión
     */
    public List<Menu> obtenerTodos() throws IOException {
        return obtenerPorLocal(null);
    }
    
    /**
     * Obtiene los menús de un local específico.
     * @param idLocal ID del local (opcional, si es null obtiene todos los menús)
     * @return Lista de menús del local especificado
     * @throws IOException Si hay un error de conexión
     */
    public List<Menu> obtenerPorLocal(Long idLocal) throws IOException {
        long currentTime = System.currentTimeMillis();
        
        // Si se solicita un local específico, verificar la caché
        if (idLocal != null && allMenusCache != null && (currentTime - lastCacheUpdate) < CACHE_DURATION_MS) {
            List<Menu> filteredMenus = new ArrayList<>();
            for (Menu menu : allMenusCache) {
                if (menu != null && menu.getIdLocal() != null && menu.getIdLocal().equals(idLocal)) {
                    filteredMenus.add(menu);
                }
            }
            if (!filteredMenus.isEmpty()) {
                return new ArrayList<>(filteredMenus);
            }
        }
        
        try {
            // Construir la URL con el formato correcto: menus/local/{local_id}
            String url = ENDPOINT;
            if (idLocal != null) {
                url = String.format("%s/local/%d", ENDPOINT, idLocal);
            }
            
            System.out.println("Solicitando menús desde: " + url);
            
            // Obtener la respuesta del servidor
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.get(url, String.class);
            
            System.out.println("Respuesta del servidor recibida. Estado: " + response.getStatusCode());
            
            // Verificar si la respuesta es exitosa (código 200)
            if (response.getStatusCode() != 200) {
                throw new IOException("Error al obtener menús. Código: " + response.getStatusCode());
            }
            
            // Obtener el cuerpo de la respuesta
            String responseBody = response.getBody();
            
            // Verificar si la respuesta está vacía
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.out.println("La respuesta del servidor está vacía");
                return new ArrayList<>();
            }
            
            // Intentar parsear la respuesta como un array de menús
            try {
                Type listType = new TypeToken<ArrayList<Menu>>() {}.getType();
                List<Menu> menus = gson.fromJson(responseBody, listType);
                
                if (menus != null) {
                    // Actualizar caché completa si no es una consulta filtrada
                    if (idLocal == null) {
                        allMenusCache = new ArrayList<>(menus);
                        lastCacheUpdate = currentTime;
                        
                        // Actualizar caché individual
                        cache.clear();
                        for (Menu menu : menus) {
                            if (menu != null && menu.getId() != null) {
                                cache.put(menu.getId(), menu);
                            }
                        }
                    }
                    
                    return new ArrayList<>(menus);
                }
                
                return new ArrayList<>();
                
            } catch (com.google.gson.JsonSyntaxException e) {
                // Si falla el parseo como array, intentar como objeto único
                try {
                    Menu menu = gson.fromJson(responseBody, Menu.class);
                    List<Menu> menus = new ArrayList<>();
                    if (menu != null) {
                        menus.add(menu);
                        
                        // Actualizar caché si no es una consulta filtrada
                        if (idLocal == null) {
                            allMenusCache = new ArrayList<>(menus);
                            lastCacheUpdate = currentTime;
                            
                            if (menu.getId() != null) {
                                cache.put(menu.getId(), menu);
                            }
                        }
                    }
                    return menus;
                } catch (Exception e2) {
                    System.err.println("Error al analizar la respuesta del servidor: " + e2.getMessage());
                    throw new IOException("Formato de respuesta del servidor no reconocido", e2);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener la lista de menús: " + e.getMessage());
            throw new IOException("No se pudo obtener la lista de menús: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene un menú por su ID.
     * @param id ID del menú
     * @return El menú encontrado o null si no existe
     */
    public Menu obtenerPorId(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        // Try to get from cache first
        Menu cachedMenu = cache.get(id);
        if (cachedMenu != null) {
            return cachedMenu;
        }
        
        try {
            String response = HttpClientUtil.get(ENDPOINT + "/" + id, String.class).getBody();
            return gson.fromJson(response, Menu.class);
        } catch (Exception e) {
            System.err.println("Error al obtener el menú con ID " + id + ": " + e.getMessage());
            throw new IOException("No se pudo obtener el menú", e);
        }
    }
    
    /**
     * Crea un nuevo menú.
     * @param menu El menú a crear
     * @return El menú creado con su ID asignado
     * @throws IOException Si hay un error de conexión o validación
     */
    public Menu crear(Menu menu) throws IOException {
        if (menu == null) {
            throw new IllegalArgumentException("El menú no puede ser nulo");
        }
        
        if (menu.getNombre() == null || menu.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del menú es obligatorio");
        }
        
        if (menu.getIdLocal() == null) {
            throw new IllegalArgumentException("El ID del local es obligatorio");
        }
        
        try {
            // Pass the Menu object directly - HttpClientUtil will handle the JSON serialization
            Menu menuCreado = HttpClientUtil.post(ENDPOINT, menu, Menu.class).getBody();
            
            // Update cache
            if (menuCreado != null) {
                cache.put(menuCreado.getId(), menuCreado);
                if (allMenusCache != null) {
                    allMenusCache.add(menuCreado);
                }
            }
            
            return menuCreado;
        } catch (Exception e) {
            System.err.println("Error al crear el menú: " + e.getMessage());
            throw new IOException("No se pudo crear el menú: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un menú existente.
     * @param menu El menú con los datos actualizados
     * @return true si la actualización fue exitosa
     * @throws IOException Si hay un error de conexión o validación
     */
    public boolean actualizar(Menu menu) throws IOException {
        if (menu == null || menu.getId() == null) {
            throw new IllegalArgumentException("El menú y su ID son obligatorios");
        }
        
        if (menu.getNombre() == null || menu.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del menú es obligatorio");
        }
        
        if (menu.getIdLocal() == null) {
            throw new IllegalArgumentException("El ID del local es obligatorio");
        }
        
        try {
            // Pasar el objeto Menu directamente - HttpClientUtil manejará la serialización JSON
            Menu menuActualizado = HttpClientUtil.put(ENDPOINT + "/" + menu.getId(), menu, Menu.class).getBody();
            
            // Actualizar caché
            if (menuActualizado != null) {
                cache.put(menuActualizado.getId(), menuActualizado);
                if (allMenusCache != null) {
                    allMenusCache.removeIf(m -> m.getId().equals(menuActualizado.getId()));
                    allMenusCache.add(menuActualizado);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error al actualizar el menú " + menu.getId() + ": " + e.getMessage());
            throw new IOException("No se pudo actualizar el menú", e);
        }
    }
    
    /**
     * Elimina un menú por su ID.
     * @param id ID del menú a eliminar
     * @return true si la eliminación fue exitosa
     * @throws IOException Si hay un error de conexión
     */
    public boolean eliminar(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        try {
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.delete(ENDPOINT + "/" + id, String.class);
            
            // Invalidate caches
            cache.remove(id);
            if (allMenusCache != null) {
                allMenusCache.removeIf(menu -> id.equals(menu.getId()));
            }
            
            return response.getStatusCode() == 200 || response.getStatusCode() == 204 || 
                   (response.getBody() != null && response.getBody().contains("true"));
        } catch (Exception e) {
            System.err.println("Error al eliminar el menú " + id + ": " + e.getMessage());
            throw new IOException("No se pudo eliminar el menú", e);
        }
    }
    

    
    /**
     * Agrega un producto a un menú.
     * @param idMenu ID del menú
     * @param idProducto ID del producto a agregar
     * @return true si la operación fue exitosa
     * @throws IOException Si hay un error de conexión
     */
    public boolean agregarProductoAMenu(Long idMenu, Long idProducto) throws IOException {
        try {
            String url = String.format("%s/%d/productos/%d", ENDPOINT, idMenu, idProducto);
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.post(url, "", String.class);
            return response.getStatusCode() == 200 || response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al agregar producto al menú: " + e.getMessage());
            throw new IOException("No se pudo agregar el producto al menú", e);
        }
    }
    
    /**
     * Elimina un producto de un menú.
     * @param idMenu ID del menú
     * @param idProducto ID del producto a eliminar
     * @return true si la operación fue exitosa
     * @throws IOException Si hay un error de conexión
     */
    public boolean eliminarProductoDeMenu(Long idMenu, Long idProducto) throws IOException {
        try {
            String url = String.format("%s/%d/productos/%d", ENDPOINT, idMenu, idProducto);
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.delete(url, String.class);
            return response.getStatusCode() == 200 || response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al eliminar producto del menú: " + e.getMessage());
            throw new IOException("No se pudo eliminar el producto del menú", e);
        }
    }
    
    /**
     * Actualiza la disponibilidad de un menú.
     * @param idMenu ID del menú
     * @param disponible true para marcar como disponible, false en caso contrario
     * @return true si la operación fue exitosa
     * @throws IOException Si hay un error de conexión
     */
    public boolean actualizarDisponibilidad(Long idMenu, boolean disponible) throws IOException {
        try {
            String url = String.format("%s/%d/disponibilidad", ENDPOINT, idMenu);
            String body = String.format("{\"disponible\":%b}", disponible);
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.put(url, body, String.class);
            return response.getStatusCode() == 200 || response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al actualizar disponibilidad del menú: " + e.getMessage());
            throw new IOException("No se pudo actualizar la disponibilidad del menú", e);
        }
    }
    
    /**
     * Fuerza la actualización de la caché.
     */
    public void invalidarCache() {
        allMenusCache = null;
        cache.clear();
        lastCacheUpdate = 0;
    }
}
