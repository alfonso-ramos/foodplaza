
# Manual Técnico - FoodPlaza

## 1. Presentación

Este documento técnico detalla la arquitectura, componentes y procesos del sistema FoodPlaza. Está dirigido a desarrolladores, personal de mantenimiento y cualquier persona que necesite comprender el funcionamiento interno de la aplicación.

## 2. Objetivo

El objetivo de este manual es proporcionar una guía completa sobre la estructura técnica del sistema para facilitar su mantenimiento, depuración y futuras ampliaciones. Se busca documentar los aspectos clave del desarrollo, desde la configuración del entorno hasta la descripción de la base de datos.

## 3. Procesos del Sistema

El sistema se basa en una arquitectura Modelo-Vista-Controlador (MVC) implementada en JavaFX.

*   **Autenticación:** El `AuthService` se encarga de verificar las credenciales del usuario contra un endpoint de la API. La sesión del usuario se gestiona a través del `SessionService`.
*   **Gestión de Datos (Plazas, Locales, etc.):** Cada entidad principal (Plaza, Local, Producto) tiene un `Service` asociado (`PlazaService`, `LocalService`, etc.) que se comunica con la API para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar).
*   **Navegación:** La navegación entre vistas (archivos `.fxml`) es gestionada por los controladores, que cargan las diferentes escenas según las acciones del usuario.

## 4. Requisitos del Sistema

*   **Hardware:**
    *   Mínimo 4 GB de RAM.
    *   Procesador de 1 GHz o superior.
*   **Software:**
    *   Java Development Kit (JDK) 11 o superior.
    *   Sistema Operativo: Windows, macOS o Linux.

## 5. Herramientas de Desarrollo

*   **Lenguaje:** Java 11
*   **Framework:** JavaFX
*   **Gestor de dependencias:** Maven
*   **Librerías principales:**
    *   `com.google.code.gson:gson`: Para el manejo de JSON.
    *   `org.openjfx:javafx-controls`, `javafx-fxml`: Para la interfaz de usuario.
*   **API:** El frontend consume una API REST para toda la gestión de datos.

## 6. Instalación de Aplicaciones

Para ejecutar el proyecto desde el código fuente, es necesario tener instalado Java (JDK 11+) y Maven. Se deben instalar las dependencias con `mvn install` y luego ejecutar la clase `Main`.

## 7. Modelo de Clases

*AQUÍ SE AGREGARÍA UNA IMAGEN DEL DIAGRAMA DE CLASES.*

Principales clases del modelo:

*   `Usuario.java`
*   `Plaza.java`
*   `Local.java`
*   `Menu.java`
*   `Producto.java`

## 8. Diagrama de Casos de Uso

*AQUÍ SE AGREGARÍA UNA IMAGEN DEL DIAGRAMA DE CASOS DE USO.*

## 9. Diagrama Entidad-Relación

*AQUÍ SE AGREGARÍA UNA IMAGEN DEL DIAGRAMA ENTIDAD-RELACIÓN DE LA BASE DE DATOS.*

## 10. Diccionario de Datos

*AQUÍ SE DESCRIBIRÍAN LAS TABLAS DE LA BASE DE DATOS.*

