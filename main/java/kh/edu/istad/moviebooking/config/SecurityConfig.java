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


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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

        http.csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.POST,
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh"
                                )
                                .permitAll()
                                .requestMatchers("/ws/**")
                                .permitAll()
                                .requestMatchers(
                                        "/api/v1/users/me/**"
                                )
                                .authenticated()
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/users"
                                )
                                .hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/v1/users/*/disable",
                                        "/api/v1/users/*/enable"
                                )
                                .hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/v1/users/*/role"
                        )
                        .hasRole("ADMIN")
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
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());

        return converter;
    }

}