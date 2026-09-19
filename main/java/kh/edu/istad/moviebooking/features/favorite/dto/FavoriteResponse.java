package kh.edu.istad.moviebooking.features.favorite.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteResponse(
        UUID uuid,

        UUID movieUuid,

        String title,

        String posterPath,

        LocalDate releaseDate,

        LocalDateTime createdAt

) {
}
