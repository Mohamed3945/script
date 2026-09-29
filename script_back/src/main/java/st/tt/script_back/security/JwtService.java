package st.tt.script_back.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Crée et valide les jetons JWT utilisés par l'authentification de l'API.
 *
 * <p>Les jetons utilisent l'algorithme HMAC-SHA256 et contiennent l'identifiant
 * utilisateur, son nom, ses rôles, ainsi que leurs dates d'émission et d'expiration.
 */
@Service
public class JwtService {

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationMs;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${script.security.jwt.secret}") String secret,
            @Value("${script.security.jwt.expiration-ms}") long expirationMs) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
    }

    /**
     * Génère un jeton signé pour l'utilisateur fourni.
     *
     * @param userDetails identité et autorités de l'utilisateur authentifié
     * @return jeton JWT composé d'un en-tête, d'une charge utile et d'une signature
     */
    public String generateToken(AppUserDetails userDetails) {
        Instant now = Instant.now();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("userId", userDetails.getId());
        payload.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        payload.put("iat", now.toEpochMilli());
        payload.put("exp", now.plusMillis(expirationMs).toEpochMilli());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    /**
     * Extrait le sujet du jeton après vérification de sa structure et de sa signature.
     *
     * @param token jeton JWT à analyser
     * @return nom d'utilisateur porté par la propriété {@code sub}
     * @throws IllegalArgumentException si le jeton est invalide
     */
    public String extractUsername(String token) {
        return String.valueOf(readPayload(token).get("sub"));
    }

    /**
     * Vérifie que le jeton appartient à l'utilisateur et n'est pas expiré.
     *
     * @param token jeton JWT à vérifier
     * @param userDetails utilisateur auquel le jeton doit correspondre
     * @return {@code true} si le sujet, l'expiration et la signature sont valides
     * @throws IllegalArgumentException si la charge utile du jeton est illisible
     */
    public boolean isTokenValid(String token, AppUserDetails userDetails) {
        Map<String, Object> payload = readPayload(token);
        Object expiration = payload.get("exp");
        long expirationEpochMs = expiration instanceof Number number
                ? number.longValue()
                : Long.parseLong(String.valueOf(expiration));
        return userDetails.getUsername().equals(payload.get("sub"))
                && expirationEpochMs > Instant.now().toEpochMilli()
                && signatureMatches(token);
    }

    private Map<String, Object> readPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !signatureMatches(token)) {
                throw new IllegalArgumentException("Invalid token");
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token", ex);
        }
    }

    private boolean signatureMatches(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        return expectedSignature.equals(parts[2]);
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot encode token", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign token", ex);
        }
    }
}