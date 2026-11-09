package com.example.springquiz.question;

import com.example.springquiz.metrics.QuestionMetrics;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository repository;
    private final QuestionMetrics metrics;

    public QuestionService(QuestionRepository repository, QuestionMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public List<Question> findAll(String topic, Difficulty difficulty) {
        return all().stream()
                .filter(q -> topic == null || q.getTopic().equalsIgnoreCase(topic.trim()))
                .filter(q -> difficulty == null || q.getDifficulty() == difficulty)
                .toList();
    }

    public Question findById(Long id) {
        Question question = repository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));
        metrics.served(question);
        return question;
    }

    public Question random() {
        List<Question> all = all();
        if (all.isEmpty()) {
            throw new QuestionNotFoundException("No questions loaded");
        }
        Question question = all.get(ThreadLocalRandom.current().nextInt(all.size()));
        metrics.served(question);
        return question;
    }

    public QuestionStats stats() {
        List<Question> all = all();

        Map<String, Long> byTopic = all.stream()
                .collect(Collectors.groupingBy(Question::getTopic, TreeMap::new, Collectors.counting()));

        Map<Difficulty, Long> byDifficulty = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            byDifficulty.put(difficulty, 0L);
        }
        all.forEach(q -> byDifficulty.merge(q.getDifficulty(), 1L, Long::sum));

        return new QuestionStats(all.size(), byTopic, byDifficulty);
    }

    public List<String> topics() {
        return all().stream().map(Question::getTopic).distinct().sorted().toList();
    }

    private List<Question> all() {
        return repository.findAll(Sort.by("id"));
    }
}
