package kh.edu.istad.moviebooking.config;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}
