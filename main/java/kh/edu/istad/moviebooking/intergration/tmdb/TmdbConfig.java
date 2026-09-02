package kh.edu.istad.moviebooking.intergration.tmdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class TmdbConfig {

    @Bean
    public RestClient tmdbRestClient(
            @Value("${tmdb.base-url}") String baseUrl,
            @Value("${tmdb.token}") String token
    ) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}
