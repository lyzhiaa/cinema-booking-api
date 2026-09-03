package kh.edu.istad.moviebooking.features.seat;

import kh.edu.istad.moviebooking.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findSeatByUuid(UUID uuid);

    List<Seat> findAllSeatByHallUuidOrderByRowLabelAscSeatNumberAsc(
            UUID hallUuid
    );

    boolean existsByHallUuidAndSeatLabel(
            UUID hallUuid,
            String seatLabel
    );

    long countByHallUuid(UUID hallUuid);

}
