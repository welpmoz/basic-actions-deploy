package com.example.springquiz.batch;

import com.example.springquiz.question.Difficulty;
import com.example.springquiz.question.Question;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionItemProcessorTest {

    private final QuestionItemProcessor processor = new QuestionItemProcessor();

    @Test
    void mapsValidRowToTrimmedQuestion() {
        var row = new QuestionCsv(7L, "  Transactions ", "expert", " Why? ", " Because. ");

        Question q = processor.process(row);

        assertThat(q.getId()).isEqualTo(7L);
        assertThat(q.getTopic()).isEqualTo("Transactions");
        assertThat(q.getDifficulty()).isEqualTo(Difficulty.EXPERT);
        assertThat(q.getQuestion()).isEqualTo("Why?");
        assertThat(q.getAnswer()).isEqualTo("Because.");
    }

    @Test
    void rejectsBlankQuestion() {
        var row = new QuestionCsv(1L, "AOP", "ADVANCED", "  ", "answer");

        assertThatThrownBy(() -> processor.process(row))
                .isInstanceOf(InvalidQuestionException.class)
                .hasMessageContaining("question");
    }

    @Test
    void rejectsMissingId() {
        var row = new QuestionCsv(null, "AOP", "ADVANCED", "q", "a");

        assertThatThrownBy(() -> processor.process(row))
                .isInstanceOf(InvalidQuestionException.class)
                .hasMessageContaining("id");
    }

    @Test
    void rejectsUnknownDifficulty() {
        var row = new QuestionCsv(1L, "AOP", "BEGINNER", "q", "a");

        assertThatThrownBy(() -> processor.process(row))
                .isInstanceOf(InvalidQuestionException.class)
                .hasMessageContaining("difficulty");
    }
}
