package kh.edu.istad.moviebooking.features.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingRequest(
        @NotNull
        UUID userUuid,
        @NotNull(message = "Showtime UUID is required")
        UUID showtimeUuid,

        @NotNull(message = "Hold ID is required")
        UUID holdId
) {

}
