package kh.edu.istad.moviebooking.features.seatHold.dto;

import java.util.List;
import java.util.UUID;

public record SeatHoldResponse(

        UUID holdId,

        UUID showtimeUuid,

        List<UUID> seatUuids,

        Long expiresInSeconds
) {
}
