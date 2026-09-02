package com.sakshi.brainforgeai.controller;
import com.sakshi.brainforgeai.entity.Question;
import com.sakshi.brainforgeai.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.Optional;
import com.sakshi.brainforgeai.dto.QuestionRequest;
import com.sakshi.brainforgeai.dto.QuestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/questions")
@SecurityRequirement(name = "Bearer Authentication")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Operation(
            summary = "Create a question",
            description = "Creates a new question and answer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Question created successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have permission"
            )
    })

    @PostMapping
    public QuestionResponse createQuestion(@RequestBody QuestionRequest request) {

        Question question = new Question();

        question.setQuestion(request.getQuestion());
        question.setAnswer(request.getAnswer());

        Question savedQuestion = questionService.saveQuestion(question);

        return new QuestionResponse(
                savedQuestion.getId(),
                savedQuestion.getQuestion(),
                savedQuestion.getAnswer()
        );
    }

    @Operation(
            summary = "Get all questions",
            description = "Returns all questions available in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Questions retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have permission"
            )
    })

    @GetMapping("/all")
    public List<QuestionResponse> getAllQuestions() {

        List<Question> questions = questionService.getAllQuestions();

        return questions.stream()
                .map(question -> new QuestionResponse(
                        question.getId(),
                        question.getQuestion(),
                        question.getAnswer()
                ))
                .toList();
    }
    @Operation(
            summary = "Get my questions",
            description = "Returns all questions created by the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Questions retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have permission"
            )
    })

    @GetMapping("/my")
    public List<QuestionResponse> getMyQuestions() {

        List<Question> questions = questionService.getMyQuestions();

        return questions.stream()
                .map(question -> new QuestionResponse(
                        question.getId(),
                        question.getQuestion(),
                        question.getAnswer()
                ))
                .toList();
    }
    @Operation(
            summary = "Get question by ID",
            description = "Returns a question using its unique ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Question found successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have permission"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })

    @GetMapping("/{id}")
    public QuestionResponse getQuestionById(@PathVariable Long id) {

        Optional<Question> question = questionService.getQuestionById(id);

        if (question.isEmpty()) {
            throw new RuntimeException("Question not found with id: " + id);
        }

        Question q = question.get();

        return new QuestionResponse(
                q.getId(),
                q.getQuestion(),
                q.getAnswer()
        );
    }

    @Operation(
            summary = "Update a question",
            description = "Updates an existing question and answer using its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Question updated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have permission"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })

    @PutMapping("/{id}")
    public QuestionResponse updateQuestion(
            @PathVariable Long id,
            @RequestBody Question updatedQuestion) {

        Question savedQuestion =
                questionService.updateQuestion(id, updatedQuestion);

        if (savedQuestion == null) {
            return null;
        }

        return new QuestionResponse(
                savedQuestion.getId(),
                savedQuestion.getQuestion(),
                savedQuestion.getAnswer()
        );
    }
    @Operation(
            summary = "Delete a question",
            description = "Deletes an existing question using its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Question deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have permission"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })

    @DeleteMapping("/{id}")
    public String deleteQuestion(@PathVariable Long id) {

        return questionService.deleteQuestion(id);
    }
}