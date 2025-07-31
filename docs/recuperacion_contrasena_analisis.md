# Análisis: Recuperación de Contraseña

## Flujo de Recuperación

### 1. Solicitud de Recuperación
- **Pantalla de Inicio**: Opción "¿Olvidaste tu contraseña?"
- **Datos requeridos**: Correo electrónico o número de teléfono
- **Validación**: Verificar que el correo/teléfono exista en el sistema

### 2. Generación y Envío del Código
- **Código de 6 dígitos**
  - Generado aleatoriamente
  - Almacenado con timestamp de expiración (ej. 15 minutos)
  - Vinculado al usuario solicitante

- **Métodos de envío**:
  - **Correo Electrónico**:
    - Plantilla HTML con el código
    - Incluir instrucciones claras
    - Maravilloso si incluye un botón de acción directa
  
  - **SMS**:
    - Mensaje de texto con el código
    - Incluir nombre de la aplicación
    - Mensaje claro y conciso

### 3. Validación del Código
- **Pantalla de verificación**:
  - Campos para ingresar el código
  - Contador de tiempo restante
  - Opción para reenviar código

- **Validaciones**:
  - Código correcto
  - No expirado
  - No utilizado previamente

### 4. Cambio de Contraseña
- **Pantalla de nueva contraseña**:
  - Campo para nueva contraseña
  - Confirmación de contraseña
  - Requisitos de seguridad visibles

- **Validaciones**:
  - Las contraseñas coinciden
  - Cumple con políticas de seguridad
  - No puede ser igual a la anterior

## Requerimientos Técnicos

### Frontend (JavaFX)
1. **Pantallas Necesarias**:
   - Solicitud de recuperación
   - Ingreso de código
   - Nueva contraseña
   - Confirmación de éxito

2. **Validaciones en Tiempo Real**:
   - Formato de correo/teléfono
   - Formato del código (6 dígitos)
   - Fortaleza de la contraseña

### Backend (FastAPI)
1. **Endpoints Necesarios**:
   ```
   POST   /api/auth/forgot-password     # Iniciar recuperación
   POST   /api/auth/verify-code         # Validar código
   POST   /api/auth/reset-password      # Cambiar contraseña
   ```

2. **Modelo de Datos**:
   - Tabla `password_reset_codes`
     - id
     - user_id (FK)
     - code (6 dígitos)
     - created_at
     - expires_at
     - used (boolean)
     - method (email|sms)

## Consideraciones de Seguridad

1. **Protección contra fuerza bruta**:
   - Límite de intentos (ej. 3 intentos)
   - Tiempo de espera después de intentos fallidos

2. **Caducidad del Código**:
   - 15 minutos de validez
   - Un solo uso

3. **Registro de Actividad**:
   - Log de solicitudes de recuperación
   - IP y dispositivo de origen

4. **Mensajes Genéricos**:
   - No revelar si el correo/teléfono existe
   - Mensajes ambiguos en caso de error

## Mejoras Futuras

1. **Autenticación de Dos Factores (2FA)**:
   - Usar el mismo sistema para 2FA

2. **Notificaciones Push**:
   - Opción para recibir notificaciones en la app

3. **Preguntas de Seguridad**:
   - Como método alternativo de verificación

4. **Integración con Redes Sociales**:
   - Recuperación a través de cuentas vinculadas

## Próximos Pasos

1. Diseñar las pantallas de la interfaz de usuario
2. Implementar los endpoints del backend
3. Configurar el servicio de envío de correos/SMS
4. Desarrollar la lógica de generación y validación de códigos
5. Realizar pruebas de seguridad y usabilidad
