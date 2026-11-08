package com.example.springquiz.question;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
class QuestionController {

    private final QuestionService service;

    QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping
    List<QuestionResponse> list(@RequestParam(required = false) String topic,
                                @RequestParam(required = false) Difficulty difficulty) {
        return service.findAll(topic, difficulty).stream().map(QuestionResponse::from).toList();
    }

    @GetMapping("/{id}")
    QuestionResponse byId(@PathVariable Long id) {
        return QuestionResponse.from(service.findById(id));
    }

    @GetMapping("/random")
    QuestionResponse random() {
        return QuestionResponse.from(service.random());
    }

    @GetMapping("/topics")
    List<String> topics() {
        return service.topics();
    }
}
