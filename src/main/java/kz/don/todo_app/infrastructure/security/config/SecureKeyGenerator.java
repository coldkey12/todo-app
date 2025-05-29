package kz.don.todo_app.infrastructure.security.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class SecureKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("Secure JWT Key: " + base64Key);
        System.out.println("Key length: " + base64Key.length() + " characters");
    }
}