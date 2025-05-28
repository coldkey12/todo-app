package kz.don.todo_app.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import kz.don.todo_app.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessExpiration);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpiration);
    }

    private String buildToken(User user, long expiration) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("userId", user.getId().toString());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())  // Use getSigningKey() instead of raw secret
                .parseClaimsJws(token)
                .getBody();
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        final String username = getUsernameFromToken(jwt);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
    }

    private boolean isTokenExpired(String jwt) {
        final Date expiration = getClaimsFromToken(jwt).getExpiration();
        return expiration.before(new Date());
    }

    public String extractUsername(String jwt) {
        try {
            return getClaimsFromToken(jwt).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenStructureValid(String token) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token);  // Use getSigningKey()
            return true;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateTokenSignature(String token) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token);  // Use getSigningKey()
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }

    public UUID getUserIdFromTokenIgnoreExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())  // Use getSigningKey()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = (String) claims.get("userId");
            return UUID.fromString(userId);
        } catch (ExpiredJwtException e) {
            String userId = (String) e.getClaims().get("userId");
            return UUID.fromString(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token format");
        }
    }

}