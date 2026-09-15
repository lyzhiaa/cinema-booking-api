package kh.edu.istad.moviebooking.features.payment;

import kh.edu.istad.moviebooking.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findPaymentByUuid(UUID uuid);

    List<Payment> findAllByBookingUuid(UUID bookingUuid);
}
