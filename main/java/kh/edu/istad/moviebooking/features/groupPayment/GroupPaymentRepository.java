package kh.edu.istad.moviebooking.features.groupPayment;

import kh.edu.istad.moviebooking.domain.GroupPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupPaymentRepository extends JpaRepository<GroupPayment, Long> {

    Optional<GroupPayment> findByUuid(UUID uuid);

    Optional<GroupPayment> findByGroupBookingUuid(UUID groupUuid);

    boolean existsByGroupBookingUuid(UUID groupUuid);


}