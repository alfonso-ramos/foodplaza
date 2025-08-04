# Plan de Desarrollo - Módulo de Gerencia

## 1. Estructura del Proyecto

### 1.1 Directorios Creados ✅
```
src/main/java/asedi/
├── controllers/gerencia/
│   ├── ProductoController.java         # ✅ Gestión de productos (implementado)
│   ├── ProductoFormController.java     # ✅ Formulario de productos (implementado)
│   ├── MenuController.java             # ✅ Gestión de menús (implementado)
│   ├── MenuFormController.java         # ✅ Formulario de menús (implementado)
│   └── components/
│       └── ProductoCardController.java # ✅ Componente de tarjeta de producto
├── models/
│   └── Producto.java                   # ✅ Modelo de producto (implementado)
└── services/
    └── ProductoService.java            # ✅ Servicio de productos (implementado)
```

### 1.2 Próximos Pasos
1. Completar la implementación de `MenuController` y sus componentes asociados
2. Implementar `PedidoController` para la gestión de pedidos
3. Desarrollar las vistas FXML faltantes
4. Implementar la lógica de negocio restante
5. Realizar pruebas integrales

## 2. Módulo de Productos (Completado) ✅

### 2.1 Características Implementadas
- [x] Listado de productos con paginación
- [x] Búsqueda y filtrado por categoría/disponibilidad
- [x] Crear, editar y eliminar productos
- [x] Carga de imágenes para productos
- [x] Validación de formularios
- [x] Manejo de errores y retroalimentación al usuario

### 2.2 Archivos Principales
- `ProductoController.java`: Controlador principal de productos
- `ProductoFormController.java`: Controlador del formulario de productos
- `ProductoCardController.java`: Componente de tarjeta para mostrar productos
- `ProductoService.java`: Lógica de negocio y comunicación con la API
- `Producto.java`: Modelo de datos

## 3. Módulo de Menús (En Progreso) ⏳

### 3.1 Características Implementadas ✅
- [x] Listado de menús con sus productos
- [x] Crear, editar y eliminar menús
- [x] Validación de formularios
- [x] Manejo asíncrono de operaciones
- [x] Retroalimentación al usuario
- [x] Gestión de disponibilidad de menús

### 3.2 Archivos Implementados
- `MenuController.java` - Controlador principal de menús
- `MenuFormController.java` - Controlador del formulario de menús
- `MenuService.java` - Lógica de negocio y comunicación con la API
- `Menu.java` - Modelo de datos con validaciones
- Vistas FXML:
  - `listado_menus.fxml`
  - `formulario_menu.fxml`

## 4. Módulo de Pedidos (Pendiente) ⏳

### 4.1 Características Pendientes
- [ ] Visualización de pedidos
- [ ] Actualización de estado de pedidos
- [ ] Historial de pedidos
- [ ] Filtrado y búsqueda

### 4.2 Archivos por Implementar
- `PedidoController.java`
- `PedidoService.java`
- `Pedido.java`
- `ItemPedido.java`
- Vistas FXML correspondientes

## 5. Próximos Pasos

### 5.1 Prioridad Alta
1. Completar la implementación del módulo de menús
2. Implementar las vistas FXML faltantes
3. Añadir validaciones adicionales
4. Mejorar el manejo de errores

### 5.2 Prioridad Media
1. Implementar el módulo de pedidos
2. Añadir reportes básicos
3. Implementar sistema de notificaciones

### 5.3 Mejoras Futuras
1. Panel de estadísticas
2. Exportación de datos
3. Integración con sistemas de pago
│   └── PedidoService.java
└── views/gerencia/
    ├── dashboard/                      # Vistas del dashboard
    ├── menus/                          # Vistas de menús
    ├── productos/                      # Vistas de productos
    └── pedidos/                        # Vistas de pedidos
