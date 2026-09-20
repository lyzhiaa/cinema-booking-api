package kh.edu.istad.moviebooking.features.favorite;

import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteResponse;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteStatusResponse;

import java.util.UUID;

public interface FavoriteService {
    FavoriteStatusResponse toggleFavorite(UUID movieUuid);

    PageResponse<FavoriteResponse> getFavorites(int page, int size);

    FavoriteStatusResponse getFavoriteStatus(UUID movieUuid);

    long getMyFavoriteCount();
}
