package asedi.services;

import asedi.model.Menu;
import asedi.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MenuService {
    private static final String BASE_ENDPOINT = "menus";
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
        // Verificar caché primero
        if (allMenusCache != null && (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION_MS) {
            if (idLocal == null) {
                return new ArrayList<>(allMenusCache);
            }
            return allMenusCache.stream()
                    .filter(menu -> menu.getIdLocal() != null && menu.getIdLocal().equals(idLocal))
                    .collect(Collectors.toList());
        }
        
        try {
            // Construir la URL con el formato correcto: /api/menus/local/{local_id}
            String url;
            if (idLocal != null) {
                url = String.format("menus/local/%d", idLocal);
            } else {
                // Si no hay ID de local, obtener todos los menús
                url = "menus";
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
            
            long currentTime = System.currentTimeMillis();
            
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
            String url = String.format("%s/%d", BASE_ENDPOINT, id);
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.get(url, String.class);
            
            if (response.getStatusCode() == 200) {
                Menu menu = gson.fromJson(response.getBody(), Menu.class);
                if (menu != null) {
                    cache.put(menu.getId(), menu);
                }
                return menu;
            } else if (response.getStatusCode() == 404) {
                return null; // Menú no encontrado
            } else {
                throw new IOException("Error al obtener el menú. Código: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IOException("Error al obtener el menú: " + e.getMessage(), e);
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
            String url = BASE_ENDPOINT;
            String json = gson.toJson(menu);
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.post(url, json, String.class);
            
            if (response.getStatusCode() == 201) {
                Menu menuCreado = gson.fromJson(response.getBody(), Menu.class);
                if (menuCreado != null) {
                    // Actualizar caché
                    if (allMenusCache != null) {
                        allMenusCache.add(menuCreado);
                    }
                    cache.put(menuCreado.getId(), menuCreado);
                }
                return menuCreado;
            } else {
                throw new IOException("Error al crear el menú. Código: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IOException("Error al crear el menú: " + e.getMessage(), e);
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
            String url = String.format("%s/%d", BASE_ENDPOINT, menu.getId());
            
            // Create a map with the exact field names expected by the API
            Map<String, Object> menuMap = new HashMap<>();
            menuMap.put("id", menu.getId());
            menuMap.put("nombre_menu", menu.getNombre());
            menuMap.put("descripcion", menu.getDescripcion());
            menuMap.put("id_local", menu.getIdLocal());
            menuMap.put("disponible", menu.isDisponible());
            menuMap.put("productos", new ArrayList<>());
            
            // The HttpClientUtil will handle the JSON serialization
            HttpClientUtil.HttpResponseWrapper<String> response = HttpClientUtil.put(url, menuMap, String.class);
            
            if (response.getStatusCode() == 200) {
                // Actualizar caché
                cache.put(menu.getId(), menu);
                if (allMenusCache != null) {
                    allMenusCache.removeIf(m -> m.getId().equals(menu.getId()));
                    allMenusCache.add(menu);
                }
                return true;
            } else {
                throw new IOException("Error al actualizar el menú. Código: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IOException("Error al actualizar el menú: " + e.getMessage(), e);
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
                HttpClientUtil.delete(String.format("%s/%d", BASE_ENDPOINT, id), String.class);
            
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
            // Primero obtenemos el producto
            String getProductoUrl = String.format("productos/%d", idProducto);
            HttpClientUtil.HttpResponseWrapper<String> getResponse = HttpClientUtil.get(getProductoUrl, String.class);
            
            if (getResponse.getStatusCode() == 200) {
                // Actualizamos el producto con el id_menu
                String updateUrl = String.format("productos/%d", idProducto);
                String requestBody = String.format("{\"id_menu\":%d}", idMenu);
                HttpClientUtil.HttpResponseWrapper<String> updateResponse = 
                    HttpClientUtil.put(updateUrl, requestBody, String.class);
                    
                return updateResponse.getStatusCode() == 200 || updateResponse.getStatusCode() == 204;
            }
            return false;
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
            // Actualizamos el producto estableciendo id_menu a null
            String updateUrl = String.format("productos/%d", idProducto);
            String requestBody = "{\"id_menu\":null}";
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.put(updateUrl, requestBody, String.class);
                
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
            String url = String.format("%s/%d/disponibilidad", BASE_ENDPOINT, idMenu);
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
