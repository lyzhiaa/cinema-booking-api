package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.config.RefreshTokenResponse;
import kh.edu.istad.moviebooking.domain.RefreshToken;
import kh.edu.istad.moviebooking.domain.Role;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.auth.dto.*;
import kh.edu.istad.moviebooking.features.role.RoleRepository;
import kh.edu.istad.moviebooking.features.user.UserRepository;
import kh.edu.istad.moviebooking.features.user.dto.UserResponse;
import kh.edu.istad.moviebooking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.username())) {

            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {

            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByPhone(registerRequest.phone())) {

            throw new BadRequestException("Phone already exists"
            );
        }

        Role customerRole = roleRepository.findRoleByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "CUSTOMER"));

        User user = userMapper.fromRegisterRequest(registerRequest);

        user.setPassword(passwordEncoder.encode(registerRequest.password()));

        user.setRole(customerRole);

        user.setPoints(0);

        user.setDisabled(false);

        User savedUser =
                userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsernameOrEmail(request.identifier(), request.identifier())
                        .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));

        if (Boolean.TRUE.equals(user.getDisabled())) {

            throw new BadRequestException("User account is disabled");
        }

        if (user.getPassword() == null) {

            throw new BadRequestException("This account does not have a password");
        }

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());

        if (!passwordMatches) {

            throw new BadRequestException("Invalid username/email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);

        String jti = UUID.randomUUID().toString();

        refreshTokenService.create(user, jti);

        String refreshToken = jwtService.generateRefreshToken(user, jti);

        return new LoginResponse(accessToken, refreshToken, "Bearer");
    }

    @Override
    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {

        Jwt jwt = jwtService.decodeRefreshToken(request.refreshToken());

        String jti = jwt.getId();

        if (jti == null) {

            throw new BadRequestException("Refresh token does not contain jti");
        }

        RefreshToken storedToken = refreshTokenService.findValidToken(jti);

        User user = storedToken.getUser();

        String subject = jwt.getSubject();

        if (!user.getUuid().toString().equals(subject)) {

            throw new BadRequestException("Refresh token does not belong to this user");
        }

        if (Boolean.TRUE.equals(user.getDisabled())) {

            throw new BadRequestException("User account is disabled");
        }

        refreshTokenService.revoke(storedToken);

        String newAccessToken = jwtService.generateAccessToken(user);

        String newJti = UUID.randomUUID().toString();

        refreshTokenService.create(user, newJti);

        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken, "Bearer");
    }
}