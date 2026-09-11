package kh.edu.istad.moviebooking.features.seatGroup.dto;

import kh.edu.istad.moviebooking.domain.enums.SeatGroupType;
import kh.edu.istad.moviebooking.features.seat.dto.SeatResponse;

import java.util.List;
import java.util.UUID;

public record CoupleSeatResponse(
        UUID groupUuid,

        String label,

        SeatGroupType type,

        List<SeatResponse> seats
) {
}
