package kh.edu.istad.moviebooking.features.seatReservation;

import kh.edu.istad.moviebooking.domain.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    boolean existsByShowtimeUuidAndSeatUuid(UUID showtimeUuid, UUID seatUuid);

    List<SeatReservation> findAllByBookingUuid(UUID bookingUuid);

    void deleteAllByBookingUuid(UUID bookingUuid);
}
