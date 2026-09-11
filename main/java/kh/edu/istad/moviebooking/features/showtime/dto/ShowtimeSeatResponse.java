package kh.edu.istad.moviebooking.features.showtime.dto;

import kh.edu.istad.moviebooking.domain.enums.SeatAvailabilityStatus;
import kh.edu.istad.moviebooking.domain.enums.SeatType;

import java.util.UUID;

public record ShowtimeSeatResponse(
        UUID seatUuid,

        UUID groupUuid,

        String rowLabel,

        Integer seatNumber,

        String seatLabel,

        SeatType seatType,

        SeatAvailabilityStatus availability
) {
}
