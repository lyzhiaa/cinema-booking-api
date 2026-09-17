package kh.edu.istad.moviebooking.features.seat;

import kh.edu.istad.moviebooking.domain.enums.SeatAvailabilityStatus;
import kh.edu.istad.moviebooking.features.seat.dto.SeatAvailabilityEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;


    public void broadcastSeatUpdate(UUID showtimeUuid, List<UUID> seatUuids, SeatAvailabilityStatus availability, Long expiresInSeconds) {

        SeatAvailabilityEvent event = new SeatAvailabilityEvent(showtimeUuid, seatUuids, availability, expiresInSeconds);

        messagingTemplate.convertAndSend(
                "/topic/showtimes/"
                        + showtimeUuid
                        + "/seats",
                event
        );
    }
}
