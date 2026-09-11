package kh.edu.istad.moviebooking.features.showtime.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record CreateShowtimeRequest(
        @NotNull(message = "Movie UUID is required")
        UUID movieUuid,

        @NotNull(message = "Hall UUID is required")
        UUID hallUuid,

        @NotNull(message = "Show date is required")
        @FutureOrPresent(message = "Show date cannot be in the past")
        LocalDate showDate,

        @NotNull(message = "Show time is required")
        LocalTime showTime,

        @NotNull(message = "Base price is required")
        @DecimalMin(
                value = "0.01",
                message = "Base price must be greater than 0"
        )
        BigDecimal basePrice
) {
}
