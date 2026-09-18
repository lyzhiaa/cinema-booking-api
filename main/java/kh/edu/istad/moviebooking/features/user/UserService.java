package kh.edu.istad.moviebooking.features.user;

import kh.edu.istad.moviebooking.features.user.dto.CreateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UpdateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserByUuid(UUID uuid);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(UUID uuid, UpdateUserRequest request);

    void disableUser(UUID uuid);

    void enableUser(UUID uuid);

    void deleteUser(UUID uuid);
}
