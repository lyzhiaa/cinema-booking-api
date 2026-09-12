package kh.edu.istad.moviebooking.features.booking;

import kh.edu.istad.moviebooking.domain.BookingSeat;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findAllBookingSeatByBookingUuid(UUID bookingUuid);

    boolean existsByBookingShowtimeUuidAndSeatUuidAndBookingStatusIn(
            UUID showtimeUuid,
            UUID seatUuid,
            Collection<BookingStatus> statuses
    );
}
