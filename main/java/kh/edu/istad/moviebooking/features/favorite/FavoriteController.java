package kh.edu.istad.moviebooking.features.favorite;

import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteResponse;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userUuid}/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PatchMapping("/{movieUuid}")
    public FavoriteStatusResponse toggleFavorite(@PathVariable UUID userUuid, @PathVariable UUID movieUuid) {

        return favoriteService.toggleFavorite(userUuid, movieUuid);
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(@PathVariable UUID userUuid) {

        return favoriteService.getFavorites(userUuid);
    }

    @GetMapping("/{movieUuid}/status")
    public FavoriteStatusResponse getFavoriteStatus(@PathVariable UUID userUuid, @PathVariable UUID movieUuid) {

        return favoriteService.getFavoriteStatus(userUuid, movieUuid);
    }
}