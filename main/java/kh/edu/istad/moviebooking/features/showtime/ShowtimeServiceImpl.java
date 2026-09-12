package kh.edu.istad.moviebooking.features.showtime;

import kh.edu.istad.moviebooking.domain.Hall;
import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.domain.Seat;
import kh.edu.istad.moviebooking.domain.Showtime;
import kh.edu.istad.moviebooking.domain.enums.*;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.booking.BookingSeatRepository;
import kh.edu.istad.moviebooking.features.hall.HallRepository;
import kh.edu.istad.moviebooking.features.movie.MovieRepository;
import kh.edu.istad.moviebooking.features.seat.SeatRepository;
import kh.edu.istad.moviebooking.features.seatReservation.SeatReservationRepository;
import kh.edu.istad.moviebooking.features.showtime.dto.CreateShowtimeRequest;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeResponse;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeSeatResponse;
import kh.edu.istad.moviebooking.mapper.ShowtimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final SeatRepository seatRepository;
//  for connect Redis holds to Showtime seat map
    private final StringRedisTemplate stringRedisTemplate;

    private final BookingSeatRepository bookingSeatRepository;
    private final SeatReservationRepository seatReservationRepository;

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
    public ShowtimeResponse getShowTimeByUuid(UUID uuid) {

        Showtime showtime = showtimeRepository.findShowtimeByUuid(uuid).orElseThrow(
                () -> new ResourceNotFoundException("Showtime", "uuid", uuid));

        return showtimeMapper
                .toShowtimeResponse(showtime);
    }

//    get showtime seat
    @Override
    public List<ShowtimeSeatResponse> getShowtimeSeats(UUID showtimeUuid) {
//        find show time
        Showtime showtime = showtimeRepository.findShowtimeByUuid(showtimeUuid).orElseThrow(
                () -> new ResourceNotFoundException("Showtime", "uuid", showtimeUuid)
        );
//        get the hall from the showtime
        Hall hall = showtime.getHall();
//        get all seats belonging to the hall
        List<Seat> seats = seatRepository.findAllSeatByHallUuidOrderByRowLabelAscSeatNumberAsc(hall.getUuid());
//        convert seat status into show time seat available
        return seats.stream().map(seat -> {
            SeatAvailabilityStatus availability;
//            if(seat.getStatus() == SeatStatus.ACTIVE) {
//                availabilityStatus = SeatAvailabilityStatus.AVAILABLE;
//            }else {
//                availabilityStatus = SeatAvailabilityStatus.UNAVAILABLE;
//            }
//            check seat is available or not
            if (seat.getStatus() != SeatStatus.ACTIVE) {

                availability =
                        SeatAvailabilityStatus.UNAVAILABLE;

            } else {

                // 1. Check PostgreSQL first
                boolean isBooked = seatReservationRepository.existsByShowtimeUuidAndSeatUuid(showtimeUuid, seat.getUuid());

                if (isBooked) {
                    availability = SeatAvailabilityStatus.BOOKED;

                } else {

                    // 2. If not booked, check temporary Redis hold
                    String holdKey = buildSeatHoldKey(showtimeUuid, seat.getUuid());

                    Boolean isHeld = stringRedisTemplate.hasKey(holdKey);

                    if (Boolean.TRUE.equals(isHeld)) {

                        availability = SeatAvailabilityStatus.HELD;

                    } else {

                        availability = SeatAvailabilityStatus.AVAILABLE;
                    }
                }
            }
            UUID groupUuid = null;

            if (seat.getSeatGroup() != null) {
                groupUuid = seat.getSeatGroup().getUuid();
            }

            return new ShowtimeSeatResponse(
                    seat.getUuid(),
                    groupUuid,
                    seat.getRowLabel(),
                    seat.getSeatNumber(),
                    seat.getSeatLabel(),
                    seat.getSeatType(),
                    availability
            );
        }).toList();
    }

//    helper for Redis key
private String buildSeatHoldKey(UUID showtimeUuid, UUID seatUuid) {
    return "seat:hold:"
            + showtimeUuid
            + ":"
            + seatUuid;
}
}
