package kh.edu.istad.moviebooking.features.concession;

import kh.edu.istad.moviebooking.domain.ConcessionOrder;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderStatus;
import kh.edu.istad.moviebooking.domain.enums.ConcessionOrderType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcessionOrderRepository extends JpaRepository<ConcessionOrder, Long> {

    @EntityGraph(attributePaths = {
            "booking",
            "booking.user",
            "items",
            "items.concessionItem"
    })
    Optional<ConcessionOrder> findByBooking_UuidAndStatus(UUID bookingUuid, ConcessionOrderStatus status);

    Optional<ConcessionOrder> findByBookingUuidAndTypeAndStatus(
            UUID bookingUuid,
            ConcessionOrderType type,
            ConcessionOrderStatus status
    );

    boolean existsByBookingUuidAndTypeAndStatus(
            UUID bookingUuid,
            ConcessionOrderType type,
            ConcessionOrderStatus status
    );

    Optional<ConcessionOrder> findConcessionOrderByUuid(UUID concessionOrderUuid);

    List<ConcessionOrder> findAllByBookingUserIdOrderByCreatedAtDesc(Long userId);


    List<ConcessionOrder> findAllByBookingUserIdAndTypeOrderByCreatedAtDesc(Long userId, ConcessionOrderType type);
}