package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder refreshJwtDecoder;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public JwtService(JwtEncoder jwtEncoder, @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder) {

        this.jwtEncoder = jwtEncoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
    }

    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                        .issuer(issuer)
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(accessTokenExpiration))
                        .subject(user.getUuid().toString())
                        .claim("type", "access")
                        .claim("username", user.getUsername())
                        .claim("role", user.getRole().getName())
                        .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    public String generateRefreshToken(User user, String jti) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                        .issuer(issuer)
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(refreshTokenExpiration))
                        .subject(user.getUuid().toString())
                        .id(jti)
                        .claim("type", "refresh")
                        .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Jwt decodeRefreshToken(String token) {

        try {
            return refreshJwtDecoder.decode(token);

        } catch (JwtException e) {

            throw new BadRequestException("Invalid or expired refresh token"
            );
        }
    }
}