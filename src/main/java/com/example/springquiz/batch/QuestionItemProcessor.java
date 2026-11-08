package com.example.springquiz.batch;

import com.example.springquiz.question.Difficulty;
import com.example.springquiz.question.Question;
import org.springframework.batch.item.ItemProcessor;

import java.util.Arrays;
import java.util.Locale;

/** Validates and normalises a CSV row into a {@link Question}. An invalid row fails the job. */
public class QuestionItemProcessor implements ItemProcessor<QuestionCsv, Question> {

    @Override
    public Question process(QuestionCsv row) {
        if (row.id() == null) {
            throw new InvalidQuestionException("Row is missing an id");
        }
        return new Question(
                row.id(),
                required(row, "topic", row.topic()),
                difficulty(row),
                required(row, "question", row.question()),
                required(row, "answer", row.answer()));
    }

    private static String required(QuestionCsv row, String field, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidQuestionException("Row " + row.id() + ": " + field + " must not be blank");
        }
        return value.trim();
    }

    private static Difficulty difficulty(QuestionCsv row) {
        String value = required(row, "difficulty", row.difficulty()).toUpperCase(Locale.ROOT);
        try {
            return Difficulty.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidQuestionException("Row " + row.id() + ": unknown difficulty " + value
                    + ", expected one of " + Arrays.toString(Difficulty.values()));
        }
    }
}
