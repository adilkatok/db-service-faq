package kz.adilka.dbservice.controllers;

import kz.adilka.dbservice.exceptions.ResourceNotFoundException;
import kz.adilka.dbservice.models.Answer;
import kz.adilka.dbservice.models.Question;
import kz.adilka.dbservice.models.User;
import kz.adilka.dbservice.repositories.QuestionRepository;
import kz.adilka.dbservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users/{userId}/questions")
    public List<Question> getAnswersByUserId(@PathVariable Long userId) {
        return questionRepository.findByUserId(userId);
    }

    @GetMapping("/questions")
    public Page<Question> getQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }


    @PostMapping("/questions/{userId}")
    public Question createQuestion(@PathVariable Long userId, @Valid @RequestBody Question question) {
        Optional<User> user = userRepository.findById(userId);
        if(userId!=null && userId>0){
            question.setUser(user.get());
            questionRepository.save(question);
            return question;
        }
        return null;
    }

    @PutMapping("/questions/{questionId}")
    public Question updateQuestion(@PathVariable Long questionId,
                                   @Valid @RequestBody Question questionRequest) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    question.setTitle(questionRequest.getTitle());
                    question.setDescription(questionRequest.getDescription());
                    question.setUser(questionRequest.getUser());
                    return questionRepository.save(question);
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }


    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    questionRepository.delete(question);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }
}
