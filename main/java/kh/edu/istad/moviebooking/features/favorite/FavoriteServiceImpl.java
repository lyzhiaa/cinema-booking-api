package kh.edu.istad.moviebooking.features.favorite;

import kh.edu.istad.moviebooking.domain.Favorite;
import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.CurrentUserService;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteResponse;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteStatusResponse;
import kh.edu.istad.moviebooking.features.movie.MovieRepository;
import kh.edu.istad.moviebooking.features.user.UserRepository;
import kh.edu.istad.moviebooking.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final FavoriteMapper favoriteMapper;

    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public FavoriteStatusResponse toggleFavorite(UUID movieUuid) {

        User user = currentUserService.getCurrentUser();

        Movie movie = movieRepository.findByUuid(movieUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "uuid", movieUuid));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserUuidAndMovieUuid(user.getUuid(), movieUuid);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());

            return new FavoriteStatusResponse(movieUuid, false);
        }

        Favorite favorite = favoriteMapper.fromUserAndMovie(user, movie);

        favoriteRepository.save(favorite);

        return new FavoriteStatusResponse(movieUuid, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites() {

        User user = currentUserService.getCurrentUser();

        List<Favorite> favorites = favoriteRepository.findAllByUserUuidOrderByCreatedAtDesc(user.getUuid());

        return favoriteMapper.toFavoriteResponseList(favorites);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteStatusResponse getFavoriteStatus(UUID movieUuid) {

        User user = currentUserService.getCurrentUser();

        boolean favorite = favoriteRepository.existsByUserUuidAndMovieUuid(user.getUuid(), movieUuid);

        return new FavoriteStatusResponse(movieUuid, favorite
        );
    }
}