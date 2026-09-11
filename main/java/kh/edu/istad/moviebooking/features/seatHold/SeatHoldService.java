package kh.edu.istad.moviebooking.features.seatHold;

import kh.edu.istad.moviebooking.features.seatHold.dto.CreateSeatHoldRequest;
import kh.edu.istad.moviebooking.features.seatHold.dto.SeatHoldResponse;

import java.util.UUID;

public interface SeatHoldService {
//    create hold seats
    SeatHoldResponse holdSeats(UUID showtimeUuid, CreateSeatHoldRequest createSeatHoldRequest);

//    remove if user does not book
    void releaseHold(UUID showtimeUuid, UUID holdId);
}
