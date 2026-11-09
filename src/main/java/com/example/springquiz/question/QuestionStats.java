package com.example.springquiz.question;

import java.util.Map;

/** Aggregate counts of the question catalogue. Maps are sorted (topic name / enum order). */
public record QuestionStats(long total, Map<String, Long> byTopic, Map<Difficulty, Long> byDifficulty) {
}
