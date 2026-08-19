package com.example.petnow.service;

import com.example.petnow.common.config.KakaoLocalApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoLocalApiClientTest {

    private KakaoLocalApiClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        KakaoLocalApiProperties properties = new KakaoLocalApiProperties();
        properties.setRestApiKey("test-rest-api-key");
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK test-rest-api-key");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KakaoLocalApiClient(properties, builder.build());
    }

    @Test
    void mapsKakaoLongitudeAndLatitudeInCorrectOrder() {
        server.expect(once(), requestTo(containsString("/v2/local/search/address.json")))
                .andExpect(queryParam("size", "1"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK test-rest-api-key"))
                .andRespond(withSuccess("""
                        {
                          "documents": [
                            {"x": "127.0557550", "y": "37.5446397", "address_name": "ignored"}
                          ],
                          "meta": {"total_count": 1}
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<GeocodedCoordinates> result = client.geocode(
                "서울특별시 성동구 왕십리로 83-21");

        assertThat(result).contains(new GeocodedCoordinates(
                new BigDecimal("37.5446397"),
                new BigDecimal("127.0557550")));
        server.verify();
    }

    @Test
    void returnsEmptyForZeroDocumentResponse() {
        server.expect(once(), requestTo(containsString("/v2/local/search/address.json")))
                .andRespond(withSuccess("{\"documents\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.geocode("missing")).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyForApiFailure() {
        server.expect(once(), requestTo(containsString("/v2/local/search/address.json")))
                .andRespond(withServerError());

        assertThat(client.geocode("address")).isEmpty();
        server.verify();
    }
}
