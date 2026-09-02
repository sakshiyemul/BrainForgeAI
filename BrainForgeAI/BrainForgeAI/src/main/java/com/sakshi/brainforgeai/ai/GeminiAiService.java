package com.sakshi.brainforgeai.ai;

import com.sakshi.brainforgeai.ai.dto.GeminiRequest;
import com.sakshi.brainforgeai.ai.dto.GeminiResponse;
import com.sakshi.brainforgeai.entity.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class GeminiAiService implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAiService.class);

    @Autowired
    private RestTemplate aiRestTemplate;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public String generateAnswer(String question) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Gemini API key is not configured. Returning fallback error message.");
            return "Error: Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable.";
        }

        try {
            String urlWithKey = apiUrl + "?key=" + apiKey;
            GeminiRequest request = new GeminiRequest(question);

            GeminiResponse response = aiRestTemplate.postForObject(urlWithKey, request, GeminiResponse.class);

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                GeminiResponse.Candidate candidate = response.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }

            return "Error: No response generated from Gemini API.";

        } catch (Exception e) {
            logger.error("Failed to generate answer from Gemini API", e);
            return "Error generating answer: " + e.getMessage();
        }
    }

    @Override
    public String generateChatAnswer(List<ChatMessage> messages) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Error: Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable.";
        }

        try {
            String urlWithKey = apiUrl + "?key=" + apiKey;
            List<GeminiRequest.Content> contents = new ArrayList<>();

            for (ChatMessage msg : messages) {
                String role = "user";
                if ("assistant".equalsIgnoreCase(msg.getRole()) || "model".equalsIgnoreCase(msg.getRole())) {
                    role = "model";
                }
                contents.add(new GeminiRequest.Content(role, List.of(new GeminiRequest.Part(msg.getContent()))));
            }

            GeminiRequest request = new GeminiRequest(contents);
            GeminiResponse response = aiRestTemplate.postForObject(urlWithKey, request, GeminiResponse.class);

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                GeminiResponse.Candidate candidate = response.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }

            return "Error: No response generated from Gemini API.";

        } catch (Exception e) {
            logger.error("Failed to generate chat answer from Gemini API", e);
            return "Error generating chat answer: " + e.getMessage();
        }
    }

    @Override
    public void streamChatAnswer(List<ChatMessage> messages, Consumer<String> chunkConsumer) {
        String fullAnswer = generateChatAnswer(messages);
        
        // Emulate streaming chunks over SSE for ultra-smooth UI experience
        String[] words = fullAnswer.split("(?<=\\s)|(?<=\\n)");
        for (String word : words) {
            chunkConsumer.accept(word);
            try {
                Thread.sleep(25); // 25ms per word for natural reading pace
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
