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

    public String extractUsername(String token) {
        return String.valueOf(readPayload(token).get("sub"));
    }

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