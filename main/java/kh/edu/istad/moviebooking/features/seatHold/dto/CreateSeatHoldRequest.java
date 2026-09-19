package kh.edu.istad.moviebooking.features.seatHold.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateSeatHoldRequest(
        @NotNull(message = "User UUID is required")
        UUID userUuid,

        @NotEmpty(message = "At least one seat is required")
        List<UUID> seatUuids
) {
}
