package kh.edu.istad.moviebooking.features.groupBooking.dto;


import kh.edu.istad.moviebooking.domain.enums.GroupMemberStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupMemberResponse(

        UUID uuid,

        UUID userUuid,

        String firstName,

        String lastName,

        UUID bookingUuid,

        GroupMemberStatus status,

        LocalDateTime joinedAt

) {
}
