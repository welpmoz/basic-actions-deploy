package com.example.springquiz.batch;

/** Raw CSV row as read by the FlatFileItemReader. */
public record QuestionCsv(Long id, String topic, String difficulty, String question, String answer) {
}
