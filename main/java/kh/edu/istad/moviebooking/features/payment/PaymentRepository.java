package kh.edu.istad.moviebooking.features.payment;

import kh.edu.istad.moviebooking.domain.Payment;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findPaymentByUuid(UUID uuid);

    List<Payment> findAllByBookingUuid(UUID bookingUuid);

    @EntityGraph(attributePaths = {
            "booking",
            "booking.showtime",
            "booking.showtime.movie",
            "booking.showtime.hall"
    })
    Page<Payment> findAllByBookingUserUuid(UUID userUuid, Pageable pageable);

    boolean existsByBookingUuidAndStatus(UUID bookingUuid, PaymentStatus status);
}
