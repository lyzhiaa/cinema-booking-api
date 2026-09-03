package kh.edu.istad.moviebooking.features.seat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCreateSeatRequest(
        @NotEmpty(message = "At least one row is required")
        List<@Valid SeatRowRequest> rows
) {
}
