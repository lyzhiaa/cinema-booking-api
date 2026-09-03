package kh.edu.istad.moviebooking.features.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kh.edu.istad.moviebooking.domain.enums.SeatType;

public record SeatRowRequest(
        @NotBlank(message = "Row label is required")
        String rowLabel,

        @NotNull(message = "Number of seats is required")
        @Positive(message = "Number of seats must be greater than 0")
        Integer numberOfSeats,

        @NotNull(message = "Seat type is required")
        SeatType seatType
) {
}
