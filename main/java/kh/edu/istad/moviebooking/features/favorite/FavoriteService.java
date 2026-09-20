package kh.edu.istad.moviebooking.features.favorite;

import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteResponse;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteStatusResponse;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {
    FavoriteStatusResponse toggleFavorite(UUID movieUuid);

    List<FavoriteResponse> getFavorites();

    FavoriteStatusResponse getFavoriteStatus(UUID movieUuid);
}
