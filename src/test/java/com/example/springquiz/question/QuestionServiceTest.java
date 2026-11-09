package com.example.springquiz.question;

import com.example.springquiz.metrics.QuestionMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionServiceTest {

    private final Question q1 = new Question(1L, "AOP & Proxies", Difficulty.ADVANCED, "q1", "a1");
    private final Question q2 = new Question(2L, "AOP & Proxies", Difficulty.EXPERT, "q2", "a2");
    private final Question q3 = new Question(3L, "Transactions", Difficulty.EXPERT, "q3", "a3");

    private QuestionRepository repository;
    private SimpleMeterRegistry registry;
    private QuestionService service;

    @BeforeEach
    void setUp() {
        repository = mock(QuestionRepository.class);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(q1, q2, q3));
        registry = new SimpleMeterRegistry();
        service = new QuestionService(repository, new QuestionMetrics(registry));
    }

    @Test
    void findAllWithoutFiltersReturnsEverything() {
        assertThat(service.findAll(null, null)).containsExactly(q1, q2, q3);
    }

    @Test
    void filtersByTopicIgnoringCase() {
        assertThat(service.findAll("aop & proxies", null)).containsExactly(q1, q2);
    }

    @Test
    void filtersByDifficulty() {
        assertThat(service.findAll(null, Difficulty.EXPERT)).containsExactly(q2, q3);
    }

    @Test
    void filtersByTopicAndDifficulty() {
        assertThat(service.findAll("AOP & Proxies", Difficulty.EXPERT)).containsExactly(q2);
    }

    @Test
    void findByIdReturnsQuestionAndCountsIt() {
        when(repository.findById(3L)).thenReturn(Optional.of(q3));

        assertThat(service.findById(3L)).isEqualTo(q3);
        assertThat(registry.get("quiz.questions.served").tag("topic", "Transactions").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void randomReturnsOneOfTheQuestions() {
        assertThat(service.random()).isIn(q1, q2, q3);
    }

    @Test
    void randomThrowsWhenEmpty() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        assertThatThrownBy(() -> service.random()).isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void statsCountsTotalTopicsAndDifficulties() {
        QuestionStats stats = service.stats();

        assertThat(stats.total()).isEqualTo(3);
        assertThat(stats.byTopic()).containsExactly(
                java.util.Map.entry("AOP & Proxies", 2L),
                java.util.Map.entry("Transactions", 1L));
        assertThat(stats.byDifficulty()).containsExactly(
                java.util.Map.entry(Difficulty.ADVANCED, 1L),
                java.util.Map.entry(Difficulty.EXPERT, 2L));
    }

    @Test
    void statsListsEveryDifficultyEvenWhenEmpty() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        QuestionStats stats = service.stats();

        assertThat(stats.total()).isZero();
        assertThat(stats.byTopic()).isEmpty();
        assertThat(stats.byDifficulty()).containsOnlyKeys(Difficulty.values()).containsValues(0L);
    }

    @Test
    void topicsAreDistinctAndSorted() {
        assertThat(service.topics()).containsExactly("AOP & Proxies", "Transactions");
    }
}
