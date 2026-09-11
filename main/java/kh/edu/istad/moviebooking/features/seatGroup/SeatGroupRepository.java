package kh.edu.istad.moviebooking.features.seatGroup;

import kh.edu.istad.moviebooking.domain.SeatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SeatGroupRepository extends JpaRepository<SeatGroup, Long> {
    Optional<SeatGroup> findSeatGroupByUuid(UUID uuid);

    boolean existsByHallUuidAndLabel(UUID hallUuid, String label);
}