```

## 2. Tareas por Módulo

### 2.1 Módulo de Menús ✅

#### Modelo de Datos (`Menu.java`)
- [x] Clase Menu con propiedades básicas
- [x] Anotaciones de validación (@NotBlank, @Size, @NotNull)
- [x] Métodos helper y validación

#### Servicio (`MenuService.java`)
- [x] Métodos CRUD completos
- [x] Manejo de errores y excepciones
- [x] Caché de menús
- [x] Integración con API REST

#### Vistas (`/views/gerencia/menus/`)
- [x] `listado_menus.fxml` - Vista de lista con búsqueda y filtrado
- [x] `formulario_menu.fxml` - Formulario CRUD con validación
- [ ] `detalle_menu.fxml` - Vista detallada (pendiente)

### 2.2 Módulo de Productos

#### Modelo de Datos (`Producto.java`)
- [x] Crear clase Producto
- [x] Relaciones con categorías
- [x] Manejo de imágenes

#### Servicio (`ProductoService.java`)
- [x] Métodos CRUD
- [ ] Subida de imágenes
- [x] Búsqueda y filtrado

#### Vistas (`/views/gerencia/productos/`)
- [x] `listado_productos.fxml` (implementado en el controlador)
- [ ] `formulario_producto.fxml`
- [ ] `categorias.fxml`

### 2.3 Módulo de Pedidos

#### Modelo de Datos (`Pedido.java`, `ItemPedido.java`)
- [ ] Estructura de pedidos
- [ ] Estados del pedido
- [ ] Relaciones con menús/productos

#### Servicio (`PedidoService.java`)
- [ ] Consulta de pedidos
- [ ] Actualización de estados
- [ ] Notificaciones

#### Vistas (`/views/gerencia/pedidos/`)
- [ ] `panel_pedidos.fxml` - Vista general
- [ ] `detalle_pedido.fxml` - Detalle de pedido
- [ ] `estadisticas.fxml` - Gráficos

## 3. Componentes Compartidos

### 3.1 Componentes de UI
- [ ] `Navbar` - Navegación principal
- [ ] `Sidebar` - Menú lateral
- [ ] `Card` - Tarjetas reutilizables
- [ ] `Modal` - Ventanas emergentes
- [ ] `Alerta` - Notificaciones

### 3.2 Utilidades
- [ ] `FormValidator` - Validación de formularios
- [ ] `ImageHandler` - Manejo de imágenes
- [ ] `DateUtils` - Utilidades de fecha

## 4. Flujo de Pantallas

### 4.1 Flujo Principal
1. Login → Dashboard → (Menús/Productos/Pedidos)
2. Dashboard → Lista de Menús → Editar Menú
3. Dashboard → Lista de Pedidos → Detalle de Pedido

### 4.2 Navegación
- [ ] Implementar sistema de rutas
- [ ] Manejo de estado global
- [ ] Historial de navegación

## 5. Integración con API

### 5.1 Endpoints Requeridos
```
# Menús
GET    /api/menus
POST   /api/menus
GET    /api/menus/{id}
PUT    /api/menus/{id}
DELETE /api/menus/{id}

# Productos
GET    /api/productos
POST   /api/productos
# ... (similar estructura a menús)

# Pedidos
GET    /api/pedidos
GET    /api/pedidos/{id}
PUT    /api/pedidos/{id}/estado
```

### 5.2 Manejo de Autenticación
- [ ] Interceptor de peticiones
- [ ] Refresh token
- [ ] Manejo de sesión

## 6. Pruebas

### 6.1 Pruebas Unitarias
- [ ] Pruebas de servicios
- [ ] Pruebas de controladores
- [ ] Pruebas de utilidades

### 6.2 Pruebas de Integración
- [ ] Flujos completos
- [ ] Integración con API
- [ ] Pruebas de carga

## 7. Despliegue

### 7.1 Configuración
- [ ] Variables de entorno
- [ ] Configuración de producción
- [ ] Documentación de despliegue

### 7.2 Monitoreo
- [ ] Logging
- [ ] Métricas
- [ ] Alertas

## 8. Documentación

### 8.1 Documentación Técnica
- [ ] Estructura del proyecto
- [ ] Guía de estilos
- [ ] Convenciones de código

### 8.2 Manual de Usuario
- [ ] Guía de instalación
- [ ] Tutoriales
- [ ] Preguntas frecuentes

## 9. Cronograma Estimado

### Fase 1: Estructura Base (Semana 1)
- [ ] Configuración inicial
- [ ] Autenticación
- [ ] Estructura de navegación

### Fase 2: Módulo de Productos (Semana 2)
- [ ] CRUD de productos
- [ ] Gestión de imágenes
- [ ] Categorías

### Fase 3: Módulo de Menús (Semana 3)
- [ ] CRUD de menús
- [ ] Asociación de productos
- [ ] Disponibilidad

### Fase 4: Módulo de Pedidos (Semana 4)
- [ ] Listado de pedidos
- [ ] Gestión de estados
- [ ] Notificaciones

### Fase 5: Reportes y Ajustes (Semana 5)
- [ ] Estadísticas
- [ ] Ajustes finales
- [ ] Pruebas de usuario
