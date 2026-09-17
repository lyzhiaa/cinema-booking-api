package kh.edu.istad.moviebooking.features.seat.dto;

import kh.edu.istad.moviebooking.domain.enums.SeatAvailabilityStatus;

import java.util.List;
import java.util.UUID;

public record SeatAvailabilityEvent(
        UUID showtimeUuid,

        List<UUID> seatUuids,

        SeatAvailabilityStatus availability,

        Long expiresInSeconds
) {
}
