package com.asp.integration.infrastructure.security.crypto;

import com.asp.integration.application.port.inbound.PayloadCryptoPort;
import com.asp.integration.domain.exception.PayloadDecryptionException;
import com.asp.integration.infrastructure.config.properties.CryptoProperties;
import com.asp.integration.shared.constants.ResponseMessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Implementación compatible con el esquema de cifrado del canal ASP Pago.
 *
 * Compatibilidad:
 * - AES/CBC/PKCS5Padding
 * - key + iv derivados de SHA-512(secretKey)
 * - payload transportado como Base64 y frecuentemente URL-encoded
 *
 * Nota de seguridad: para evolución futura conviene migrar a AES-GCM
 * con IV aleatorio y autenticación integrada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AesCbcPayloadCryptoService implements PayloadCryptoPort {

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-512";
    private static final HexFormat HEX = HexFormat.of();

    private final CryptoProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String decryptRequest(String encryptedPayload) {
        ensureEnabledAndConfigured();

        if (!StringUtils.hasText(encryptedPayload)) {
            throw new PayloadDecryptionException(ResponseMessages.PAYLOAD_CIFRADO_VACIO);
        }

        String normalizedPayload = normalizeEncryptedPayload(encryptedPayload);
        String keySource = generateKeySource(properties.getSecretKey());
        String keyHex = keySource.substring(0, 32);
        String ivHex = keySource.substring(32, 64);

        try {
            byte[] keyBytes = HEX.parseHex(keyHex);
            byte[] ivBytes = HEX.parseHex(ivHex);
            byte[] encryptedBytes = Base64.getDecoder().decode(normalizedPayload);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM), new IvParameterSpec(ivBytes));

            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new PayloadDecryptionException(ResponseMessages.PAYLOAD_DESENCRIPTAR_ERROR, ex);
        }
    }

    @Override
    public String encryptResponse(String plainPayload) {
        ensureEnabledAndConfigured();

        if (!StringUtils.hasText(plainPayload)) {
            throw new PayloadDecryptionException(ResponseMessages.PAYLOAD_ENCRIPTAR_VACIO);
        }

        String keySource = generateKeySource(properties.getSecretKey());
        String keyHex = keySource.substring(0, 32);
        String ivHex = keySource.substring(32, 64);

        try {
            byte[] keyBytes = HEX.parseHex(keyHex);
            byte[] ivBytes = HEX.parseHex(ivHex);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM), new IvParameterSpec(ivBytes));

            byte[] encryptedBytes = cipher.doFinal(plainPayload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception ex) {
            throw new PayloadDecryptionException(ResponseMessages.PAYLOAD_ENCRIPTAR_ERROR, ex);
        }
    }

    private String normalizeEncryptedPayload(String encryptedPayload) {
        try {
            String decoded = URLDecoder.decode(encryptedPayload, Charset.forName(properties.getEncoding()));
            decoded = decoded.replace(" ", "+").trim();

            if (decoded.startsWith("{") && decoded.endsWith("}")) {
                JsonNode node = objectMapper.readTree(decoded);
                JsonNode dataNode = node.get("data");
                if (dataNode != null && dataNode.isTextual()) {
                    return dataNode.asText();
                }
            }

            if (decoded.startsWith("\"") && decoded.endsWith("\"") && decoded.length() >= 2) {
                return decoded.substring(1, decoded.length() - 1);
            }

            // Algunos clientes agregan una llave de cierre extra al payload cifrado.
            if (!decoded.startsWith("{") && decoded.endsWith("}")) {
                return decoded.substring(0, decoded.length() - 1);
            }

            return decoded;
        } catch (JsonProcessingException ex) {
            throw new PayloadDecryptionException(ResponseMessages.PAYLOAD_JSON_INVALIDO, ex);
        } catch (Exception ex) {
            throw new PayloadDecryptionException(ResponseMessages.PAYLOAD_NORMALIZAR_ERROR, ex);
        }
    }

    private String generateKeySource(String secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(hash);
        } catch (Exception ex) {
            throw new PayloadDecryptionException(ResponseMessages.CRYPTO_KEY_DERIVATION_ERROR, ex);
        }
    }

    private void ensureEnabledAndConfigured() {
        if (!properties.isEnabled()) {
            throw new PayloadDecryptionException(ResponseMessages.CRYPTO_DISABLED);
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new PayloadDecryptionException(ResponseMessages.CRYPTO_KEY_NOT_CONFIGURED);
        }
    }
}
