package com.example.springquiz.metrics;

import com.example.springquiz.question.Question;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Business metric: how many times questions of each topic have been served. */
@Component
public class QuestionMetrics {

    private final MeterRegistry registry;

    public QuestionMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void served(Question question) {
        Counter.builder("quiz.questions.served")
                .description("Questions served to clients")
                .tag("topic", question.getTopic())
                .register(registry)
                .increment();
    }
}
