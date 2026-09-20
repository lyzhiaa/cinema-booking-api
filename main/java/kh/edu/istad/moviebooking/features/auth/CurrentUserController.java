package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;
import kh.edu.istad.moviebooking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class CurrentUserController {

    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse getCurrentUser() {

        User user = currentUserService.getCurrentUser();

        return userMapper.toUserResponse(user);
    }
}