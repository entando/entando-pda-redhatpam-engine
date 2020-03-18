package org.entando.plugins.pda.pam.service.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.drools.core.io.impl.ClassPathResource;
import org.entando.plugins.pda.core.engine.Connection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UIServicesClient;
import org.springframework.http.HttpStatus;

public class KieApiServiceTest {

    private static final String KIE_SERVER_PATH = "/kie-server/server";
    private static final String QUERIES_DEFINITIONS = "/queries/definitions/";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    private KieApiService kieApiService;

    @Before
    public void init() {
        kieApiService = new KieApiService();

        stubFor(get(urlEqualTo(KIE_SERVER_PATH))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody(new ClassPathResource("kie-server-result.json").getBytes())));
        stubCustomQueries();
    }

    private void stubCustomQueries() {
        stubFor(put(urlEqualTo(KIE_SERVER_PATH + QUERIES_DEFINITIONS + KieApiService.PDA_GROUPS))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.CREATED.value())));
    }

    @Test
    public void shouldCreateNewKieServicesClient() {
        // Given
        Connection connection = getConnection();

        // When
        KieServicesClient kieServicesClient = kieApiService.getKieServicesClient(connection);

        // Then
        assertThat(kieServicesClient).isNotNull();
        assertThat(kieServicesClient).isEqualTo(kieApiService.getKieServicesClientMap().get(connection));
    }

    @Test
    public void shouldHitCacheOnSecondRequestWithSameConnection() {
        // Given
        Connection connection = getConnection();

        // When
        kieApiService.getKieServicesClient(connection);
        kieApiService.getKieServicesClient(connection);

        // Then
        verify(1, getRequestedFor(urlEqualTo(KIE_SERVER_PATH)));
    }

    @Test
    public void shouldUpdateCacheIfConnectionChanges() {
        // Given
        Connection connection = getConnection();

        // When
        kieApiService.getKieServicesClient(connection);
        Connection connection2 = getConnection();
        connection2.setUsername("test2");
        kieApiService.getKieServicesClient(connection2);

        // Then
        verify(2, getRequestedFor(urlEqualTo(KIE_SERVER_PATH)));
        assertThat(kieApiService.getKieServicesClientMap().size()).isEqualTo(1);
    }

    @Test
    public void shouldGetProcessServicesClient() {
        // Given
        Connection connection = getConnection();

        // When
        ProcessServicesClient processServicesClient = kieApiService.getProcessServicesClient(connection);

        // Then
        assertThat(processServicesClient).isNotNull();
    }

    @Test
    public void shouldGetQueryServicesClient() {
        // Given
        Connection connection = getConnection();

        // When
        QueryServicesClient queryServicesClient = kieApiService.getQueryServicesClient(connection);

        // Then
        assertThat(queryServicesClient).isNotNull();
    }

    @Test
    public void shouldGetUIServicesClient() {
        // Given
        Connection connection = getConnection();

        // When
        UIServicesClient uiServicesClient = kieApiService.getUiServicesClient(connection);

        // Then
        assertThat(uiServicesClient).isNotNull();
    }

    @Test
    public void shouldRegisterCustomQueries() {
        // Given
        Connection connection = getConnection();

        // When
        kieApiService.getKieServicesClient(connection);

        // Then
        verify(putRequestedFor(urlEqualTo(KIE_SERVER_PATH + QUERIES_DEFINITIONS + KieApiService.PDA_GROUPS)));
    }

    private Connection getConnection() {
        return Connection.builder()
                .name("testConnection")
                .url("http://localhost:" + wireMockRule.port() + KIE_SERVER_PATH)
                .username("test")
                .password("test")
                .build();
    }


}
