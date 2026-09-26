package kh.edu.istad.moviebooking.features.groupBooking.dto;

import kh.edu.istad.moviebooking.domain.enums.GroupBookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupBookingResponse(

        UUID uuid,

        String name,

        String inviteToken,

        GroupBookingStatus status,

        UUID hostUuid,

        UUID showtimeUuid,

        long memberCount,

        long readyCount,

        LocalDateTime expiresAt,

        LocalDateTime lockedAt,

        LocalDateTime paymentExpiresAt

) {
}