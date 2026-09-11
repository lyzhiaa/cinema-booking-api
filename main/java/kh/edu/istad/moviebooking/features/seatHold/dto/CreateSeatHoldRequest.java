package kh.edu.istad.moviebooking.features.seatHold.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateSeatHoldRequest(
        @NotEmpty(message = "At least one seat is required")
        List<UUID> seatUuids
) {
}
