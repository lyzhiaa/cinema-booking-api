package kh.edu.istad.moviebooking.features.hall.dto;

import kh.edu.istad.moviebooking.domain.enums.HallStatus;
import kh.edu.istad.moviebooking.domain.enums.HallType;

import java.time.LocalDateTime;
import java.util.UUID;

public record HallResponse(
        UUID uuid,
        String name,
        String description,
        Integer capacity,
        HallType hallType,
        HallStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
//        update

) {
}
