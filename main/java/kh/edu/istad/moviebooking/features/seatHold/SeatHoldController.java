package kh.edu.istad.moviebooking.features.seatHold;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.seatHold.dto.CreateSeatHoldRequest;
import kh.edu.istad.moviebooking.features.seatHold.dto.SeatHoldResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;


    @PostMapping("/{showtimeUuid}/holds")
    public SeatHoldResponse  holdSeats(@PathVariable UUID showtimeUuid, @Valid @RequestBody CreateSeatHoldRequest createSeatHoldRequest) {

        return seatHoldService.holdSeats(
                showtimeUuid,
                createSeatHoldRequest
        );
    }

    @DeleteMapping("/{showtimeUuid}/holds/{holdId}")
    public void releaseHold(@PathVariable UUID showtimeUuid, @PathVariable UUID holdId) {

        seatHoldService.releaseHold(showtimeUuid, holdId);
    }
}
