package com.example.trade_vision_backend.ingestion.market.infrastructure.config;

import com.example.trade_vision_backend.ingestion.market.domain.client.MarketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class RestClientConfig {

    private static final String BASE_URL = "https://api.coincap.io/v2";

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeaders(this::configureHeaders)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String errorMessage = String.format(
                            "Server error occurred while calling %s. Status code: %s, Response body: %s",
                            request.getURI(),
                            response.getStatusCode(),
                            response.getBody()
                    );
                    log.error(String.format(
                            "Server error occurred while calling %s. Status code: %s, Response body: %s",
                            request.getURI(),
                            response.getStatusCode(),
                            response.getBody()));
                    throw new RestClientException(errorMessage);
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String errorMessage = String.format(
                            "Client error occurred while calling %s. Status code: %s, Response body: %s",
                            request.getURI(),
                            response.getStatusCode(),
                            response.getBody()
                    );
                    log.error(errorMessage);
                    throw new RestClientException(errorMessage);
                })
                .build();
    }

    public void configureHeaders(HttpHeaders headers) {
        headers.add(HttpHeaders.ACCEPT, "application/json");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.ACCEPT_ENCODING, "deflate");
    }

    @Bean
    public MarketClient marketClient() {
        RestClient client = restClient();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
        return factory.createClient(MarketClient.class);
    }
}
