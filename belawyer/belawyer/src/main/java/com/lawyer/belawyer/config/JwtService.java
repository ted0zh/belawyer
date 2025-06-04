//package com.lawyer.belawyer.config;
//
//
//import com.lawyer.belawyer.data.entity.User;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//public class JwtService {
//    @Value("${jwt.secret}")
//    private String SECRET_KEY;
//    public String extractUsername(String token){
//        return extractClaim(token,Claims::getSubject);
//    }
//    public boolean isValid(String token, UserDetails user){
//        String userName = extractUsername(token);
//        return userName.equals(user.getUsername()) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    private Date extractExpiration(String token) {
//        return extractClaim(token,Claims::getExpiration);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims,T> resolver){
//        Claims claims = extractAllClaims(token);
//        return resolver.apply(claims);
//    }
//    private Claims extractAllClaims(String token){
//        return Jwts
//                .parser()
//                .verifyWith(getSigninKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
//    public String generateToken(User user) {
//        Map<String,Object> claims = new HashMap<>();
//        claims.put("roles", List.of("ROLE_" + user.getRole().name()));
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(user.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
//                .signWith(getSigninKey())
//                .compact();
//    }
//    public String generateRefreshToken(User user) {
//        return Jwts.builder()
//                .setSubject(user.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
//                .signWith(getSigninKey())
//                .compact();
//    }
//
//    private SecretKey getSigninKey(){
//        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//}
package com.lawyer.belawyer.config;

import com.lawyer.belawyer.data.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

//@Service
//@Slf4j
//public class JwtService {
//
//    @Value("${jwt.secret}")
//    private String SECRET_KEY;
//
//    @Value("${jwt.expiration:86400000}") // 24 hours default
//    private long JWT_EXPIRATION;
//
//    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
//    private long REFRESH_EXPIRATION;
//
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public List<String> extractRoles(String token) {
//        Claims claims = extractAllClaims(token);
//        return claims.get("roles", List.class);
//    }
//
//    public boolean isValid(String token, UserDetails user) {
//        String userName = extractUsername(token);
//        return userName.equals(user.getUsername()) && !isTokenExpired(token);
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            return !isTokenExpired(token);
//        } catch (JwtException e) {
//            log.error("Invalid JWT token: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    public boolean isTokenExpired(String token) {
//        try {
//            return extractExpiration(token).before(new Date());
//        } catch (ExpiredJwtException e) {
//            log.warn("JWT token is expired: {}", e.getMessage());
//            return true;
//        }
//    }
//
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
//        Claims claims = extractAllClaims(token);
//        return resolver.apply(claims);
//    }
//
//    private Claims extractAllClaims(String token) {
//        try {
//            return Jwts
//                    .parser()
//                    .verifyWith(getSigninKey())
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//        } catch (JwtException e) {
//            log.error("Failed to parse JWT token: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    public String generateToken(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("roles", List.of("ROLE_" + user.getRole().name()));
//        claims.put("userId", user.getId());
//
//        return createToken(claims, user.getUsername(), JWT_EXPIRATION);
//    }
//
//    public String generateRefreshToken(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("type", "refresh");
//
//        return createToken(claims, user.getUsername(), REFRESH_EXPIRATION);
//    }
//
//    private String createToken(Map<String, Object> claims, String subject, long expiration) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(getSigninKey())
//                .compact();
//    }
//
//    public boolean canTokenBeRefreshed(String token) {
//        try {
//            return !isTokenExpired(token);
//        } catch (ExpiredJwtException e) {
//            // Token is expired, but we can still check if it's a valid refresh token
//            return e.getClaims().get("type") != null && "refresh".equals(e.getClaims().get("type"));
//        }
//    }
//
//    private SecretKey getSigninKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    public Long extractUserId(String token) {
//        Claims claims = extractAllClaims(token);
//        return claims.get("userId", Long.class);
//    }
//}

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Add roles to the JWT token
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        extraClaims.put("roles", roles);

        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}