package kh.edu.istad.moviebooking.features.showtime.dto;

import kh.edu.istad.moviebooking.domain.enums.ShowtimeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShowtimeResponse(
        UUID uuid,

        UUID movieUuid,
        String movieTitle,

        UUID hallUuid,
        String hallName,

        LocalDateTime startTime,
        LocalDateTime endTime,

        BigDecimal basePrice,

        ShowtimeStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
