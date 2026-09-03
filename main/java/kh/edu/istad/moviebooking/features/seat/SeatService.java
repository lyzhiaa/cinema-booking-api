package kh.edu.istad.moviebooking.features.seat;

import kh.edu.istad.moviebooking.features.seat.dto.CreateSeatRequest;
import kh.edu.istad.moviebooking.features.seat.dto.SeatResponse;
import kh.edu.istad.moviebooking.features.seat.dto.UpdateSeatStatusRequest;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    SeatResponse createSeat(
            UUID hallUuid,
            CreateSeatRequest createSeatRequest
    );

    List<SeatResponse> getSeatsByHallUuid(
            UUID hallUuid
    );

    SeatResponse getSeatByUuid(
            UUID uuid
    );

    SeatResponse updateSeatStatus(
            UUID uuid,
            UpdateSeatStatusRequest updateSeatStatusRequest
    );
}
