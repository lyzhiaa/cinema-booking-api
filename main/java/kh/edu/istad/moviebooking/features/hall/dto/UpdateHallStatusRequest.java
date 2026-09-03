package kh.edu.istad.moviebooking.features.hall.dto;

import kh.edu.istad.moviebooking.domain.enums.HallStatus;

public record UpdateHallStatusRequest(
        HallStatus status
) {
}
