package com.sc.property.management.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final Key key;

  public JwtUtil() {
    // we can change this based on the secret key if needed in future by using hmacShaKeyFor
    this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generates a secure key for HS256
  }

  // Generate JWT token
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(
            new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hours
        .signWith(key)
        .compact();
  }

  // Extract username from JWT token
  public String extractSub(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // Extract a specific claim from JWT token
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // Updated extractAllClaims to use the generated key
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key) // Use the same key that was used to sign the token
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // Check if token has expired
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // Extract expiration date from JWT token
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  // Validate token
  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractSub(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  // Generate a reset token
  public String generateResetToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", "reset"); // Add a specific claim to identify the reset token
    return createToken(claims, username);
  }

  // Validate reset token
  public boolean validateResetToken(String token, String email) {
    final String tokenEmail = extractSub(token);
    final String tokenType = extractClaim(token, claims -> claims.get("type", String.class));
    return (email.equals(tokenEmail) && "reset".equals(tokenType) && !isTokenExpired(token));
  }
}
