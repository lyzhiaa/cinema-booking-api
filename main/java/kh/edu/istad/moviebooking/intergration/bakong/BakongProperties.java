package kh.edu.istad.moviebooking.intergration.bakong;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bakong")
public class BakongProperties {
    private String baseUrl;

    private String token;

    private String accountId;

    private String accountName;

    private String currency;
}
