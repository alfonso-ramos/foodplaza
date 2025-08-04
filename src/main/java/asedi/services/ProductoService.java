package asedi.services;

import asedi.model.ImagenResponse;
import asedi.model.Producto;
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
    private static final String UPLOAD_ENDPOINT = "upload";
    private final Gson gson = new Gson();
    
    // Caches
    private final Map<Long, Producto> cache = new ConcurrentHashMap<>();
    private final Map<Long, List<Producto>> productosPorMenuCache = new ConcurrentHashMap<>();
    private List<Producto> allProductosCache = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Obtiene todos los productos disponibles.
     */
    public List<Producto> obtenerTodos() throws IOException {
        long currentTime = System.currentTimeMillis();
        
        if (allProductosCache != null && (currentTime - lastCacheUpdate) < CACHE_DURATION_MS) {
            return new ArrayList<>(allProductosCache);
        }
        
        try {
            String response = HttpClientUtil.get(ENDPOINT, String.class).getBody();
            Type listType = new TypeToken<ArrayList<Producto>>() {}.getType();
            List<Producto> productos = gson.fromJson(response, listType);
            
            // Actualizar cachés
            allProductosCache = productos;
            lastCacheUpdate = currentTime;
            cache.clear();
            productosPorMenuCache.clear();
            
            for (Producto producto : productos) {
                cache.put(producto.getId(), producto);
                // Actualizar caché por menú
                if (producto.getIdMenu() != null) {
                    productosPorMenuCache
                        .computeIfAbsent(producto.getIdMenu(), _ -> new ArrayList<>())
                        .add(producto);
                }
            }
            
            return new ArrayList<>(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener la lista de productos: " + e.getMessage());
            throw new IOException("No se pudo obtener la lista de productos", e);
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
     * Obtiene los productos de un menú específico.
     */
    public List<Producto> obtenerPorMenu(Long idMenu) throws IOException {
        if (idMenu == null) {
            throw new IllegalArgumentException("El ID del menú no puede ser nulo");
        }
        
        // Verificar caché primero
        if (productosPorMenuCache.containsKey(idMenu)) {
            return new ArrayList<>(productosPorMenuCache.get(idMenu));
        }
        
        try {
            String response = HttpClientUtil.get(ENDPOINT + "?id_menu=" + idMenu, String.class).getBody();
            Type listType = new TypeToken<ArrayList<Producto>>() {}.getType();
            List<Producto> productos = gson.fromJson(response, listType);
            
            // Actualizar cachés
            productosPorMenuCache.put(idMenu, new ArrayList<>(productos));
            productos.forEach(p -> cache.put(p.getId(), p));
            
            return new ArrayList<>(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener productos del menú " + idMenu + ": " + e.getMessage());
            throw new IOException("No se pudieron obtener los productos del menú", e);
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
            String jsonProducto = gson.toJson(producto);
            String response = HttpClientUtil.post(ENDPOINT, jsonProducto, String.class).getBody();
            Producto productoCreado = gson.fromJson(response, Producto.class);
            
            // Invalidar cachés
            invalidarCache();
            
            return productoCreado;
        } catch (Exception e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
            throw new IOException("No se pudo crear el producto", e);
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
            String jsonProducto = gson.toJson(producto);
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.put(ENDPOINT + "/" + producto.getId(), jsonProducto, String.class);
            
            // Invalidar cachés
            cache.remove(producto.getId());
            if (producto.getIdMenu() != null) {
                productosPorMenuCache.remove(producto.getIdMenu());
            }
            allProductosCache = null;
            
            return response.getStatusCode() == 200 || response.getStatusCode() == 204;
        } catch (Exception e) {
            System.err.println("Error al actualizar el producto: " + e.getMessage());
            throw new IOException("No se pudo actualizar el producto", e);
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
            if (producto != null) {
                cache.remove(id);
                if (producto.getIdMenu() != null) {
                    List<Producto> productosDelMenu = productosPorMenuCache.get(producto.getIdMenu());
                    if (productosDelMenu != null) {
                        productosDelMenu.removeIf(p -> p.getId().equals(id));
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
     */
    public String subirImagen(File imagen) throws IOException {
        if (imagen == null || !imagen.exists()) {
            throw new IllegalArgumentException("El archivo de imagen no existe");
        }
        
        try {
            Map<String, String> formFields = new HashMap<>();
            HttpResponseWrapper<ImagenResponse> response = HttpClientUtil.uploadFile(
                UPLOAD_ENDPOINT, 
                "image", 
                imagen, 
                formFields, 
                ImagenResponse.class
            );
            
            ImagenResponse imagenResponse = response.getBody();
            return imagenResponse != null ? imagenResponse.getUrl() : null;
        } catch (Exception e) {
            System.err.println("Error al subir la imagen: " + e.getMessage());
            throw new IOException("No se pudo subir la imagen", e);
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
            // Este endpoint debería ser implementado en el backend
            String endpoint = String.format("%s/delete-image?public_id=%s", ENDPOINT, publicId);
            HttpClientUtil.HttpResponseWrapper<String> response = 
                HttpClientUtil.delete(endpoint, String.class);
                
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error al eliminar la imagen: " + e.getMessage());
            throw new IOException("No se pudo eliminar la imagen", e);
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
