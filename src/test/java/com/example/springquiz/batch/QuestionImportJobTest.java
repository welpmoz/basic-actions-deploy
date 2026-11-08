package com.example.springquiz.batch;

import com.example.springquiz.question.Question;
import com.example.springquiz.question.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(properties = "spring.batch.job.enabled=false")
class QuestionImportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private QuestionRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void importsAllFortyQuestionsInChunks() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(40);
        assertThat(step.getWriteCount()).isEqualTo(40);
        assertThat(step.getCommitCount()).isGreaterThanOrEqualTo(4); // chunk size 10

        List<Question> all = repository.findAll();
        assertThat(all).hasSize(40);
        assertThat(all).extracting(Question::getId).doesNotHaveDuplicates();
    }

    @Test
    void everyTopicHasFiveQuestions() throws Exception {
        jobLauncherTestUtils.launchJob();

        Map<String, Long> perTopic = repository.findAll().stream()
                .collect(Collectors.groupingBy(Question::getTopic, Collectors.counting()));

        assertThat(perTopic).hasSize(8);
        assertThat(perTopic.values()).containsOnly(5L);
    }

    @Test
    void rerunIsIdempotent() throws Exception {
        jobLauncherTestUtils.launchJob();
        jobLauncherTestUtils.launchJob();

        assertThat(repository.count()).isEqualTo(40);
    }
}
