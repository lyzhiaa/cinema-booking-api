package kh.edu.istad.moviebooking.features.bakong;


import kh.edu.istad.moviebooking.features.bakong.dto.BakongCheckTransactionRequest;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongCheckTransactionResponse;
import kh.edu.istad.moviebooking.intergration.bakong.BakongProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BakongClient {

    private final BakongProperties properties;

    public BakongCheckTransactionResponse checkTransactionByMd5(String md5) {

        RestClient restClient = RestClient.builder()
                        .baseUrl(properties.getBaseUrl())
                        .defaultHeader(HttpHeaders.AUTHORIZATION,
                                "Bearer " + properties.getToken())
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();

        return restClient
                .post()
                .uri("/v1/check_transaction_by_md5")
                .body(new BakongCheckTransactionRequest(md5))
                .retrieve()
                .body(BakongCheckTransactionResponse.class);
    }
}
