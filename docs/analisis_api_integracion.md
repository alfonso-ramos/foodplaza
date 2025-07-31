# Análisis de Integración con API FastAPI

## 1. Configuración Básica

### Frontend (JavaFX)
- **Librerías Necesarias**:
  - `java.net.http` (incluido en Java 11+)
  - `com.google.gson` para JSON

### Backend (FastAPI)
- **URL Base**: `http://localhost:8000` (ajustar según despliegue)
- **Formato**: JSON
- **Autenticación**: JWT (JSON Web Tokens)

## 2. Endpoints Esenciales

### Autenticación
```
POST   /api/auth/login
Body: { "email": "...", "password": "..." }
Respuesta: { "access_token": "...", "token_type": "bearer" }
```

### Plazas
```
GET    /api/plazas           # Listar plazas
POST   /api/plazas           # Crear plaza
GET    /api/plazas/{id}      # Obtener plaza
PUT    /api/plazas/{id}      # Actualizar plaza
DELETE /api/plazas/{id}      # Eliminar plaza
```

### Locales
```
GET    /api/locales          # Listar locales
POST   /api/locales          # Crear local
GET    /api/locales/{id}     # Obtener local
PUT    /api/locales/{id}     # Actualizar local
DELETE /api/locales/{id}     # Eliminar local
```

## 3. Estructura de Datos

### Usuario
```json
{
  "id": 1,
  "email": "usuario@ejemplo.com",
  "full_name": "Nombre Usuario",
  "role": "admin|manager|user"
}
```

### Plaza
```json
{
  "id": 1,
  "nombre": "Nombre Plaza",
  "direccion": "Dirección completa",
  "estado": "activo|inactivo"
}
```

### Local
```json
{
  "id": 1,
  "nombre": "Nombre Local",
  "descripcion": "Descripción del local",
  "id_plaza": 1,
  "estado": "abierto|cerrado"
}
```

## 4. Implementación Cliente API

### Clase ApiClient
```java
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8000";
    private static String authToken;
    
    // Métodos para hacer peticiones HTTP (GET, POST, PUT, DELETE)
    // Manejo de headers y autenticación
}
```

### Ejemplo de Uso
```java
// Login
Response loginResponse = ApiClient.post("/api/auth/login", 
    "{\"email\":\"admin@ejemplo.com\",\"password\":\"12345\"}");

// Obtener plazas
Response plazasResponse = ApiClient.get("/api/plazas");
```

## 5. Manejo de Errores
- **401 Unauthorized**: Token inválido o expirado
- **403 Forbidden**: Sin permisos suficientes
- **404 Not Found**: Recurso no encontrado
- **500 Internal Server Error**: Error del servidor

## 6. Próximos Pasos
1. Implementar `ApiClient` con métodos básicos
2. Crear servicios para cada entidad (Auth, Plazas, Locales)
3. Actualizar controladores para usar la API
4. Manejar estados de carga y error en la UI
5. Implementar refresco de token

## Notas
- Mantener el token JWT en memoria (no almacenar en disco por seguridad)
- Manejar la expiración del token (refresh token si es necesario)
- Validar respuestas del servidor
- Usar HTTPS en producción
