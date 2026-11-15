package com.chetan.dms.auth;

import com.chetan.dms.user.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirySeconds;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Value("${dms.security.jwt.expiry-seconds:3600}") long expirySeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirySeconds = expirySeconds;
    }

    public AuthResponse issueToken(UserAccount user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expirySeconds, ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("document-management-system")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("roles", java.util.List.of("ROLE_" + user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", expirySeconds, UserSummary.from(user));
    }
}
