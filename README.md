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

## Generar Ejecutable (.exe) con Launch4j

Para crear un ejecutable nativo de Windows (`.exe`) para la aplicación, puedes usar Launch4j.

### Prerrequisitos

1.  **Tener Java 21 (o superior) instalado** y configurado en las variables de entorno del sistema.
2.  **Haber compilado el proyecto** para generar el JAR con todas las dependencias. Ejecuta el siguiente comando si aún no lo has hecho:
    ```bash
    mvn clean package
    ```
    Esto creará el archivo `foodplaza-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar` en el directorio `target/`.
3.  **Descargar e instalar Launch4j** desde su [sitio web oficial](http://launch4j.sourceforge.net/).

### Pasos para Generar el .exe

1.  **Abrir Launch4j**: Inicia la aplicación Launch4j.
2.  **Cargar Configuración**: Haz clic en el icono de la carpeta (**Open configuration**) y selecciona el archivo `launch4j.xml` que se encuentra en la raíz del proyecto.
3.  **Verificar la Configuración**: El archivo ya está preconfigurado, pero puedes verificar los siguientes campos:
    *   **Output file**: `target/FoodPlaza.exe` (donde se guardará el ejecutable).
    *   **Jar**: La ruta al "fat JAR" generado por Maven.
    *   **Icon**: La ruta al icono de la aplicación.
    *   **Bundled JRE path**: (Opcional) Si quieres incluir un JRE para que los usuarios no necesiten tener Java instalado.
4.  **Construir el Ejecutable**: Haz clic en el icono del engranaje (**Build wrapper**).
5.  **Verificar el Log**: Revisa la pestaña de logs en Launch4j para asegurarte de que no haya errores.
6.  **Encontrar el Ejecutable**: Si el proceso fue exitoso, encontrarás el archivo `FoodPlaza.exe` dentro del directorio `target/`.
