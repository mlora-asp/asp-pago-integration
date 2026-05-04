package com.asp.integration.shared.constants;

public final class ResponseMessages {

    private ResponseMessages() {
    }

    public static final String BENEFICIARIO_REGISTRADO = "Beneficiario registrado correctamente";
    public static final String LOGIN_EXITOSO = "Login realizado exitosamente";
    public static final String LOGOUT_EXITOSO = "Logout realizado exitosamente";
    public static final String OTP_GENERADO = "OTP generado exitosamente";
    public static final String PASSWORD_ACTUALIZADO = "Password actualizado exitosamente";
    public static final String ONBOARDING_EXITOSO = "Onboarding procesado exitosamente";
    public static final String PAGO_CREDITO_EXITOSO = "Pago realizado exitosamente";
    public static final String OPERACION_EXITOSA = "Operación procesada exitosamente";
    public static final String CLIENTE_PROCESADO_PREFIX = "Cliente procesado: ";
    public static final String CUENTA_PROCESADA_PREFIX = "Cuenta procesada: ";

    public static final String PAYLOAD_CONTRATO_INVALIDO =
            "El payload desencriptado no corresponde al contrato esperado";
    public static final String CAMPOS_INVALIDOS_PREFIX = "Campos inválidos en la solicitud: ";
    public static final String CAMPOS_INVALIDOS = "Campos inválidos en la solicitud";
    public static final String SERIALIZAR_DATA_RESPUESTA_ERROR = "No fue posible serializar data de respuesta";

    public static final String ERROR_INTERNO_BRIDGE = "Error interno del Bridge de Integración";
    public static final String SERVICIO_EXTERNO_NO_DISPONIBLE =
            "Servicio externo temporalmente no disponible";
    public static final String ERROR_PROCESAR_SOLICITUD = "Ocurrió un error al procesar la solicitud";
    public static final String SOLICITUD_NO_PROCESADA = "La solicitud no pudo ser procesada";
    public static final String SERVICIO_CAUDEX_NO_DISPONIBLE =
            "Servicio Caudex temporalmente no disponible. Intente más tarde.";
    public static final String OPERACION_CAUDEX_NO_SOPORTADA_PREFIX = "Operación Caudex no soportada: ";
    public static final String OPERACION_NO_SOPORTADA_PREFIX = "Tipo de operación no soportado: ";
    public static final String ERROR_AUTENTICACION_CAUDEX_PREFIX = "Error de autenticación con Caudex HTTP ";
    public static final String ERROR_TECNICO_CAUDEX =
            "Caudex devolvió un error técnico al procesar la operación";
    public static final String ENDPOINT_CAUDEX_NO_CONFIGURADO_PREFIX =
            "Endpoint Caudex no configurado para operacion: ";

    public static final String TOKEN_CAUDEX_VACIO =
            "Caudex devolvió un token vacío — verificar configuración OAuth2";
    public static final String PARAMETROS_OAUTH_INVALIDOS =
            "Parámetros OAuth2 inválidos (grant_type, scope)";
    public static final String CREDENCIALES_CAUDEX_INVALIDAS =
            "Credenciales Caudex inválidas (client_id/client_secret)";
    public static final String PERMISOS_CAUDEX_INVALIDOS = "Sin permisos para obtener token Caudex";
    public static final String TOKEN_CAUDEX_RECHAZADO =
            "Token Caudex rechazado: credenciales inválidas o token expirado";
    public static final String OPERACION_CAUDEX_SIN_PERMISOS =
            "Sin permisos para ejecutar esta operación en Caudex";
    public static final String CREDENTIALS_CAUDEX_UNEXPECTED_PREFIX =
            "Error inesperado al obtener token Caudex HTTP ";

    public static final String PAYLOAD_CIFRADO_VACIO = "El payload cifrado está vacío o nulo";
    public static final String PAYLOAD_DESENCRIPTAR_ERROR =
            "No fue posible desencriptar la información recibida";
    public static final String PAYLOAD_ENCRIPTAR_VACIO =
            "La información a encriptar está vacía o nula";
    public static final String PAYLOAD_ENCRIPTAR_ERROR =
            "No fue posible encriptar la información de respuesta";
    public static final String PAYLOAD_JSON_INVALIDO =
            "El payload cifrado llegó en un formato JSON inválido";
    public static final String PAYLOAD_NORMALIZAR_ERROR =
            "No fue posible normalizar el payload cifrado recibido";
    public static final String CRYPTO_KEY_DERIVATION_ERROR =
            "No fue posible derivar la llave de cifrado";
    public static final String CRYPTO_DISABLED =
            "El soporte de crypto está deshabilitado en este entorno";
    public static final String CRYPTO_KEY_NOT_CONFIGURED = "La llave de crypto no está configurada";
}
