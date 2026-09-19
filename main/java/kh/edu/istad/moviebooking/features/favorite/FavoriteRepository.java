package kh.edu.istad.moviebooking.features.favorite;

import kh.edu.istad.moviebooking.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserUuidAndMovieUuid(UUID userUuid, UUID movieUuid);

    Optional<Favorite> findByUserUuidAndMovieUuid(UUID userUuid, UUID movieUuid);

    List<Favorite> findAllByUserUuidOrderByCreatedAtDesc(UUID userUuid);
}
