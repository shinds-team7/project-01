package com.example.petnow.service;

import com.example.petnow.common.config.KakaoLocalApiProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class KakaoLocalApiClient implements GeocodingClient {

    private static final String ADDRESS_SEARCH_PATH = "/v2/local/search/address.json";

    private final KakaoLocalApiProperties properties;
    private final RestClient restClient;

    public KakaoLocalApiClient(KakaoLocalApiProperties properties,
                               @Qualifier("kakaoLocalApiRestClient") RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public Optional<GeocodedCoordinates> geocode(String roadAddress) {
        if (!StringUtils.hasText(roadAddress)) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(properties.getRestApiKey())) {
            log.warn("카카오 로컬 API 키가 없어 지오코딩을 건너뜁니다.");
            return Optional.empty();
        }

        try {
            KakaoAddressSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ADDRESS_SEARCH_PATH)
                            .queryParam("query", roadAddress.trim())
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .body(KakaoAddressSearchResponse.class);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                return Optional.empty();
            }

            KakaoAddressDocument document = response.documents().get(0);
            return toCoordinates(document);
        } catch (RestClientException | NumberFormatException exception) {
            log.warn("카카오 주소 지오코딩 요청에 실패했습니다.", exception);
            return Optional.empty();
        }
    }

    private Optional<GeocodedCoordinates> toCoordinates(KakaoAddressDocument document) {
        if (document == null || !StringUtils.hasText(document.x()) || !StringUtils.hasText(document.y())) {
            return Optional.empty();
        }
        return Optional.of(new GeocodedCoordinates(
                new BigDecimal(document.y()),
                new BigDecimal(document.x())));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoAddressSearchResponse(List<KakaoAddressDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoAddressDocument(String x, String y) {
    }
}
