package com.financetracker.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating and validating JWT access tokens.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiry-ms:7200000}")
    private long expiryMs;

    /**
     * Generates a signed JWT token for the given user.
     *
     * @param userDetails the user to generate the token for
     * @return a signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        var now = Instant.now();
        var algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(expiryMs))
                .sign(algorithm);
    }

    /**
     * Validates the given JWT token against the provided user details.
     *
     * @param token the JWT token to validate
     * @param userDetails the user to validate the token against
     * @return true if the token is valid and belongs to the user, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            var verifier = JWT.require(algorithm).build();
            var decodedJwt = verifier.verify(token);
            return decodedJwt.getSubject().equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from a JWT token without full validation.
     *
     * @param token the JWT token
     * @return the username extracted from the token subject
     */
    public String extractUsername(String token) {
        return JWT.decode(token).getSubject();
    }

    /**
     * Returns the token expiry duration in milliseconds.
     *
     * @return expiry duration in milliseconds
     */
    public long getExpiryMs() {
        return expiryMs;
    }
}
