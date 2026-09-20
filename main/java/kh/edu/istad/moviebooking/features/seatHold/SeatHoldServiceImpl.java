package kh.edu.istad.moviebooking.features.seatHold;

import kh.edu.istad.moviebooking.domain.Seat;
import kh.edu.istad.moviebooking.domain.Showtime;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.domain.enums.SeatAvailabilityStatus;
import kh.edu.istad.moviebooking.domain.enums.SeatStatus;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.seat.SeatRealtimeService;
import kh.edu.istad.moviebooking.features.seat.SeatRepository;
import kh.edu.istad.moviebooking.features.seatHold.dto.CreateSeatHoldRequest;
import kh.edu.istad.moviebooking.features.seatHold.dto.SeatHoldResponse;
import kh.edu.istad.moviebooking.features.seatReservation.SeatReservationRepository;
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

    private final SeatReservationRepository seatReservationRepository;

    private final SeatRealtimeService seatRealtimeService;

    private final CurrentUserService currentUserService;


    @Override
    public SeatHoldResponse holdSeats(UUID showtimeUuid, CreateSeatHoldRequest createSeatHoldRequest) {

        User user = currentUserService.getCurrentUser();

        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new BadRequestException("User account is disabled");
        }

        // Find Showtime
        Showtime showtime = showtimeRepository.findShowtimeByUuid(showtimeUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Showtime",
                                "uuid",
                                showtimeUuid
                        )
                );
