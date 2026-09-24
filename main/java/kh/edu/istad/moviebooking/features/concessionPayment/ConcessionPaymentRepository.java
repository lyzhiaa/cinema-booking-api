package kh.edu.istad.moviebooking.features.concessionPayment;

import kh.edu.istad.moviebooking.domain.ConcessionPayment;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConcessionPaymentRepository extends JpaRepository<ConcessionPayment, Long> {

    Optional<ConcessionPayment> findByUuid(UUID uuid);

    Optional<ConcessionPayment> findByConcessionOrder_Uuid(UUID concessionOrderUuid);

    boolean existsByConcessionOrder_UuidAndStatus(UUID concessionOrderUuid, PaymentStatus status);
}
