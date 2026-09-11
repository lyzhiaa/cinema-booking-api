package kh.edu.istad.moviebooking.features.showtime;

import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.domain.Showtime;
import kh.edu.istad.moviebooking.domain.enums.HallStatus;
import kh.edu.istad.moviebooking.domain.enums.ShowtimeStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.hall.HallRepository;
import kh.edu.istad.moviebooking.features.movie.MovieRepository;
import kh.edu.istad.moviebooking.features.showtime.dto.CreateShowtimeRequest;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeResponse;
import kh.edu.istad.moviebooking.mapper.ShowtimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {
    private final ShowTimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final ShowtimeMapper showtimeMapper;

//    create showtime
    @Override
    public ShowtimeResponse createShowtime(CreateShowtimeRequest createShowtimeRequest) {
//        find movie
        Movie movie = movieRepository.findByUuid(createShowtimeRequest.movieUuid()).orElseThrow(
                () -> new ResourceNotFoundException("Movie", "uuid", createShowtimeRequest.movieUuid())
        );
        // 2. Find Hall
        Hall hall = hallRepository.findHallByUuid(createShowtimeRequest.hallUuid()).orElseThrow(
                () -> new ResourceNotFoundException("Hall", "uuid", createShowtimeRequest.hallUuid())
        );
//        Make sure hall can be used
        if(hall.getStatus() != HallStatus.ACTIVE) {
            throw new BadRequestException("Hall is not available for showtime");
        }
//        movie must have run time
        if(movie.getRuntimeMinutes() == null) {
            throw new BadRequestException("Movie runtime is not available");
        }
//        for
        LocalDateTime startDateTime = LocalDateTime.of(
                createShowtimeRequest.showDate(), createShowtimeRequest.showTime()
        );
//        Calculate showtime end time
        LocalDateTime endDateTime = startDateTime.plusMinutes(movie.getRuntimeMinutes());
//        Check if the Hall already has another showtime
        boolean hasConflict = showtimeRepository
                .existsByHallAndStartTimeLessThanAndEndTimeGreaterThan
                        (hall, endDateTime, startDateTime);
        if (hasConflict) {
            throw new BadRequestException(
                    "Hall already has another showtime during this time"
            );
        }
//        create show time
        Showtime showtime = new Showtime();

        showtime.setMovie(movie);
        showtime.setHall(hall);
        showtime.setStartTime(startDateTime);
        showtime.setEndTime(endDateTime);
        showtime.setBasePrice(createShowtimeRequest.basePrice());
        showtime.setStatus(ShowtimeStatus.DRAFT);
        // New entity -> save()
        Showtime savedShowtime = showtimeRepository.save(showtime);

        // Entity -> DTO
        return showtimeMapper.toShowtimeResponse(savedShowtime);
    }


    @Override
    public List<ShowtimeResponse> getAllShowTimes() {

        List<Showtime> showTimes = showtimeRepository.findAll();

        return showtimeMapper.toShowtimeResponseList(showTimes);
    }


    @Override
    public ShowtimeResponse getShowTimeByUuid(
            UUID uuid
    ) {

        Showtime showtime = showtimeRepository.findShowtimeByUuid(uuid).orElseThrow(
                () -> new ResourceNotFoundException("Showtime", "uuid", uuid));

        return showtimeMapper
                .toShowtimeResponse(showtime);
    }
}
