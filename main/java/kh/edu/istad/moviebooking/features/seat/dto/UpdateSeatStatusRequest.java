package kh.edu.istad.moviebooking.features.seat.dto;

import jakarta.validation.constraints.NotNull;
import kh.edu.istad.moviebooking.domain.enums.SeatStatus;

public record UpdateSeatStatusRequest(
        @NotNull(message = "Seat status is required")
        SeatStatus status
) {
}
