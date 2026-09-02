package com.sakshi.brainforgeai.controller;

import com.sakshi.brainforgeai.ai.AiService;
import com.sakshi.brainforgeai.dto.AiRequest;
import com.sakshi.brainforgeai.dto.AiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@SecurityRequirement(name = "Bearer Authentication")
public class AiController {

    @Autowired
    private AiService aiService;

    @Operation(
            summary = "Ask the AI assistant",
            description = "Submits a question to the configured Gemini AI service and returns the generated answer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated response from AI"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (e.g. empty question)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Valid JWT token is required"
                )
    })
    @PostMapping("/ask")
    public AiResponse askAi(@Valid @RequestBody AiRequest request) {
        String answer = aiService.generateAnswer(request.getQuestion());
        return new AiResponse(answer);
    }
}
