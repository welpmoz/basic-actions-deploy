package com.example.springquiz.question;

public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(Long id) {
        super("Question " + id + " not found");
    }

    public QuestionNotFoundException(String message) {
        super(message);
    }
}
