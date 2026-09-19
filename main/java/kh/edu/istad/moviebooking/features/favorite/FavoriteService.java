package kh.edu.istad.moviebooking.features.favorite;

import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteResponse;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteStatusResponse;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {
    FavoriteStatusResponse toggleFavorite(UUID userUuid, UUID movieUuid);

    List<FavoriteResponse> getFavorites(UUID userUuid);

    FavoriteStatusResponse getFavoriteStatus(UUID userUuid, UUID movieUuid);
}
