package pl.wrona.webserver.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.wrona.webserver.BaseIntegrationTest;
import pl.wrona.webserver.client.geoapify.GeoResponse;
import pl.wrona.webserver.client.geoapify.GeoapifyService;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class GeoapifyServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GeoapifyService geoapifyService;

    @Test
    void shouldReturnPositionBaseOnAddress() {
        // given
        String text = "Nakielska 5/38; 01-106 Warszawa; Polska";

        // and
        mockGeoapifyClient.stubFor(
                get(urlPathEqualTo("/v1/geocode/search"))
                        .withQueryParam("text", equalTo(text))
                        .withQueryParam("apiKey", equalTo("geoapify-api-key"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody((
                                        // language=JSON
                                        """
                                                {
                                                    "type": "FeatureCollection",
                                                    "features": [
                                                        {
                                                            "type": "Feature",
                                                            "properties": {
                                                                "datasource": {
                                                                    "sourcename": "openstreetmap",
                                                                    "attribution": "© OpenStreetMap contributors",
                                                                    "license": "Open Database License",
                                                                    "url": "https://www.openstreetmap.org/copyright"
                                                                },
                                                                "country": "United Kingdom",
                                                                "country_code": "gb",
                                                                "state": "England",
                                                                "county": "Greater London",
                                                                "city": "London",
                                                                "postcode": "W1H 1LJ",
                                                                "suburb": "Marylebone",
                                                                "street": "Upper Montagu Street",
                                                                "housenumber": "38",
                                                                "iso3166_2": "GB-ENG",
                                                                "lon": -0.16030636023550826,
                                                                "lat": 51.52016005,
                                                                "state_code": "ENG",
                                                                "result_type": "building",
                                                                "formatted": "38 Upper Montagu Street, London, W1H 1LJ, United Kingdom",
                                                                "address_line1": "38 Upper Montagu Street",
                                                                "address_line2": "London, W1H 1LJ, United Kingdom",
                                                                "category": "building.residential",
                                                                "timezone": {
                                                                "name": "Europe/London",
                                                                "offset_STD": "+00:00",
                                                                "offset_STD_seconds": 0,
                                                                "offset_DST": "+01:00",
                                                                "offset_DST_seconds": 3600,
                                                                "abbreviation_STD": "GMT",
                                                                "abbreviation_DST": "BST"
                                                            },
                                                            "plus_code": "9C3XGRCQ+3V",
                                                            "plus_code_short": "GRCQ+3V London, Greater London, United Kingdom",
                                                            "rank": {
                                                            "importance": 0.00000999999999995449,
                                                            "popularity": 8.988490181891963,
                                                            "confidence": 1,
                                                            "confidence_city_level": 1,
                                                            "confidence_street_level": 1,
                                                            "confidence_building_level": 1,
                                                            "match_type": "full_match"
                                                        },
                                                        "place_id": "51dcb14637eb84c4bf59c6b7c19a94c24940f00102f901370cef1100000000c00203"
                                                },
                                                "geometry": {
                                                  "type": "Point",
                                                  "coordinates": [-0.16030636023550826, 51.52016005]
                                                },
                                                "bbox": [
                                                -0.160394,
                                                51.5201061,
                                                -0.1602251,
                                                51.5202273
                                                ]
                                                }
                                                ],
                                                "query": {
                                                "text": "38 Upper Montagu Street, Westminster W1H 1LJ, United Kingdom",
                                                "parsed": {
                                                "housenumber": "38",
                                                "street": "upper montagu street",
                                                "postcode": "w1h 1lj",
                                                "district": "westminster",
                                                "country": "united kingdom",
                                                "expected_type": "building"
                                                }
                                                }
                                                }
                                                """))));

        // when
        GeoResponse geoResponse = geoapifyService.geoapify(text);

        // then
        assertThat(geoResponse).isNotNull();
        assertThat(geoResponse.features()).hasSize(1);
        assertThat(geoResponse.features().get(0).properties().city()).isEqualTo("Greater London, United Kingdom");
    }

}