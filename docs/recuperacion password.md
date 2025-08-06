al dar click en recuperar contraseña, el usuario tendra que ingresar su correo electronico, se le mandara un codigo de 6 digitosde un solo uso, el cual agrega al sistema, el usuario tendra que ingresar el codigo y su nueva contraseña.

endpoint para pedir codigo: POST /api/auth/password-reset/request


body:
{
  "email": "user@example.com"
}

Responses
Code	Description	Links
200	

Código de verificación enviado exitosamente
Media type
Controls Accept header.

{
  "message": "Se ha enviado un código de verificación a tu correo electrónico",
  "email": "usuario@ejemplo.com"
}

	No links
404	

No existe una cuenta con este correo electrónico
Media type

{
  "detail": "No existe una cuenta con este correo electrónico"
}

	No links
422	

Validation Error
Media type

{
  "detail": [
    {
      "loc": [
        "string",
        0
      ],
      "msg": "string",
      "type": "string"
    }
  ]
}

	No links
500	

Error interno del servidor
Media type

{
  "detail": "Ocurrió un error al procesar tu solicitud"
}

	No links


Endpoint para recuperr contraseña POST: /api/auth/password-reset/verify

body:
{
  "email": "user@example.com",
  "code": "123456",
  "new_password": "newpassword"
}

Responses
Code	Description	Links
200	

Contraseña actualizada exitosamente
Media type
Controls Accept header.

"string"

	No links
400	

Código de verificación inválido o expirado

"string"

	No links
404	

Usuario no encontrado

"string"

	No links
422	

Error de validación en los datos proporcionados

"string"

	No links
500	

Error interno del servidor

"string"