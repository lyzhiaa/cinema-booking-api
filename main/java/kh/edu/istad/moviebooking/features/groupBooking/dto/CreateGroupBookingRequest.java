package kh.edu.istad.moviebooking.features.groupBooking.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGroupBookingRequest(

        @NotNull
        UUID showtimeUuid,

        @NotBlank
        String name

) {
}
