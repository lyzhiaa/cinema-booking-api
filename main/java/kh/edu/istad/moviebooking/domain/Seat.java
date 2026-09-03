package kh.edu.istad.moviebooking.domain;

import jakarta.persistence.*;
import kh.edu.istad.moviebooking.domain.enums.SeatStatus;
import kh.edu.istad.moviebooking.domain.enums.SeatType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_hall_label",
                        columnNames = {"hall_id", "seat_label"}
                )
        }
)
@NoArgsConstructor
public class Seat {

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
            name = "hall_id",
            nullable = false
    )
    private Hall hall;

    @Column(
            name = "row_label",
            nullable = false
    )
    private String rowLabel;

    @Column(
            name = "seat_number",
            nullable = false
    )
    private Integer seatNumber;

    @Column(
            name = "seat_label",
            nullable = false
    )
    private String seatLabel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "seat_type",
            nullable = false
    )
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(name = "x_position")
    private Integer xPosition;

    @Column(name = "y_position")
    private Integer yPosition;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        if (status == null) {
            status = SeatStatus.ACTIVE;
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_group_id")
    private SeatGroup seatGroup;
}
