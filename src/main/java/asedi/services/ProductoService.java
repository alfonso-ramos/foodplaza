package asedi.services;

import asedi.model.ImagenResponse;
import asedi.model.Producto;
import asedi.model.Menu;
import asedi.utils.HttpClientUtil;
import asedi.utils.HttpClientUtil.HttpResponseWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProductoService {
    private static final String ENDPOINT = "productos";
    private final Gson gson = new Gson();
    
    // Caches
    private final Map<Long, Producto> cache = new ConcurrentHashMap<>();
    private final Map<Long, List<Producto>> productosPorMenuCache = new ConcurrentHashMap<>();
    private List<Producto> allProductosCache = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Obtiene todos los productos del sistema, agrupados por menú.
     * @return Lista de todos los productos disponibles
     * @throws IOException Si hay un error de conexión
     */
    /**
     * Obtiene todos los productos del sistema.
     * @return Lista de todos los productos
     */
    public List<Producto> obtenerTodos() {
        return obtenerTodosLosProductos();
    }
    
    /**
     * Obtiene todos los productos del sistema, agrupados por menú.
     * @return Lista de todos los productos disponibles
     * @deprecated Usar obtenerTodos() en su lugar
     */
    public List<Producto> obtenerTodosLosProductos() {
        // Primero necesitamos obtener la lista de menús disponibles
        MenuService menuService = new MenuService();
        List<asedi.model.Menu> menus;
        List<Producto> todosLosProductos = new ArrayList<>();
        
        try {
            // Obtener todos los menús disponibles
            menus = menuService.obtenerTodos();
            
            if (menus == null || menus.isEmpty()) {
                System.out.println("No se encontraron menús disponibles");
                return todosLosProductos;
            }
            
            // Obtener productos de cada menú
            for (asedi.model.Menu menu : menus) {
                try {
                    List<Producto> productosDelMenu = obtenerTodos(menu.getId());
                    if (productosDelMenu != null && !productosDelMenu.isEmpty()) {
                        // Agregar los productos del menú actual a la lista total
                        todosLosProductos.addAll(productosDelMenu);
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener productos del menú " + menu.getId() + ": " + e.getMessage());
                    // Continuar con el siguiente menú en caso de error
                }
            }
            
            System.out.println("Total de productos obtenidos de todos los menús: " + todosLosProductos.size());
        } catch (Exception e) {
            System.err.println("Error al obtener la lista de menús: " + e.getMessage());
            e.printStackTrace();
        }
        
        return todosLosProductos;
    }
    
    /**
     * Obtiene todos los productos de un menú específico.
     * @param menuId ID del menú del que se desean obtener los productos
     * @return Lista de productos del menú especificado
     * @throws IOException Si hay un error de conexión
     */
    public List<Producto> obtenerTodos(Long menuId) throws IOException {
        if (menuId == null) {
            throw new IllegalArgumentException("El ID del menú no puede ser nulo");
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Verificar caché para este menú específico
        if (productosPorMenuCache.containsKey(menuId) && (currentTime - lastCacheUpdate) < CACHE_DURATION_MS) {
            System.out.println("Obteniendo productos del menú " + menuId + " desde caché");
            return new ArrayList<>(productosPorMenuCache.get(menuId));
        }
        
        try {
            // Construir la URL para obtener productos por menú
            String url = String.format("%s/menu/%d", ENDPOINT, menuId);
            
            System.out.println("Solicitando productos desde: " + url);
            
            // Realizar la petición GET
            HttpClientUtil.HttpResponseWrapper<String> httpResponse = HttpClientUtil.get(url, String.class);
            String responseBody = httpResponse.getBody();
            
            System.out.println("Respuesta del servidor recibida. Estado: " + httpResponse.getStatusCode());
            System.out.println("Tamaño de la respuesta: " + (responseBody != null ? responseBody.length() : 0) + " caracteres");
            
            // Verificar si la respuesta es exitosa (código 200)
            if (httpResponse.getStatusCode() != 200) {
                throw new IOException("Error al obtener productos. Código: " + httpResponse.getStatusCode());
            }
            
            // Verificar si la respuesta está vacía
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.out.println("La respuesta del servidor está vacía");
                return new ArrayList<>();
            }
            
            // Intentar parsear como array directo
            try {
                Type listType = new TypeToken<ArrayList<Producto>>() {}.getType();
                List<Producto> productos = gson.fromJson(responseBody, listType);
                
                if (productos != null) {
                    System.out.println("Se obtuvieron " + productos.size() + " productos para el menú " + menuId);
                    
                    // Actualizar cachés
                    productosPorMenuCache.put(menuId, new ArrayList<>(productos));
                    lastCacheUpdate = currentTime;
                    
                    // Actualizar caché individual
                    for (Producto producto : productos) {
                        if (producto != null && producto.getId() != null) {
                            cache.put(producto.getId(), producto);
                        }
                    }
                    
                    return new ArrayList<>(productos);
                }
            } catch (com.google.gson.JsonSyntaxException e) {
                System.out.println("Error al analizar como array directo: " + e.getMessage());
                
                // Si falla el parseo como array, intentar como objeto con campo 'data'
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
                    
                    if (responseMap != null) {
                        System.out.println("Respuesta del servidor (formato objeto): " + responseMap.keySet());
                        
                        if (responseMap.containsKey("data")) {
                            Type listType = new TypeToken<ArrayList<Producto>>() {}.getType();
                            List<Producto> productos = gson.fromJson(gson.toJson(responseMap.get("data")), listType);
                            
                            if (productos != null) {
                                System.out.println("Se obtuvieron " + productos.size() + " productos desde el campo 'data'");
                                
                                // Actualizar cachés
                                productosPorMenuCache.put(menuId, new ArrayList<>(productos));
                                lastCacheUpdate = currentTime;
                                
                                // Actualizar caché individual
                                for (Producto producto : productos) {
                                    if (producto != null && producto.getId() != null) {
                                        cache.put(producto.getId(), producto);
                                    }
                                }
                                
                                return new ArrayList<>(productos);
                            }
                        } else {
                            // Si no hay campo 'data', intentar parsear como un solo producto
                            try {
                                Producto producto = gson.fromJson(responseBody, Producto.class);
                                if (producto != null) {
                                    List<Producto> productos = new ArrayList<>();
                                    productos.add(producto);
                                    
                                    // Actualizar cachés
                                    productosPorMenuCache.put(menuId, new ArrayList<>(productos));
                                    lastCacheUpdate = currentTime;
                                    
                                    if (producto.getId() != null) {
                                        cache.put(producto.getId(), producto);
                                    }
                                    
                                    return productos;
                                }
                            } catch (Exception e2) {
                                System.err.println("Error al analizar como objeto único: " + e2.getMessage());
                            }
                        }
                    }
                } catch (Exception e2) {
                    System.err.println("Error al analizar la respuesta del servidor: " + e2.getMessage());
                    throw new IOException("Formato de respuesta del servidor no reconocido", e2);
                }
            }
            
            // Si llegamos aquí, no se pudo parsear la respuesta de ninguna manera
            System.err.println("No se pudo interpretar la respuesta del servidor: " + responseBody);
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Error al obtener la lista de productos: " + e.getMessage());
            e.printStackTrace();
            
            // Si hay datos en caché, devolverlos como respaldo
            if (allProductosCache != null) {
                System.err.println("Usando datos en caché debido al error");
                return new ArrayList<>(allProductosCache);
            }
            
            throw new IOException("No se pudo obtener la lista de productos: " + e.getMessage(), e);
        }
    }
    
    private void updateCaches(List<Producto> productos, long currentTime) {
        allProductosCache = productos;
        lastCacheUpdate = currentTime;
        cache.clear();
        productosPorMenuCache.clear();
        
        for (Producto producto : productos) {
            if (producto != null) {
                cache.put(producto.getId(), producto);
                // Actualizar caché por menú
                if (producto.getIdMenu() != null) {
                    productosPorMenuCache
                        .computeIfAbsent(producto.getIdMenu(), _ -> new ArrayList<>())
                        .add(producto);
                }
            }
        }
    }
    
    /**
     * Obtiene un producto por su ID.
     */
    public Producto obtenerPorId(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        // Intentar obtener de la caché primero
        Producto cachedProducto = cache.get(id);
        if (cachedProducto != null) {
            return cachedProducto;
        }
        
        try {
            String response = HttpClientUtil.get(ENDPOINT + "/" + id, String.class).getBody();
            Producto producto = gson.fromJson(response, Producto.class);
            
            // Actualizar caché
            if (producto != null) {
                cache.put(producto.getId(), producto);
            }
            
            return producto;
        } catch (Exception e) {
            System.err.println("Error al obtener el producto con ID " + id + ": " + e.getMessage());
            throw new IOException("No se pudo obtener el producto", e);
        }
    }
    
    /**
     * Obtiene una página de productos con paginación.
     * @param inicio Índice del primer elemento a recuperar (0-based)
     * @param cantidad Cantidad de elementos a recuperar
     * @return Lista de productos para la página solicitada
     */
    public List<Producto> obtenerPaginados(int inicio, int cantidad) throws IOException {
        if (inicio < 0 || cantidad <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }

        try {
            // Si tenemos una caché reciente, usarla para la paginación
            if (allProductosCache != null && !allProductosCache.isEmpty()) {
                int end = Math.min(inicio + cantidad, allProductosCache.size());
                if (inicio >= allProductosCache.size()) {
                    return new ArrayList<>();
                }
                return new ArrayList<>(allProductosCache.subList(inicio, end));
            }

            // Si no hay caché, hacer una petición al servidor
            Map<String, String> params = new HashMap<>();
            params.put("inicio", String.valueOf(inicio));
            params.put("cantidad", String.valueOf(cantidad));
            
            String response = HttpClientUtil.getWithParams(ENDPOINT + "/paginados", params, String.class).getBody();
            Type listType = new TypeToken<ArrayList<Producto>>() {}.getType();
            return gson.fromJson(response, listType);
        } catch (Exception e) {
            System.err.println("Error al obtener productos paginados: " + e.getMessage());
            throw new IOException("No se pudieron obtener los productos", e);
        }
    }

    /**
     * Obtiene el número total de productos.
     */
    public int contarTotal() throws IOException {
        try {
            // Si tenemos una caché reciente, usarla
            if (allProductosCache != null) {
                return allProductosCache.size();
            }
            
            // Si no, hacer una petición al servidor
            String response = HttpClientUtil.get(ENDPOINT + "/total", String.class).getBody();
            return Integer.parseInt(response);
        } catch (Exception e) {
            System.err.println("Error al contar productos: " + e.getMessage());
            throw new IOException("No se pudo obtener el conteo de productos", e);
        }
    }
    
    /**
     * Obtiene los productos de un menú específico.
     */
    public List<Producto> obtenerPorMenu(Long idMenu) throws IOException {
        if (idMenu == null) {
            throw new IllegalArgumentException("El ID del menú no puede ser nulo");
        }
        
        // Verificar caché primero
        if (productosPorMenuCache.containsKey(idMenu) && 
            (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION_MS) {
            return new ArrayList<>(productosPorMenuCache.get(idMenu));
        }
        
        try {
            // Usar el endpoint correcto: /api/menus/local/{id_local}
            String url = String.format("menus/local/%d", idMenu);
            System.out.println("Solicitando productos del menú desde: " + url);
            
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.get(url, String.class);
                
            System.out.println("Respuesta del servidor recibida. Estado: " + response.getStatusCode());
            
            if (response.getStatusCode() != 200) {
                throw new IOException("Error al obtener productos. Código: " + response.getStatusCode());
            }
            
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.out.println("La respuesta del servidor está vacía");
                return new ArrayList<>();
            }
            
            // El endpoint devuelve un array de menús, necesitamos extraer los productos
            Type listType = new TypeToken<ArrayList<Menu>>() {}.getType();
            List<Menu> menus = gson.fromJson(responseBody, listType);
            
            // Extraer productos de los menús
            List<Producto> productos = new ArrayList<>();
            if (menus != null && !menus.isEmpty()) {
                for (Menu menu : menus) {
                    if (menu != null && menu.getProductos() != null) {
                        for (Producto producto : menu.getProductos()) {
                            if (producto != null) {
                                // Asegurarse de que el ID del menú esté establecido
                                producto.setIdMenu(menu.getId());
                                productos.add(producto);
                            }
                        }
                    }
                }
            }
            
            // Actualizar cachés
            if (!productos.isEmpty()) {
                productosPorMenuCache.put(idMenu, new ArrayList<>(productos));
                productos.forEach(p -> cache.put(p.getId(), p));
                lastCacheUpdate = System.currentTimeMillis();
            }
            
            return new ArrayList<>(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener productos del menú " + idMenu + ": " + e.getMessage());
            e.printStackTrace();
            throw new IOException("No se pudieron obtener los productos del menú: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crea un nuevo producto.
     */
    public Producto crear(Producto producto) throws IOException {
        if (producto == null || !producto.isValid()) {
            throw new IllegalArgumentException("Los datos del producto no son válidos");
        }
        
        try {
            // No serializamos a JSON manualmente, HttpClientUtil lo hará
            Producto productoCreado = HttpClientUtil.post(ENDPOINT, producto, Producto.class).getBody();
            
            // Invalidar cachés
            invalidarCache();
            
            return productoCreado;
        } catch (Exception e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
            throw new IOException("No se pudo crear el producto: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un producto existente.
     */
    public boolean actualizar(Producto producto) throws IOException {
        if (producto == null || producto.getId() == null || !producto.isValid()) {
            throw new IllegalArgumentException("Datos del producto no válidos para actualización");
        }
        
        try {
            // No serializamos a JSON manualmente
            HttpClientUtil.HttpResponseWrapper<Producto> response = 
                HttpClientUtil.put(ENDPOINT + "/" + producto.getId(), producto, Producto.class);
            
            // Actualizar cachés
            if (response.getStatusCode() == 200 || response.getStatusCode() == 204) {
                cache.remove(producto.getId());
                if (producto.getIdMenu() != null) {
                    productosPorMenuCache.remove(producto.getIdMenu());
                }
                allProductosCache = null;
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error al actualizar el producto: " + e.getMessage());
            throw new IOException("No se pudo actualizar el producto: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sube una imagen para un producto.
     * @param productoId ID del producto
     * @param imagen Archivo de imagen a subir
     * @param descripcion Descripción de la imagen
     * @return true si la operación fue exitosa
     */
    public boolean subirImagen(Long productoId, File imagen, String descripcion) throws IOException {
        if (productoId == null || imagen == null || !imagen.exists()) {
            throw new IllegalArgumentException("Datos de imagen no válidos");
        }
        
        try {
            // Crear un mapa para los parámetros de la solicitud
            Map<String, Object> params = new HashMap<>();
            params.put("file", imagen);
            
            // Crear un mapa para los headers
            Map<String, String> headers = new HashMap<>();
            headers.put("description", descripcion != null ? descripcion : "");
            
            // Realizar la petición POST con el archivo
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.uploadFile(
                    ENDPOINT + "/" + productoId + "/imagen", 
                    "file", 
                    imagen, 
                    headers, 
                    String.class
                );
            
            // Invalidar caché del producto
            cache.remove(productoId);
            
            return response.getStatusCode() == 200 || response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al subir la imagen: " + e.getMessage());
            throw new IOException("No se pudo subir la imagen: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina un producto por su ID.
     */
    public boolean eliminar(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        try {
            // Obtener el producto primero para actualizar las cachés correctamente
            Producto producto = obtenerPorId(id);
            
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.delete(ENDPOINT + "/" + id, String.class);
            
            // Actualizar cachés
            if (response.getStatusCode() == 200 || response.getStatusCode() == 204) {
                if (producto != null) {
                    cache.remove(id);
                    if (producto.getIdMenu() != null) {
                        List<Producto> productosDelMenu = productosPorMenuCache.get(producto.getIdMenu());
                        if (productosDelMenu != null) {
                            productosDelMenu.removeIf(p -> p.getId().equals(id));
                        }
                    }
                }
                allProductosCache = null;
            }
            
            return response.getStatusCode() == 200 || response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al eliminar el producto con ID " + id + ": " + e.getMessage());
            throw new IOException("No se pudo eliminar el producto", e);
        }
    }
    
    /**
     * Sube una imagen para un producto.
     * @param imagen Archivo de imagen a subir
     * @return URL de la imagen subida o null si falla
     * @throws IOException Si hay un error al subir la imagen
     */
    public String subirImagen(File imagen) throws IOException {
        if (imagen == null || !imagen.exists()) {
            throw new IllegalArgumentException("El archivo de imagen no existe");
        }
        
        try {
            // Usar el endpoint de la API para subir imágenes
            String endpoint = "upload"; // Este será relativo a la URL base de la API
            Map<String, String> formFields = new HashMap<>();
            
            // Agregar descripción vacía para mantener compatibilidad
            formFields.put("description", "");
            
            // Usar el método de subida de archivos con el endpoint correcto
            HttpResponseWrapper<ImagenResponse> response = HttpClientUtil.uploadFile(
                endpoint, 
                "file", 
                imagen, 
                formFields, 
                ImagenResponse.class
            );
            
            ImagenResponse imagenResponse = response.getBody();
            return imagenResponse != null ? imagenResponse.getUrl() : null;
        } catch (Exception e) {
            System.err.println("Error al subir la imagen: " + e.getMessage());
            throw new IOException("No se pudo subir la imagen: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina una imagen de Cloudinary usando su public ID.
     */
    public boolean eliminarImagen(String publicId) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("El public ID no puede estar vacío");
        }
        
        try {
            // Construir el endpoint correctamente sin duplicar /api
            String endpoint = String.format("upload/delete?public_id=%s", publicId);
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.delete(endpoint, String.class);
                
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error al eliminar la imagen: " + e.getMessage());
            throw new IOException("No se pudo eliminar la imagen: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fuerza la actualización de las cachés.
     */
    public void invalidarCache() {
        allProductosCache = null;
        cache.clear();
        productosPorMenuCache.clear();
        lastCacheUpdate = 0;
    }
}