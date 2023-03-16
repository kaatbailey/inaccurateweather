package com.kaat.inaccurateweather;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    private static final String ISSUER = "InAccurateWeather";
    private static final int EXPIRATION_DAYS = 30;

    public String createToken(String zipCode) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plus(EXPIRATION_DAYS, ChronoUnit.DAYS));

        Claims claims = Jwts.claims().setSubject("user");
        claims.put("zipCode", zipCode);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(ISSUER)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(SECRET_KEY)
                .compact();

        return token;
    }

    public String getZipCodeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String zipCode = claims.get("zipCode", String.class);

        return zipCode;
    }
}
