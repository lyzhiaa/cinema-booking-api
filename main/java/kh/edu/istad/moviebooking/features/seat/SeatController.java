package kh.edu.istad.moviebooking.features.seat;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.seat.dto.CreateSeatRequest;
import kh.edu.istad.moviebooking.features.seat.dto.SeatResponse;
import kh.edu.istad.moviebooking.features.seat.dto.UpdateSeatStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/halls/{hallUuid}/seats")
    public SeatResponse createSeat(
            @PathVariable UUID hallUuid,
            @Valid @RequestBody CreateSeatRequest createSeatRequest
    ) {
        return seatService.createSeat(hallUuid, createSeatRequest);
    }

    @GetMapping("/halls/{hallUuid}/seats")
    public List<SeatResponse> getSeatsByHall(@PathVariable UUID hallUuid) {
        return seatService.getSeatsByHallUuid(hallUuid);
    }

    @GetMapping("/seats/{uuid}")
    public SeatResponse getSeatByUuid(@PathVariable UUID uuid) {
        return seatService.getSeatByUuid(uuid);
    }

    @PatchMapping("/seats/{uuid}/status")
    public SeatResponse updateSeatStatus(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateSeatStatusRequest updateSeatStatusRequest
    ) {
        return seatService.updateSeatStatus(uuid, updateSeatStatusRequest);
    }
}
