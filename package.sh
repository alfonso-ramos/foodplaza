#!/bin/bash

# Colores para la salida
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    echo -e "${YELLOW}Error: No se encontró el archivo pom.xml. Ejecuta este script desde el directorio raíz del proyecto.${NC}"
    exit 1
fi

# Limpiar y construir el proyecto
echo -e "${GREEN}Limpiando y construyendo el proyecto...${NC}"
mvn clean package

# Verificar si la construcción fue exitosa
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}Error al construir el proyecto. Por favor, corrige los errores e inténtalo de nuevo.${NC}"
    exit 1
fi

# Verificar si jpackage está instalado
if ! command -v jpackage &> /dev/null; then
    echo -e "${YELLOW}jpackage no está instalado. Asegúrate de tener instalado JDK 16 o superior.${NC}"
    exit 1
fi

# Crear directorio de salida si no existe
mkdir -p target/package

# Obtener información del proyecto
APP_NAME="FoodPlaza"
APP_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
APP_VENDOR="TuEmpresa"
MAIN_CLASS="asedi.Main"
JAR_PATH="target/foodplaza-frontend-${APP_VERSION}-jar-with-dependencies.jar"

# Verificar si el archivo JAR existe
if [ ! -f "$JAR_PATH" ]; then
    echo -e "${YELLOW}Error: No se encontró el archivo JAR en $JAR_PATH${NC}"
    exit 1
fi

# Crear el paquete
echo -e "${GREEN}Creando paquete de la aplicación...${NC}"

# Comando base para jpackage
jpackage \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "$APP_VENDOR" \
  --description "Aplicación de Gestión para FoodPlaza" \
  --type app-image \
  --input target \
  --main-jar "foodplaza-frontend-${APP_VERSION}-jar-with-dependencies.jar" \
  --main-class "$MAIN_CLASS" \
  --dest target/package \
  --runtime-image "$JAVA_HOME" \
  --java-options "--enable-preview" \
  --java-options "-Dfile.encoding=UTF-8" \
  --icon src/main/resources/images/logo.png \
  --win-console

# Verificar si jpackage se ejecutó correctamente
if [ $? -eq 0 ]; then
    echo -e "${GREEN}¡Paquete creado exitosamente en target/package/!${NC}"
    echo -e "${GREEN}Puedes encontrar la aplicación en: $(pwd)/target/package/${APP_NAME}/"
else
    echo -e "${YELLOW}Error al crear el paquete. Por favor, revisa los mensajes de error.${NC}"
    exit 1
fi
