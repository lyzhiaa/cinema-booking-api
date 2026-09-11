package kh.edu.istad.moviebooking.features.showtime;

import kh.edu.istad.moviebooking.features.showtime.dto.CreateShowtimeRequest;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeResponse;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeSeatResponse;

import java.util.List;
import java.util.UUID;

public interface ShowtimeService {
    ShowtimeResponse createShowtime(CreateShowtimeRequest createShowtimeRequest);
    List<ShowtimeResponse> getAllShowTimes();
    ShowtimeResponse getShowTimeByUuid(UUID showTimeUuid);
//    get showtime seats
    List<ShowtimeSeatResponse> getShowtimeSeats(UUID showtimeUuid);
}
