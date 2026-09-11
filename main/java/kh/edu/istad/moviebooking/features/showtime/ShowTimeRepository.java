package kh.edu.istad.moviebooking.features.showtime;

import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.domain.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowTimeRepository extends JpaRepository<Showtime, Long> {
    Optional<Showtime> findShowtimeByUuid(UUID uuid);
    List<Showtime> findAllShowtimeByMovieUuid(UUID movieUuid);
    List<Showtime> findAllShowtimeByHallUuid(UUID hallUuid);
//    each hall cannot show 2 movies at the same time
    boolean existsByHallAndStartTimeLessThanAndEndTimeGreaterThan(
            Hall hall,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}
