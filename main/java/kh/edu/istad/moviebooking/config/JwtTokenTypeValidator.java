package kh.edu.istad.moviebooking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
public class JwtTokenTypeValidator
        implements OAuth2TokenValidator<Jwt> {

    private final String expectedType;

    @Override
    public OAuth2TokenValidatorResult validate(
            Jwt jwt
    ) {

        String tokenType =
                jwt.getClaimAsString("type");

        if (expectedType.equals(tokenType)) {

            return OAuth2TokenValidatorResult
                    .success();
        }

        OAuth2Error error =
                new OAuth2Error(
                        "invalid_token",
                        "Invalid token type",
                        null
                );

        return OAuth2TokenValidatorResult
                .failure(error);
    }
}