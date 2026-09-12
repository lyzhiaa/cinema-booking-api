package kh.edu.istad.moviebooking.features.booking;

import kh.edu.istad.moviebooking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findBookingByUuid(UUID uuid);

    boolean existsBookingByHoldId(UUID holdId);
}
