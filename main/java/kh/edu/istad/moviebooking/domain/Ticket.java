package kh.edu.istad.moviebooking.domain;

import jakarta.persistence.*;
import kh.edu.istad.moviebooking.domain.enums.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_booking_seat",
                        columnNames = "booking_seat_id"
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "booking_id",
            nullable = false
    )
    private Booking booking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "booking_seat_id",
            nullable = false,
            unique = true
    )
    private BookingSeat bookingSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(
            name = "issued_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime issuedAt;

    @PrePersist
    public void prePersist() {

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        if (status == null) {
            status = TicketStatus.ISSUED;
        }

        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }
}