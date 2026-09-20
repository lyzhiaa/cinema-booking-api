package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.domain.RefreshToken;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository
            refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Transactional
    public RefreshToken create(
            User user,
            String jti
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .jti(jti)
                        .user(user)
                        .expiresAt(
                                now.plusSeconds(
                                        refreshTokenExpiration
                                )
                        )
                        .revoked(false)
                        .createdAt(now)
                        .build();

        return refreshTokenRepository
                .save(refreshToken);
    }

    public RefreshToken findValidToken(
            String jti
    ) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByJti(jti)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Refresh token is invalid"
                                )
                        );

        if (Boolean.TRUE.equals(
                refreshToken.getRevoked()
        )) {

            throw new BadRequestException(
                    "Refresh token has been revoked"
            );
        }

        if (!refreshToken
                .getExpiresAt()
                .isAfter(
                        LocalDateTime.now()
                )) {

            throw new BadRequestException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(
            RefreshToken refreshToken
    ) {

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(
                refreshToken
        );
    }
}