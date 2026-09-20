package kh.edu.istad.moviebooking.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private final ActiveUserFilter activeUserFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private SecretKey secretKey() {

        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {

        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey()));
    }

    @Bean("accessJwtDecoder")
    public JwtDecoder accessJwtDecoder() {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                        .withSecretKey(secretKey())
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(jwtIssuer);

        OAuth2TokenValidator<Jwt> typeValidator = new JwtTokenTypeValidator("access"
                );
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        typeValidator
                )
        );

        return decoder;
    }

    @Bean("refreshJwtDecoder")
    public JwtDecoder refreshJwtDecoder() {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                        .withSecretKey(secretKey())
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators
                        .createDefaultWithIssuer(jwtIssuer);

        OAuth2TokenValidator<Jwt> typeValidator = new JwtTokenTypeValidator("refresh");

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, typeValidator));

        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Qualifier("accessJwtDecoder") JwtDecoder accessJwtDecoder) throws Exception {

        http
                .cors(cors -> {})

                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC AUTH
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        )
                        .permitAll()


                        // =========================
                        // WEBSOCKET
                        // =========================

                        .requestMatchers(
                                "/ws/**"
                        )
                        .permitAll()


                        // =========================
                        // GUEST
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/movies/**",
                                "/api/v1/showtimes/**",
                                "/api/v1/halls/**",
                                "/api/v1/seats/**"
                        )
                        .permitAll()


                        // =========================
                        // CURRENT USER
                        // =========================

                        .requestMatchers(
                                "/api/v1/users/me/**"
                        )
                        .authenticated()


                        // =========================
                        // ADMIN - USER MANAGEMENT
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/users/*/disable",
                                "/api/v1/users/*/enable",
                                "/api/v1/users/*/role"
                        )
                        .hasRole("ADMIN")


                        // =========================
                        // STAFF + ADMIN
                        // CREATE CINEMA DATA
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/movies",
                                "/api/v1/halls",
                                "/api/v1/seats",
                                "/api/v1/showtimes"
                        )
                        .hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )


                        // =========================
                        // STAFF + ADMIN
                        // UPDATE CINEMA DATA
                        // =========================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/movies/**",
                                "/api/v1/halls/**",
                                "/api/v1/seats/**",
                                "/api/v1/showtimes/**"
                        )
                        .hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/movies/**",
                                "/api/v1/halls/**",
                                "/api/v1/seats/**",
                                "/api/v1/showtimes/**"
                        )
                        .hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )


                        // =========================
                        // STAFF + ADMIN
                        // DELETE CINEMA DATA
                        // =========================

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/movies/**",
                                "/api/v1/halls/**",
                                "/api/v1/seats/**",
                                "/api/v1/showtimes/**"
                        )
                        .hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )


                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .anyRequest()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt
                                        .decoder(accessJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .addFilterAfter(activeUserFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());

        return converter;
    }

}