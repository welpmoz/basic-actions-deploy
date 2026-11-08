package com.example.springquiz.batch;

import com.example.springquiz.question.Question;
import com.example.springquiz.question.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Chunk-oriented import: CSV (reader) -> validation (processor) -> JPA (writer),
 * committed every {@code quiz.import.chunk-size} items.
 */
@Configuration
class QuestionImportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(QuestionImportJobConfig.class);

    @Bean
    FlatFileItemReader<QuestionCsv> questionReader(@Value("${quiz.import.source}") Resource source) {
        return new FlatFileItemReaderBuilder<QuestionCsv>()
                .name("questionReader")
                .resource(source)
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited()
                .quoteCharacter('"')
                .names("id", "topic", "difficulty", "question", "answer")
                .targetType(QuestionCsv.class)
                .build();
    }

    @Bean
    QuestionItemProcessor questionProcessor() {
        return new QuestionItemProcessor();
    }

    @Bean
    RepositoryItemWriter<Question> questionWriter(QuestionRepository repository) {
        return new RepositoryItemWriterBuilder<Question>().repository(repository).build();
    }

    @Bean
    Step importQuestionsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             FlatFileItemReader<QuestionCsv> questionReader, QuestionItemProcessor questionProcessor,
                             RepositoryItemWriter<Question> questionWriter,
                             @Value("${quiz.import.chunk-size:10}") int chunkSize) {
        return new StepBuilder("importQuestionsStep", jobRepository)
                .<QuestionCsv, Question>chunk(chunkSize, transactionManager)
                .reader(questionReader)
                .processor(questionProcessor)
                .writer(questionWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution step) {
                        log.info("Question import finished: read={}, written={}, commits={}, status={}",
                                step.getReadCount(), step.getWriteCount(), step.getCommitCount(), step.getStatus());
                        return step.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    Job importQuestionsJob(JobRepository jobRepository, Step importQuestionsStep) {
        return new JobBuilder("importQuestionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importQuestionsStep)
                .build();
    }
}
