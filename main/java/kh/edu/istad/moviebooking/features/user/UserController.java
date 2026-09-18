package kh.edu.istad.moviebooking.features.user;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.user.dto.CreateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UpdateUserRequest;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // CREATE USER

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {

        return userService.createUser(createUserRequest);
    }

    // GET ALL USERS

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    // GET USER BY UUID

    @GetMapping("/{uuid}")
    public UserResponse getUserByUuid(@PathVariable UUID uuid) {

        return userService.getUserByUuid(uuid);
    }

    // UPDATE USER

    @PatchMapping("/{uuid}")
    public UserResponse updateUser(@PathVariable UUID uuid, @Valid @RequestBody UpdateUserRequest updateUserRequest) {

        return userService.updateUser(uuid, updateUserRequest);
    }


    // DISABLE
    @PatchMapping("/{uuid}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableUser(@PathVariable UUID uuid) {
        userService.disableUser(uuid);
    }


    // ENABLE
    @PatchMapping("/{uuid}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableUser(@PathVariable UUID uuid) {

        userService.enableUser(uuid);
    }


    // SOFT DELETE
    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID uuid) {

        userService.deleteUser(uuid);
    }
}