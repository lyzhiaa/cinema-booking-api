package kh.edu.istad.moviebooking.features.seat.dto;

import kh.edu.istad.moviebooking.domain.enums.SeatStatus;
import kh.edu.istad.moviebooking.domain.enums.SeatType;

import java.time.LocalDateTime;
import java.util.UUID;

public record SeatResponse(
        UUID uuid,

        UUID hallUuid,

        String rowLabel,

        Integer seatNumber,

        String seatLabel,

        SeatType seatType,

        SeatStatus status,

        Integer xPosition,

        Integer yPosition,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
