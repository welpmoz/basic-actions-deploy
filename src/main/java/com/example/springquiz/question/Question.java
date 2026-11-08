package com.example.springquiz.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Question {

    @Id
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(nullable = false, length = 3000)
    private String answer;

    protected Question() {
    }

    public Question(Long id, String topic, Difficulty difficulty, String question, String answer) {
        this.id = id;
        this.topic = topic;
        this.difficulty = difficulty;
        this.question = question;
        this.answer = answer;
    }

    public Long getId() { return id; }

    public String getTopic() { return topic; }

    public Difficulty getDifficulty() { return difficulty; }

    public String getQuestion() { return question; }

    public String getAnswer() { return answer; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Question other && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Question{id=" + id + ", topic=" + topic + "}";
    }
}
