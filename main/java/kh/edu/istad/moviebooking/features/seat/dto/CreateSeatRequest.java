package kh.edu.istad.moviebooking.features.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kh.edu.istad.moviebooking.domain.enums.SeatType;

public record CreateSeatRequest(
        @NotBlank(message = "Row label is required")
        String rowLabel,

        @Positive(message = "Seat number must be greater than 0")
        Integer seatNumber,

        @NotNull(message = "Seat type is required")
        SeatType seatType,

        Integer xPosition,

        Integer yPosition
) {
}
