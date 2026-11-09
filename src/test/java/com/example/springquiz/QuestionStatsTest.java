package com.example.springquiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO (red): implement GET /api/questions/stats.
 *
 * Expected response (200, application/json):
 * <pre>
 * {
 *   "total": 40,
 *   "byTopic":      { "AOP & Proxies": 5, "Boot Auto-configuration": 5, ... },   // 8 topics
 *   "byDifficulty": { "ADVANCED": 24, "EXPERT": 16 }
 * }
 * </pre>
 * Note: "/stats" must take precedence over "/{id}" (today it falls into /{id} and returns 400).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestionStatsTest {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {};

    @Autowired
    private TestRestTemplate http;

    @Test
    void statsEndpointReturnsJson() {
        ResponseEntity<Map<String, Object>> response = getStats();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
    }

    @Test
    void statsReportsTotal() {
        Map<String, Object> stats = getStats().getBody();

        assertThat(stats).containsEntry("total", 40);
    }

    @Test
    void statsCountsFiveQuestionsPerTopic() {
        Map<String, Object> byTopic = section(getStats().getBody(), "byTopic");

        assertThat(byTopic).hasSize(8);
        assertThat(byTopic.values()).allMatch(count -> Integer.valueOf(5).equals(count));
        assertThat(byTopic).containsEntry("Transactions", 5).containsEntry("Security", 5);
    }

    @Test
    void statsCountsByDifficulty() {
        Map<String, Object> byDifficulty = section(getStats().getBody(), "byDifficulty");

        assertThat(byDifficulty).containsOnly(
                Map.entry("ADVANCED", 24),
                Map.entry("EXPERT", 16));
    }

    private ResponseEntity<Map<String, Object>> getStats() {
        return http.exchange("/api/questions/stats", HttpMethod.GET, null, JSON_OBJECT);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> stats, String name) {
        assertThat(stats).isNotNull();
        assertThat(stats.get(name)).as("JSON field '%s'", name).isInstanceOf(Map.class);
        return (Map<String, Object>) stats.get(name);
    }
}
