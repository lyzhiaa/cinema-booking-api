package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl
        implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {

            throw new BadRequestException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Jwt jwt)) {

            throw new BadRequestException("Invalid authentication principal");
        }

        String subject = jwt.getSubject();

        if (subject == null) {

            throw new BadRequestException("JWT subject is missing");
        }

        UUID userUuid;

        try {

            userUuid = UUID.fromString(subject);

        } catch (IllegalArgumentException e) {

            throw new BadRequestException("Invalid user UUID in token");
        }

        User user = userRepository.findUserByUuid(userUuid).orElseThrow(() ->
                        new ResourceNotFoundException("User", "uuid", userUuid)
                );

        if (Boolean.TRUE.equals(user.getDisabled())) {

            throw new BadRequestException("User account is disabled");
        }

        return user;
    }
}