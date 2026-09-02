package com.sakshi.brainforgeai.controller;

import com.sakshi.brainforgeai.dto.ChatMessageRequest;
import com.sakshi.brainforgeai.dto.ChatMessageResponse;
import com.sakshi.brainforgeai.dto.ConversationResponse;
import com.sakshi.brainforgeai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/chat")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {

    @Autowired
    private ChatService chatService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Operation(summary = "Get user conversations", description = "Returns all conversations for the authenticated user.")
    @GetMapping("/conversations")
    public List<ConversationResponse> getConversations() {
        return chatService.getUserConversations();
    }

    @Operation(summary = "Create conversation", description = "Initializes a new chat thread.")
    @PostMapping("/conversations")
    public ConversationResponse createConversation(@RequestBody(required = false) Map<String, String> body) {
        String title = body != null ? body.get("title") : "New Conversation";
        return chatService.createConversation(title);
    }

    @Operation(summary = "Get conversation messages", description = "Retrieves all messages in a conversation.")
    @GetMapping("/conversations/{id}")
    public List<ChatMessageResponse> getMessages(@PathVariable Long id) {
        return chatService.getMessages(id);
    }

    @Operation(summary = "Delete conversation", description = "Deletes a conversation and its messages.")
    @DeleteMapping("/conversations/{id}")
    public String deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return "Conversation deleted successfully.";
    }

    @Operation(summary = "Send message", description = "Sends a message in the conversation and returns assistant response.")
    @PostMapping("/conversations/{id}/messages")
    public ChatMessageResponse sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageRequest request) {
        return chatService.sendMessage(id, request.getContent(), request.isUseKnowledgeBase());
    }

    @Operation(summary = "Stream message response", description = "Streams AI response via Server-Sent Events (SSE).")
    @GetMapping(value = "/conversations/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @PathVariable Long id,
            @RequestParam String prompt,
            @RequestParam(defaultValue = "false") boolean useKnowledgeBase,
            org.springframework.security.core.Authentication authentication) {

        String userEmail = authentication.getName();
        SseEmitter emitter = new SseEmitter(120_000L); // 2 minute timeout

        executor.execute(() -> {
            try {
                chatService.streamMessageForUser(id, userEmail, prompt, useKnowledgeBase, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.send(SseEmitter.event().name("DONE").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