//  NEW: Resolve normal seats + couple seats

        Map<UUID, Seat> seatsToHold = new LinkedHashMap<>();

        for (UUID seatUuid : createSeatHoldRequest.seatUuids()) {

            Seat seat = seatRepository.findSeatByUuid(seatUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "uuid", seatUuid));

            // Check whether this seat belongs to a couple group
            if (seat.getSeatGroup() != null) {

                List<Seat> groupSeats = seatRepository.findAllSeatBySeatGroupUuid(seat.getSeatGroup().getUuid());

                // Couple group must contain exactly 2 seats
                if (groupSeats.size() != 2) {
                    throw new BadRequestException("Invalid couple seat configuration");
                }

                // Add BOTH seats
                for (Seat groupSeat : groupSeats) {
                    seatsToHold.put(groupSeat.getUuid(), groupSeat);
                }

            } else {
                // Normal seat
                seatsToHold.put(seat.getUuid(), seat);
            }
        }

        // Validate ALL actual seats

        for (Seat seat : seatsToHold.values()) {

            // Seat must belong to Showtime Hall
            if (!seat.getHall()
                    .getUuid()
                    .equals(showtime.getHall().getUuid())) {

                throw new BadRequestException("Seat " + seat.getSeatLabel() + " does not belong to the showtime hall");
            }


            // Physical seat must be usable
            if (seat.getStatus() != SeatStatus.ACTIVE) {

                throw new BadRequestException("Seat " + seat.getSeatLabel() + " is not available");
            }
            //        seat must not already be booked
            boolean isBooked = seatReservationRepository.existsByShowtimeUuidAndSeatUuid(showtimeUuid, seat.getUuid());
            if (isBooked) {
                throw new BadRequestException("Seat " + seat.getSeatLabel() + " is already booked");
            }
        }

        // Generate ONE holdId for all selected seats

        UUID holdId = UUID.randomUUID();

        List<UUID> heldSeatUuids = new ArrayList<>();

        // Used for rollback if one seat fails
        List<String> acquiredKeys = new ArrayList<>();

        // Hold actual seats in Redis

        for (Seat seat : seatsToHold.values()) {

            String key = buildSeatHoldKey(showtimeUuid, seat.getUuid());

            Boolean success = redisTemplate
                    .opsForValue()
                    .setIfAbsent(key, holdId.toString(), HOLD_DURATION);


            if (!Boolean.TRUE.equals(success)) {

                // Remove seats that THIS request
                // already held before failure
                if (!acquiredKeys.isEmpty()) {
                    redisTemplate.delete(acquiredKeys);
                }

                throw new BadRequestException(
                        "Seat " + seat.getSeatLabel() + " is currently held"
                );
            }


            acquiredKeys.add(key);

            heldSeatUuids.add(seat.getUuid());
        }


        // =====================================================
        // 6. Response
        // =====================================================

        String holdUserKey = buildHoldUserKey(showtimeUuid, holdId);

        redisTemplate.opsForValue().set(holdUserKey, user.getUuid().toString(), HOLD_DURATION);

        String holdSeatsKey = buildHoldSeatsKey(showtimeUuid, holdId);

        String[] seatUuidStrings = heldSeatUuids.stream()
                        .map(UUID::toString)
                        .toArray(String[]::new);

        redisTemplate.opsForSet().add(holdSeatsKey, seatUuidStrings);

        redisTemplate.expire(holdSeatsKey, HOLD_DURATION);

        seatRealtimeService.broadcastSeatUpdate(
                showtimeUuid,
                heldSeatUuids,
                SeatAvailabilityStatus.HELD,
                HOLD_DURATION.toSeconds()
        );

        return new SeatHoldResponse(holdId, showtimeUuid, heldSeatUuids, HOLD_DURATION.toSeconds());

    }

    @Override
    public void releaseHold(UUID showtimeUuid, UUID holdId) {

        String holdSeatsKey = buildHoldSeatsKey(showtimeUuid, holdId);

        Set<String> seatUuidStrings = redisTemplate.opsForSet().members(holdSeatsKey);

        if (seatUuidStrings == null || seatUuidStrings.isEmpty()) {

            throw new BadRequestException("Seat hold does not exist or has expired");
        }

        List<UUID> releasedSeatUuids = new ArrayList<>();

        // 1. Remove every seat that really belongs
        //    to this hold
        for (String seatUuidString : seatUuidStrings) {

            UUID seatUuid = UUID.fromString(seatUuidString);

            String seatHoldKey = buildSeatHoldKey(showtimeUuid, seatUuid);

            String currentHoldId = redisTemplate.opsForValue().get(seatHoldKey);

            // Important:
            // delete only when this hold still owns the seat
            if (holdId.toString().equals(currentHoldId)) {

                redisTemplate.delete(seatHoldKey);

                releasedSeatUuids.add(seatUuid);
            }
        }

        // 2. Delete reverse mapping
        redisTemplate.delete(holdSeatsKey);

        String holdUserKey = buildHoldUserKey(showtimeUuid, holdId);

        redisTemplate.delete(holdUserKey);

        // 3. Nothing was actually released
        if (releasedSeatUuids.isEmpty()) {
            return;
        }


        List<UUID> availableSeatUuids = new ArrayList<>();

        List<UUID> bookedSeatUuids = new ArrayList<>();


        for (UUID seatUuid : releasedSeatUuids) {

            boolean isBooked = seatReservationRepository.existsByShowtimeUuidAndSeatUuid(showtimeUuid, seatUuid);

            if (isBooked) {

                bookedSeatUuids.add(seatUuid);

            } else {

                availableSeatUuids.add(seatUuid);
            }
        }


        // 5. Broadcast AVAILABLE seats
        if (!availableSeatUuids.isEmpty()) {

            seatRealtimeService.broadcastSeatUpdate(
                            showtimeUuid,
                            availableSeatUuids,
                            SeatAvailabilityStatus.AVAILABLE,
                            null
                    );
        }


        // 6. Broadcast BOOKED seats
        if (!bookedSeatUuids.isEmpty()) {

            seatRealtimeService.broadcastSeatUpdate(
                            showtimeUuid,
                            bookedSeatUuids,
                            SeatAvailabilityStatus.BOOKED,
                            null
                    );
        }
    }

    @Override
    public void validateHoldOwner(UUID showtimeUuid, UUID holdId, UUID userUuid) {

        String holdUserKey = buildHoldUserKey(showtimeUuid, holdId);

        String holdUserUuid = redisTemplate.opsForValue().get(holdUserKey);

        if (holdUserUuid == null) {
            throw new BadRequestException("Seat hold does not exist or has expired");
        }

        if (!userUuid.toString().equals(holdUserUuid)) {
            throw new BadRequestException("Seat hold does not belong to this user");
        }
    }

    //    helper method

    private String buildHoldUserKey(UUID showtimeUuid, UUID holdId) {

        return "hold:user:" + showtimeUuid + ":" + holdId;
    }

    private String buildSeatHoldKey(UUID showtimeUuid, UUID seatUuid) {
        return "seat:hold:" + showtimeUuid + ":" + seatUuid;
    }


    private String buildHoldSeatsKey(UUID showtimeUuid, UUID holdId) {
        return "hold:seats:" + showtimeUuid + ":" + holdId;
    }

}
