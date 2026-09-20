package kh.edu.istad.moviebooking.features.auth;


import kh.edu.istad.moviebooking.config.RefreshTokenResponse;
import kh.edu.istad.moviebooking.features.auth.dto.*;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

//    for logout
    void logout(LogoutRequest request);
}