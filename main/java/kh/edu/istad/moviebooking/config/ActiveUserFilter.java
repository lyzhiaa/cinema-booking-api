package kh.edu.istad.moviebooking.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActiveUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            UUID userUuid = UUID.fromString(jwt.getSubject());

            User user = userRepository.findUserByUuid(userUuid).orElse(null);

            if (user == null) {

                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User does not exist");

                return;
            }

            if (Boolean.TRUE.equals(user.getDisabled())) {

                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "User account is disabled");

                return;
            }

            String currentRole = user.getRole().getName();

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                            "ROLE_" + currentRole
                    );

            JwtAuthenticationToken updatedAuthentication = new JwtAuthenticationToken(
                            jwt, List.of(authority), user.getUsername());

            SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}