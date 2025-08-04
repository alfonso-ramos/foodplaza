#!/bin/bash

# Configuración
APP_NAME="FoodPlaza"
VERSION="1.0.0"
JAR_FILE="target/foodplaza-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar"
OUTPUT_DIR="target/installer"

# Limpiar compilaciones anteriores
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Crear el ejecutable
echo "Creando ejecutable para Windows..."
jpackage \
  --name "$APP_NAME" \
  --input "$(dirname "$JAR_FILE")" \
  --main-jar "$(basename "$JAR_FILE")" \
  --main-class asedi.Main \
  --type app-image \
  --dest "$OUTPUT_DIR" \
  --app-version "$VERSION" \
  --vendor "Asedi" \
  --description "Aplicación FoodPlaza" \
  --java-options '--enable-preview'

echo "¡Listo! Ejecutable creado en: $OUTPUT_DIR/$APP_NAME/"
