package kh.edu.istad.moviebooking.features.auth.dto;

public record LoginResponse(
        String accessToken,

        String refreshToken,

        String tokenType
) {
}