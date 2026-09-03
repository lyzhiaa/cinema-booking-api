package kh.edu.istad.moviebooking.features.seat;

import kh.edu.istad.moviebooking.features.seat.dto.BulkCreateSeatRequest;
import kh.edu.istad.moviebooking.features.seat.dto.CreateSeatRequest;
import kh.edu.istad.moviebooking.features.seat.dto.SeatResponse;
import kh.edu.istad.moviebooking.features.seat.dto.UpdateSeatStatusRequest;

import java.util.List;
import java.util.UUID;

public interface SeatService {
//    create seat
    SeatResponse createSeat(UUID hallUuid, CreateSeatRequest createSeatRequest);

//    create multiple seats
    List<SeatResponse> createSeatsBulk(UUID hallUuid, BulkCreateSeatRequest bulkCreateSeatRequest);

//    get seat by hall uuid
    List<SeatResponse> getSeatsByHallUuid(UUID hallUuid);

//    get seat by uuid
    SeatResponse getSeatByUuid(UUID uuid);

//    update seat
    SeatResponse updateSeatStatus(UUID uuid, UpdateSeatStatusRequest updateSeatStatusRequest);
}
