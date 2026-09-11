package kh.edu.istad.moviebooking.features.seatHold;

import kh.edu.istad.moviebooking.domain.Seat;
import kh.edu.istad.moviebooking.domain.Showtime;
import kh.edu.istad.moviebooking.domain.enums.SeatStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.seat.SeatRepository;
import kh.edu.istad.moviebooking.features.seatHold.dto.CreateSeatHoldRequest;
import kh.edu.istad.moviebooking.features.seatHold.dto.SeatHoldResponse;
import kh.edu.istad.moviebooking.features.showtime.ShowTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {
    private static final Duration HOLD_DURATION = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    private final ShowTimeRepository showtimeRepository;
    private final SeatRepository seatRepository;


    @Override
    public SeatHoldResponse holdSeats(UUID showtimeUuid, CreateSeatHoldRequest createSeatHoldRequest) {

        // Find Showtime
        Showtime showtime = showtimeRepository.findShowtimeByUuid(showtimeUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Showtime",
                                "uuid",
                                showtimeUuid
                        )
                );
// =====================================================
// 2. NEW: Resolve normal seats + couple seats
// =====================================================

        Map<UUID, Seat> seatsToHold = new LinkedHashMap<>();

        for (UUID seatUuid : createSeatHoldRequest.seatUuids()) {

            Seat seat = seatRepository.findSeatByUuid(seatUuid).orElseThrow(
                    () -> new ResourceNotFoundException(
                                    "Seat",
                                    "uuid",
                                    seatUuid
                            )
                    );

            // Check whether this seat belongs to a couple group
            if (seat.getSeatGroup() != null) {

                List<Seat> groupSeats =
                        seatRepository.findAllSeatBySeatGroupUuid(
                                seat.getSeatGroup().getUuid()
                        );

                // Couple group must contain exactly 2 seats
                if (groupSeats.size() != 2) {
                    throw new BadRequestException(
                            "Invalid couple seat configuration"
                    );
                }

                // Add BOTH seats
                for (Seat groupSeat : groupSeats) {

                    seatsToHold.put(
                            groupSeat.getUuid(),
                            groupSeat
                    );
                }

            } else {

                // Normal seat
                seatsToHold.put(
                        seat.getUuid(),
                        seat
                );
            }
        }


        // =====================================================
        // 3. Validate ALL actual seats
        // =====================================================

        for (Seat seat : seatsToHold.values()) {

            // Seat must belong to Showtime Hall
            if (!seat.getHall()
                    .getUuid()
                    .equals(showtime.getHall().getUuid())) {

                throw new BadRequestException(
                        "Seat "
                                + seat.getSeatLabel()
                                + " does not belong to the showtime hall"
                );
            }


            // Physical seat must be usable
            if (seat.getStatus() != SeatStatus.ACTIVE) {

                throw new BadRequestException(
                        "Seat "
                                + seat.getSeatLabel()
                                + " is not available"
                );
            }
        }


        // =====================================================
        // 4. Generate ONE holdId for all selected seats
        // =====================================================

        UUID holdId = UUID.randomUUID();

        List<UUID> heldSeatUuids = new ArrayList<>();

        // Used for rollback if one seat fails
        List<String> acquiredKeys = new ArrayList<>();


        // =====================================================
        // 5. Hold actual seats in Redis
        // =====================================================

        for (Seat seat : seatsToHold.values()) {

            String key = buildSeatHoldKey(
                    showtimeUuid,
                    seat.getUuid()
            );

            Boolean success = redisTemplate
                    .opsForValue()
                    .setIfAbsent(
                            key,
                            holdId.toString(),
                            HOLD_DURATION
                    );


            if (!Boolean.TRUE.equals(success)) {

                // Remove seats that THIS request
                // already held before failure
                if (!acquiredKeys.isEmpty()) {
                    redisTemplate.delete(acquiredKeys);
                }

                throw new BadRequestException(
                        "Seat "
                                + seat.getSeatLabel()
                                + " is currently held"
                );
            }


            acquiredKeys.add(key);

            heldSeatUuids.add(
                    seat.getUuid()
            );
        }


        // =====================================================
        // 6. Response
        // =====================================================

        String holdSeatsKey = buildHoldSeatsKey(showtimeUuid, holdId);

        String[] seatUuidStrings = heldSeatUuids.stream()
                        .map(UUID::toString)
                        .toArray(String[]::new);

        redisTemplate.opsForSet().add(holdSeatsKey, seatUuidStrings);

        redisTemplate.expire(holdSeatsKey, HOLD_DURATION);

        return new SeatHoldResponse(
                holdId,
                showtimeUuid,
                heldSeatUuids,
                HOLD_DURATION.toSeconds()
        );

    }

    @Override
    public void releaseHold(UUID showtimeUuid, UUID holdId) {
        //    create hold key
        String holdSeatsKey = buildHoldSeatsKey(showtimeUuid, holdId);
        // get all seats belonging to that hold
        Set<String> seatUuidStrings = redisTemplate.opsForSet().members(holdSeatsKey);
        // Handle an invalid or expired hold
        if (seatUuidStrings == null || seatUuidStrings.isEmpty()) {
            throw new BadRequestException("Seat hold does not exist or has expired");
        }
        // 4. Release each seat
        for (String seatUuidString : seatUuidStrings) {

            UUID seatUuid = UUID.fromString(seatUuidString);

            String seatHoldKey = buildSeatHoldKey(showtimeUuid, seatUuid);

            String currentHoldId = redisTemplate.opsForValue().get(seatHoldKey);


            // Only remove a seat owned by this hold
            if (holdId.toString().equals(currentHoldId)) {

                redisTemplate.delete(seatHoldKey);
            }
        }


        // 5. Remove hold → seats mapping
        redisTemplate.delete(holdSeatsKey);
    }

     //    helper method
    private String buildSeatHoldKey(UUID showtimeUuid, UUID seatUuid) {
        return "seat:hold:"
                + showtimeUuid
                + ":"
                + seatUuid;
    }


    private String buildHoldSeatsKey(
            UUID showtimeUuid,
            UUID holdId
    ) {
        return "hold:seats:"
                + showtimeUuid
                + ":"
                + holdId;
    }

}
