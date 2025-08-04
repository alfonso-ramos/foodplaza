# Empaquetado de la Aplicación FoodPlaza

Esta guía explica cómo empaquetar la aplicación FoodPlaza para distribución.

## Requisitos Previos

- JDK 21 o superior
- Maven 3.6.3 o superior
- jpackage (incluido en JDK 14+)
- Sistema operativo: Windows, macOS o Linux

## Pasos para Empaquetar

### 1. Configuración Inicial

Asegúrate de que tu entorno cumple con los requisitos:

```bash
java -version  # Debe ser Java 21 o superior
mvn -version   # Debe ser Maven 3.6.3 o superior
jpackage --version  # Debe estar disponible
```

### 2. Construir el Proyecto

Primero, construye el proyecto con Maven:

```bash
mvn clean package
```

Esto generará un archivo JAR en `target/foodplaza-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar`.

### 3. Opciones de Empaquetado

#### Opción 1: Usar el Script de Empaquetado (Recomendado)

Ejecuta el script `package.sh`:

```bash
./package.sh
```

El script realizará los siguientes pasos:
1. Limpiar y construir el proyecto
2. Verificar las dependencias
3. Crear un paquete ejecutable usando jpackage
4. Colocar los archivos resultantes en `target/package/`

#### Opción 2: Comandos Manuales

Si prefieres ejecutar los comandos manualmente:

```bash
# Construir el proyecto
mvn clean package

# Crear el paquete con jpackage
jpackage \
  --name "FoodPlaza" \
  --app-version "1.0.0" \
  --vendor "TuEmpresa" \
  --description "Aplicación de Gestión para FoodPlaza" \
  --type app-image \
  --input target \
  --main-jar "foodplaza-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  --main-class "asedi.Main" \
  --dest target/package \
  --runtime-image "$JAVA_HOME" \
  --java-options "--enable-preview" \
  --java-options "-Dfile.encoding=UTF-8" \
  --icon src/main/resources/images/logo.png \
  --win-console
```

## Personalización

Puedes personalizar los siguientes parámetros en el archivo `pom.xml`:

- `app.name`: Nombre de la aplicación
- `app.version`: Versión de la aplicación
- `app.vendor`: Nombre del desarrollador o empresa
- `main.class`: Clase principal de la aplicación

## Notas Importantes

- Asegúrate de tener suficiente espacio en disco (al menos 1GB libre)
- El proceso de empaquetado puede tardar varios minutos
- Para distribuir la aplicación, puedes comprimir el directorio generado en `target/package/`
- En Windows, asegúrate de ejecutar el comando como administrador si ves errores de permisos

## Solución de Problemas

### Error: "jpackage no está instalado"
Asegúrate de tener instalado JDK 16 o superior y que `jpackage` esté en tu PATH.

### Error: "No se encontró el archivo JAR"
Verifica que el proyecto se haya construido correctamente con `mvn clean package`.

### Error de permisos en Linux/macOS
Si el script no es ejecutable, ejecuta:
```bash
chmod +x package.sh
```
