package com.example.springquiz.question;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    private final Question q1 = new Question(1L, "Transactions", Difficulty.EXPERT, "Why?", "Because.");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private QuestionService service;

    @Test
    void listsQuestions() throws Exception {
        when(service.findAll(null, null)).thenReturn(List.of(q1));

        mvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].topic").value("Transactions"))
                .andExpect(jsonPath("$[0].difficulty").value("EXPERT"))
                .andExpect(jsonPath("$[0].question").value("Why?"))
                .andExpect(jsonPath("$[0].answer").value("Because."));
    }

    @Test
    void passesFiltersToService() throws Exception {
        when(service.findAll("Transactions", Difficulty.EXPERT)).thenReturn(List.of(q1));

        mvc.perform(get("/api/questions").param("topic", "Transactions").param("difficulty", "EXPERT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invalidDifficultyIsBadRequest() throws Exception {
        mvc.perform(get("/api/questions").param("difficulty", "EASY"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    void getsQuestionById() throws Exception {
        when(service.findById(1L)).thenReturn(q1);

        mvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void unknownIdIsProblemDetail404() throws Exception {
        when(service.findById(99L)).thenThrow(new QuestionNotFoundException(99L));

        mvc.perform(get("/api/questions/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Question not found"))
                .andExpect(jsonPath("$.detail").value("Question 99 not found"));
    }

    @Test
    void returnsRandomQuestion() throws Exception {
        when(service.random()).thenReturn(q1);

        mvc.perform(get("/api/questions/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void listsTopics() throws Exception {
        when(service.topics()).thenReturn(List.of("AOP & Proxies", "Transactions"));

        mvc.perform(get("/api/questions/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1]").value("Transactions"));
    }
}
