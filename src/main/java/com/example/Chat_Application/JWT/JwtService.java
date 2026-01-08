package com.example.Chat_Application.JWT;

import com.example.Chat_Application.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {


    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    //Extract the User ID from the JWT Token
    // ✅ FIXED: Handle Integer, Long, and String types
    public Long extractUserId(String jwtToken) {
        return extractClaim(jwtToken, claims -> {
            Object userIdObj = claims.get("userId");

            if (userIdObj == null) {
                return null;
            }

            // Handle different types
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            }

            throw new IllegalArgumentException("Invalid userId type in JWT: " + userIdObj.getClass());
        });
    }

    private <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String jwtToken) {

        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    public SecretKey getSignInKey(){
        // Secret key ko bytes mein convert karo
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(User user){

        return generateToken(new HashMap<>(), user);
    }

    public String generateToken(Map<String, Object> extraClaims, User user){
        Map<String, Object> claims = new HashMap<>(extraClaims);
        // ✅ Store as Long to avoid type conversion issues
        claims.put("userId", user.getId().longValue());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }


    public boolean isTokenValid(String jwtToken, User user){

        final Long userIdFromToken = extractUserId(jwtToken);

        final Long userId = user.getId();

        return (userIdFromToken != null && userIdFromToken.equals(userId) && !isTokenExpired(jwtToken));
    }

    private boolean isTokenExpired(String jwtToken){
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(String jwtToken){
        return extractClaim(jwtToken, Claims::getExpiration);
    }
}