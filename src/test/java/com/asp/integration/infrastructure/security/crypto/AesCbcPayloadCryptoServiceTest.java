package com.asp.integration.infrastructure.security.crypto;

import com.asp.integration.domain.exception.PayloadDecryptionException;
import com.asp.integration.infrastructure.config.properties.CryptoProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesCbcPayloadCryptoServiceTest {

    private static final String SECRET = "compat-secret-asp";
    private static final String JSON = """
            {"tipoOperacion":"CAUDEX_CONSULTA_SALDOS_VISTA","referencia":"ENC-001","canal":"API"}
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void decryptRequest_shouldDecryptCompatiblePayload() {
        AesCbcPayloadCryptoService service = buildService(true, SECRET);

        String encrypted = encrypt(SECRET, JSON);

        assertThat(service.decryptRequest(encrypted)).isEqualTo(JSON);
    }

    @Test
    void decryptRequest_shouldHandleUrlEncodedPayloadAndJsonWrapper() {
        AesCbcPayloadCryptoService service = buildService(true, SECRET);

        String encrypted = encrypt(SECRET, JSON);
        String wrapped = "{\"data\":\"" + URLEncoder.encode(encrypted, StandardCharsets.UTF_8) + "\"}";

        assertThat(service.decryptRequest(wrapped)).isEqualTo(JSON);
    }

    @Test
    void encryptResponse_shouldRemainRoundTripCompatible() {
        AesCbcPayloadCryptoService service = buildService(true, SECRET);

        String encrypted = service.encryptResponse(JSON);

        assertThat(service.decryptRequest(encrypted)).isEqualTo(JSON);
    }

    @Test
    void decryptRequest_shouldFailWhenSecretIsMissing() {
        AesCbcPayloadCryptoService service = buildService(true, "");

        assertThatThrownBy(() -> service.decryptRequest("abc"))
                .isInstanceOf(PayloadDecryptionException.class)
                .hasMessageContaining("llave de crypto no está configurada");
    }

    @Test
    void decryptRequest_shouldFailWhenCryptoIsDisabled() {
        AesCbcPayloadCryptoService service = buildService(false, SECRET);

        assertThatThrownBy(() -> service.decryptRequest("abc"))
                .isInstanceOf(PayloadDecryptionException.class)
                .hasMessageContaining("deshabilitado");
    }

    private AesCbcPayloadCryptoService buildService(boolean enabled, String secret) {
        CryptoProperties properties = new CryptoProperties();
        properties.setEnabled(enabled);
        properties.setSecretKey(secret);
        properties.setEncoding("UTF-8");
        return new AesCbcPayloadCryptoService(properties, objectMapper);
    }

    private String encrypt(String secret, String plainText) {
        try {
            String keySource = sha512Hex(secret);
            byte[] keyBytes = HexFormat.of().parseHex(keySource.substring(0, 32));
            byte[] ivBytes = HexFormat.of().parseHex(keySource.substring(32, 64));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivBytes));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String sha512Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-512").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
