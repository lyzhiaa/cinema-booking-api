package kh.edu.istad.moviebooking.features.favorite.dto;

import java.util.UUID;

public record FavoriteStatusResponse(
        UUID movieUuid,

        boolean favorite
) {
}
