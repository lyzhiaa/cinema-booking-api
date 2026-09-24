package kh.edu.istad.moviebooking.features.booking;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.enums.BookingStatus;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findBookingByUuid(UUID uuid);

    boolean existsBookingByHoldId(UUID holdId);

    List<Booking> findAllByStatusAndPaymentExpiresAtBefore(BookingStatus status, LocalDateTime time);

    Optional<Booking> findByTicketQrToken(UUID uuid);

    Page<Booking> findAllByUserUuid(UUID userUuid, Pageable pageable);

    List<Booking> findAllByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            BookingStatus status
    );


}
