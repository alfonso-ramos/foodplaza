#!/bin/bash

# Configuración
APP_NAME="FoodPlaza"
VERSION="1.0.0"
VENDOR="Asedi"
COPYRIGHT="Copyright © 2025 Asedi"
DESCRIPTION="Aplicación FoodPlaza"
MAIN_CLASS="asedi.Main"
JAR_FILE="target/foodplaza-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar"
OUTPUT_DIR="target/installer"
RUNTIME_DIR="target/runtime"

# Limpiar compilaciones anteriores
rm -rf "$OUTPUT_DIR" "$RUNTIME_DIR"

# Crear directorios necesarios
mkdir -p "$OUTPUT_DIR"

# 1. Crear runtime personalizado
echo "Creando runtime personalizado..."
MODULES=$(jdeps --print-module-deps --ignore-missing-deps "$JAR_FILE")

jlink \
  --add-modules "$MODULES,java.sql,java.naming,java.desktop,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.web" \
  --output "$RUNTIME_DIR" \
  --strip-debug \
  --compress=2 \
  --no-header-files \
  --no-man-pages

# 2. Crear instalador .deb
echo "\nCreando instalador .deb..."
jpackage \
  --name "$APP_NAME" \
  --input "$(dirname "$JAR_FILE")" \
  --main-jar "$(basename "$JAR_FILE")" \
  --main-class "$MAIN_CLASS" \
  --runtime-image "$RUNTIME_DIR" \
  --dest "$OUTPUT_DIR" \
  --app-version "$VERSION" \
  --vendor "$VENDOR" \
  --copyright "$COPYRIGHT" \
  --description "$DESCRIPTION" \
  --type deb \
  --linux-shortcut \
  --linux-menu-group "$APP_NAME" \
  --linux-package-deps "libx11-6, libopenal1, libgl1" \
  --java-options '--enable-preview' \
  --icon "src/main/resources/asedi/images/logo.png" 2>/dev/null || echo "No se encontró el ícono, continuando sin él"

# 3. Crear versión portable (app-image)
echo "\nCreando versión portable..."
jpackage \
  --name "${APP_NAME}-Portable" \
  --input "$(dirname "$JAR_FILE")" \
  --main-jar "$(basename "$JAR_FILE")" \
  --main-class "$MAIN_CLASS" \
  --runtime-image "$RUNTIME_DIR" \
  --dest "$OUTPUT_DIR" \
  --app-version "$VERSION" \
  --vendor "$VENDOR" \
  --copyright "$COPYRIGHT" \
  --description "$DESCRIPTION (Portable)" \
  --type app-image \
  --java-options '--enable-preview'

echo "\n¡Proceso completado!"
echo "- Instalador .deb: $OUTPUT_DIR/${APP_NAME}_${VERSION}-1_amd64.deb"
echo "- Versión portable: $OUTPUT_DIR/${APP_NAME}-Portable/${APP_NAME}-Portable"

# Hacer ejecutable el script de la versión portable
chmod +x "$OUTPUT_DIR/${APP_NAME}-Portable/${APP_NAME}-Portable" 2>/dev/null || :
