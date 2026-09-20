package kh.edu.istad.moviebooking.features.ticket;

import kh.edu.istad.moviebooking.domain.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findTicketByUuid(UUID uuid);

    List<Ticket> findAllByBookingUuid(UUID bookingUuid);

    boolean existsByBookingSeatId(Long bookingSeatId);

    boolean existsByBookingSeat_Id(Long bookingSeatId);

    Page<Ticket> findAllByBookingUserUuid(
            UUID userUuid,
            Pageable pageable
    );
}
