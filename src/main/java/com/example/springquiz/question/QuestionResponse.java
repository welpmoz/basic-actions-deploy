package com.example.springquiz.question;

public record QuestionResponse(Long id, String topic, Difficulty difficulty, String question, String answer) {

    static QuestionResponse from(Question q) {
        return new QuestionResponse(q.getId(), q.getTopic(), q.getDifficulty(), q.getQuestion(), q.getAnswer());
    }
}
