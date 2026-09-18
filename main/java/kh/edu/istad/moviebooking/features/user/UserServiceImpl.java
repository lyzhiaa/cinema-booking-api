package kh.edu.istad.moviebooking.features.user;

import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.user.dto.CreateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UpdateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;
import kh.edu.istad.moviebooking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {

        if (userRepository.existsByUsername(createUserRequest.username())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserRequest.email())) {
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByPhone(createUserRequest.phone())) {
            throw new BadRequestException("Phone already exists");
        }

        User user = userMapper.fromUserCreateRequest(createUserRequest);

        user.setPoints(0);
        user.setDisabled(false);
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUuid(UUID uuid) {

        User user = userRepository
                .findUserByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "User",
                                "uuid",
                                uuid
                        )
                );

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException(
                    "User",
                    "uuid",
                    uuid
            );
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        List<User> users = userRepository.findAllByIsDeletedFalse();

        return userMapper.toUserResponseList(users);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID uuid, UpdateUserRequest updateUserRequest) {

        User user = findNotDeletedUser(uuid);

        if (updateUserRequest.firstName() != null
                && updateUserRequest.firstName().isBlank()) {

            throw new BadRequestException("First name cannot be blank");
        }

        if (updateUserRequest.lastName() != null
                && updateUserRequest.lastName().isBlank()) {

            throw new BadRequestException("Last name cannot be blank");
        }

        if (updateUserRequest.username() != null
                && updateUserRequest.username().isBlank()) {

            throw new BadRequestException("Username cannot be blank");
        }

        if (updateUserRequest.email() != null
                && updateUserRequest.email().isBlank()) {

            throw new BadRequestException("Email cannot be blank");
        }

        if (updateUserRequest.phone() != null
                && updateUserRequest.phone().isBlank()) {

            throw new BadRequestException("Phone cannot be blank");
        }

        if (updateUserRequest.username() != null
                && !updateUserRequest.username().equals(user.getUsername())
                && userRepository.existsByUsername(updateUserRequest.username())) {

            throw new BadRequestException("Username already exists");
        }

        if (updateUserRequest.email() != null
                && !updateUserRequest.email().equals(user.getEmail())
                && userRepository.existsByEmail(updateUserRequest.email())) {

            throw new BadRequestException("Email already exists");
        }

        if (updateUserRequest.phone() != null
                && !updateUserRequest.phone().equals(user.getPhone())
                && userRepository.existsByPhone(updateUserRequest.phone())) {

            throw new BadRequestException("Phone already exists");
        }

        userMapper.fromUserUpdateRequest(updateUserRequest, user);

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void disableUser(UUID uuid) {

        User user = userRepository
                .findUserByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "uuid",
                                uuid
                        )
                );

        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new BadRequestException("User account is already disabled");
        }

        user.setDisabled(true);
    }


    @Override
    @Transactional
    public void enableUser(UUID uuid) {

        User user = userRepository
                .findUserByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "uuid",
                                uuid
                        )
                );

        if (Boolean.FALSE.equals(user.getDisabled())) {
            throw new BadRequestException("User account is already enabled");
        }

        user.setDisabled(false);
    }


    @Override
    @Transactional
    public void deleteUser(UUID uuid) {

        User user = userRepository
                .findUserByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", "uuid", uuid)
                );

        user.setIsDeleted(true);
        user.setDisabled(true);
    }

    private User findNotDeletedUser(UUID uuid) {

        User user = userRepository
                .findUserByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "uuid",
                                uuid
                        )
                );

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException(
                    "User",
                    "uuid",
                    uuid
            );
        }

        return user;
    }
}