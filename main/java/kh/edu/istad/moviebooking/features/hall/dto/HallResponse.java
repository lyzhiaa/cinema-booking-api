package kh.edu.istad.moviebooking.features.hall.dto;

import kh.edu.istad.moviebooking.domain.enums.HallStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record HallResponse(
        UUID uuid,
        String name,
        String description,
        HallStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
