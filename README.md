# FoodPlaza Frontend

Aplicación de escritorio desarrollada con JavaFX para la gestión de pedidos de FoodPlaza.

## Requisitos Previos

- Java JDK 21 o superior
- Maven 3.6.0 o superior

## Estructura del Proyecto

```
src/
  main/
    java/
      asedi/
        Main.java              # Clase principal de la aplicación
        controllers/
          InicioController.java # Controlador para la vista principal
    resources/
      views/
        inicio.fxml           # Vista principal en formato FXML
```

## Cómo Ejecutar la Aplicación

### Usando Maven (Recomendado)

Para compilar y ejecutar la aplicación, usa el siguiente comando en la raíz del proyecto:

```bash
mvn clean javafx:run
```

Este comando:
1. Limpia el directorio `target/`
2. Compila el código fuente
3. Ejecuta la aplicación JavaFX

### Opciones Adicionales

- **Ejecutar con depuración activada**:
  ```bash
  mvn clean javafx:run -Djavafx.verbose
  ```

- **Limpiar y empaquetar sin ejecutar**:
  ```bash
  mvn clean package
  ```

## Configuración

La aplicación está configurada para usar JavaFX 21.0.1. Las dependencias se gestionan a través de Maven y están definidas en el archivo `pom.xml`.

## Solución de Problemas

Si encuentras el error "JavaFX runtime components are missing", asegúrate de:

1. Tener instalado Java JDK 21 o superior
2. Usar el comando `mvn clean javafx:run` en lugar de ejecutar directamente la clase Main
3. Verificar que todas las dependencias se hayan descargado correctamente con `mvn dependency:resolve`

## Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).
