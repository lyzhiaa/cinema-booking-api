package kh.edu.istad.moviebooking.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_showtime_seat_reservation",
                        columnNames = {
                                "showtime_id",
                                "seat_id"
                        }
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "showtime_id",
            nullable = false
    )
    private Showtime showtime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seat_id",
            nullable = false
    )
    private Seat seat;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "booking_id",
            nullable = false
    )
    private Booking booking;


    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
