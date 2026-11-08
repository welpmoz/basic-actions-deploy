package com.example.springquiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Full-stack test: batch job runs at startup, API serves data, metrics are exposed. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability // metrics exporters (Prometheus) are disabled in tests by default
class ApplicationIT {

    @Autowired
    private TestRestTemplate http;

    @Test
    void startupBatchLoadsFortyQuestions() {
        ResponseEntity<List> response = http.getForEntity("/api/questions", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(40);
    }

    @Test
    void unknownQuestionIs404() {
        assertThat(http.getForEntity("/api/questions/4040", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void healthIsUp() {
        ResponseEntity<Map> health = http.getForEntity("/actuator/health", Map.class);

        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).containsEntry("status", "UP");
    }

    @Test
    void infoExposesAppVersion() {
        String info = http.getForObject("/actuator/info", String.class);

        assertThat(info).contains("\"version\":\"local\"");
    }

    @Test
    void prometheusExposesCustomMetric() {
        http.getForEntity("/api/questions/1", String.class);

        String metrics = http.getForObject("/actuator/prometheus", String.class);

        assertThat(metrics).contains("quiz_questions_served_total");
        assertThat(metrics).contains("http_server_requests_seconds");
    }
}
