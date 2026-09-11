package kh.edu.istad.moviebooking.features.showtime;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.showtime.dto.CreateShowtimeRequest;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeResponse;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeSeatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    // Create showtime
    @PostMapping
    public ShowtimeResponse createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request
    ) {
        return showtimeService.createShowtime(request);
    }

    // Get all show times
    @GetMapping
    public List<ShowtimeResponse> getAllShowTimes() {
        return showtimeService.getAllShowTimes();
    }

    // Get one showtime by UUID
    @GetMapping("/{uuid}")
    public ShowtimeResponse getShowtimeByUuid(
            @PathVariable UUID uuid
    ) {
        return showtimeService.getShowTimeByUuid(uuid);
    }

//    get show time seats
    @GetMapping("/{uuid}/seats")
    public List<ShowtimeSeatResponse> getShowtimeSeats(@PathVariable UUID uuid) {
        return showtimeService.getShowtimeSeats(uuid);
    }
}
