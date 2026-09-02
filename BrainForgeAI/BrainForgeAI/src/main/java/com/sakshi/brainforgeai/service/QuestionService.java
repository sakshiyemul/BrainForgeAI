package com.sakshi.brainforgeai.service;

import com.sakshi.brainforgeai.exception.UnauthorizedActionException;
import com.sakshi.brainforgeai.entity.Question;
import com.sakshi.brainforgeai.entity.User;
import com.sakshi.brainforgeai.repository.QuestionRepository;
import com.sakshi.brainforgeai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Question> getMyQuestions() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return questionRepository.findByUserEmail(email);
    }

    public Question saveQuestion(Question question) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        question.setUser(user);

        return questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    public Question updateQuestion(Long id, Question updatedQuestion) {
        Optional<Question> existingQuestion = questionRepository.findById(id);

        if (existingQuestion.isEmpty()) {
            throw new RuntimeException("Question not found with id: " + id);
        }

        Question question = existingQuestion.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;

        User currentUser = email != null ? userRepository.findByEmail(email).orElse(null) : null;

        boolean isAdmin = (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) ||
                (authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        boolean isOwner = question.getUser() != null &&
                email != null &&
                question.getUser().getEmail().equalsIgnoreCase(email);

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException(
                    "You are not authorized to update this question"
            );
        }

        question.setQuestion(updatedQuestion.getQuestion());
        question.setAnswer(updatedQuestion.getAnswer());

        return questionRepository.save(question);
    }

    public String deleteQuestion(Long id) {
        Optional<Question> existingQuestion = questionRepository.findById(id);

        if (existingQuestion.isEmpty()) {
            throw new RuntimeException("Question not found with id: " + id);
        }

        Question question = existingQuestion.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;

        User currentUser = email != null ? userRepository.findByEmail(email).orElse(null) : null;

        boolean isAdmin = (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) ||
                (authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        boolean isOwner = question.getUser() != null &&
                email != null &&
                question.getUser().getEmail().equalsIgnoreCase(email);

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete this question"
            );
        }

        questionRepository.delete(question);

        return "Question deleted successfully.";
    }
}