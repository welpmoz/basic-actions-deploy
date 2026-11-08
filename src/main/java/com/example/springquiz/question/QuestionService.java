package com.example.springquiz.question;

import com.example.springquiz.metrics.QuestionMetrics;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    public List<String> topics() {
        return all().stream().map(Question::getTopic).distinct().sorted().toList();
    }

    private List<Question> all() {
        return repository.findAll(Sort.by("id"));
    }
}
