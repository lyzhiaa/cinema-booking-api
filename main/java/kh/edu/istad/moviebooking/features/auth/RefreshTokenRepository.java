package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    List<RefreshToken> findAllByUserUuidAndRevokedFalse(UUID userUuid);
}