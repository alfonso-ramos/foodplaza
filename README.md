# FoodPlaza Frontend

Aplicación de escritorio desarrollada con JavaFX para la gestión integral del sistema FoodPlaza, incluyendo gestión de menús, productos, pedidos, usuarios y más.

## Características Principales

- **Módulo de Gerencia**: Gestión completa de menús, productos y configuraciones del restaurante.
- **Panel de Gerente**: Visualización de estadísticas y gestión de operaciones diarias.
- **Interfaz de Usuario**: Diseño intuitivo y responsivo para diferentes roles de usuario.
- **Integración con Backend**: Conexión con servicios RESTful para persistencia de datos.

## Requisitos Previos

- Java JDK 21 o superior
- Maven 3.6.0 o superior
- Conexión a internet para descargar dependencias
- Servidor backend de FoodPlaza en ejecución

## Estructura del Proyecto

```
src/
  main/
    java/
      asedi/
        Main.java                  # Punto de entrada de la aplicación
        controllers/               # Controladores de las vistas
          gerencia/               # Controladores para el módulo de gerencia
          gerente/                # Controladores para el panel del gerente
          usuario/                # Controladores para la interfaz de usuario
        models/                   # Modelos de datos
        services/                 # Servicios para comunicación con la API
        utils/                    # Utilidades y ayudantes
    resources/
      views/
        gerencia/                 # Vistas del módulo de gerencia
        gerente/                  # Vistas del panel del gerente
        usuario/                  # Vistas de usuario
      styles/                     # Hojas de estilo CSS
      images/                     # Recursos de imagen
      data/                       # Archivos de datos estáticos
```

## Cómo Ejecutar la Aplicación

### Configuración Inicial

1. Clona el repositorio:
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd foodplaza-frontend
   ```

2. Asegúrate de que el servidor backend de FoodPlaza esté en ejecución.

### Ejecución con Maven

Para compilar y ejecutar la aplicación, usa el siguiente comando en la raíz del proyecto:

```bash
mvn clean javafx:run
```

### Perfiles de Ejecución

- **Modo Desarrollo** (con logs detallados):
  ```bash
  mvn clean javafx:run -Djavafx.verbose
  ```

- **Empaquetado** (sin ejecutar):
  ```bash
  mvn clean package
  ```

## Dependencias Principales

- **JavaFX 21.0.1**: Para la interfaz gráfica
- **Gson 2.10.1**: Para el procesamiento de JSON
- **Hibernate Validator 8.0.1**: Para validación de datos
- **Apache HttpClient 4.5.14**: Para comunicación HTTP con el backend

## Configuración

La aplicación busca la configuración en el archivo `config.properties` en el directorio de recursos. Asegúrate de configurar:

- URL del servidor backend
- Configuraciones de conexión
- Rutas de recursos

## Solución de Problemas Comunes

### Error al cargar FXML

Si encuentras errores al cargar archivos FXML:

1. Verifica que las rutas en los controladores sean correctas
2. Asegúrate de que los archivos FXML estén en el directorio `resources/views/`
3. Revisa los logs para mensajes de error más detallados

### Problemas de Conexión

Si la aplicación no puede conectarse al backend:

1. Verifica que el servidor backend esté en ejecución
2. Comprueba la URL del servidor en la configuración
3. Asegúrate de que no haya restricciones de firewall


## Contacto

Para soporte o consultas, contacta al equipo de desarrollo en [correo@ejemplo.com](mailto:correo@ejemplo.com)
