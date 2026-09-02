package com.sakshi.brainforgeai.service;

import com.sakshi.brainforgeai.ai.AiService;
import com.sakshi.brainforgeai.dto.ChatMessageResponse;
import com.sakshi.brainforgeai.dto.ConversationResponse;
import com.sakshi.brainforgeai.entity.ChatMessage;
import com.sakshi.brainforgeai.entity.Conversation;
import com.sakshi.brainforgeai.entity.UsageMetric;
import com.sakshi.brainforgeai.entity.User;
import com.sakshi.brainforgeai.exception.UnauthorizedActionException;
import com.sakshi.brainforgeai.repository.ChatMessageRepository;
import com.sakshi.brainforgeai.repository.ConversationRepository;
import com.sakshi.brainforgeai.repository.UsageMetricRepository;
import com.sakshi.brainforgeai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageMetricRepository usageMetricRepository;

    @Autowired
    private AiService aiService;

    @Autowired(required = false)
    @Lazy
    private RagService ragService;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations() {
        User user = getAuthenticatedUser();
        List<Conversation> list = conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        List<ConversationResponse> result = new ArrayList<>();

        for (Conversation c : list) {
            String lastMsg = "";
            if (!c.getMessages().isEmpty()) {
                lastMsg = c.getMessages().get(c.getMessages().size() - 1).getContent();
                if (lastMsg.length() > 60) {
                    lastMsg = lastMsg.substring(0, 57) + "...";
                }
            }
            result.add(new ConversationResponse(
                    c.getId(),
                    c.getTitle(),
                    c.getCreatedAt(),
                    c.getUpdatedAt(),
                    c.getMessages().size(),
                    lastMsg
            ));
        }
        return result;
    }

    @Transactional
    public ConversationResponse createConversation(String initialTitle) {
        User user = getAuthenticatedUser();
        String title = (initialTitle != null && !initialTitle.isBlank()) ? initialTitle : "New Conversation";
        if (title.length() > 40) {
            title = title.substring(0, 37) + "...";
        }

        Conversation conversation = new Conversation(title, user);
        Conversation saved = conversationRepository.save(conversation);

        return new ConversationResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                0,
                ""
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long conversationId) {
        User user = getAuthenticatedUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to access this conversation");
        }

        List<ChatMessage> list = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return list.stream()
                .map(m -> new ChatMessageResponse(m.getId(), m.getRole(), m.getContent(), m.getTokensUsed(), m.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void deleteConversation(Long conversationId) {
        User user = getAuthenticatedUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to delete this conversation");
        }

        conversationRepository.delete(conversation);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long conversationId, String prompt, boolean useKnowledgeBase) {
        User user = getAuthenticatedUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to update this conversation");
        }

        // 1. Prepare user message & prompt
        String processedPrompt = prompt;
        if (useKnowledgeBase && ragService != null) {
            processedPrompt = ragService.augmentPromptWithKnowledge(prompt, user);
        }

        ChatMessage userMessage = new ChatMessage(conversation, "user", prompt);
        chatMessageRepository.save(userMessage);

        // Auto-update title if it was default
        if ("New Conversation".equals(conversation.getTitle())) {
            String newTitle = prompt.length() > 30 ? prompt.substring(0, 27) + "..." : prompt;
            conversation.setTitle(newTitle);
        }

        // 2. Fetch history for multi-turn context
        List<ChatMessage> history = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        
        // Pass augmented prompt to AI for the latest turn
        List<ChatMessage> contextHistory = new ArrayList<>(history);
        if (!processedPrompt.equals(prompt)) {
            ChatMessage augmented = new ChatMessage(conversation, "user", processedPrompt);
            contextHistory.set(contextHistory.size() - 1, augmented);
        }

        // 3. Generate Answer
        String answer = aiService.generateChatAnswer(contextHistory);

        // 4. Estimate tokens and save assistant message
        int tokens = (prompt.length() + answer.length()) / 4;
        ChatMessage assistantMessage = new ChatMessage(conversation, "assistant", answer);
        assistantMessage.setTokensUsed(tokens);
        ChatMessage savedAssistant = chatMessageRepository.save(assistantMessage);

        // Record telemetry
        usageMetricRepository.save(new UsageMetric(user, useKnowledgeBase ? "RAG_CHAT" : "CHAT", tokens, "Conversation #" + conversationId));

        return new ChatMessageResponse(
                savedAssistant.getId(),
                savedAssistant.getRole(),
                savedAssistant.getContent(),
                savedAssistant.getTokensUsed(),
                savedAssistant.getCreatedAt()
        );
    }

    @Transactional
    public void streamMessage(Long conversationId, String prompt, boolean useKnowledgeBase, Consumer<String> chunkConsumer) {
        User user = getAuthenticatedUser();
        streamMessageForUser(conversationId, user.getEmail(), prompt, useKnowledgeBase, chunkConsumer);
    }

    @Transactional
    public void streamMessageForUser(Long conversationId, String userEmail, String prompt, boolean useKnowledgeBase, Consumer<String> chunkConsumer) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + userEmail));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to access this conversation");
        }

        // 1. Prepare user prompt
        String processedPrompt = prompt;
        if (useKnowledgeBase && ragService != null) {
            processedPrompt = ragService.augmentPromptWithKnowledge(prompt, user);
        }

        ChatMessage userMessage = new ChatMessage(conversation, "user", prompt);
        chatMessageRepository.save(userMessage);

        if ("New Conversation".equals(conversation.getTitle())) {
            String newTitle = prompt.length() > 30 ? prompt.substring(0, 27) + "..." : prompt;
            conversation.setTitle(newTitle);
        }

        List<ChatMessage> history = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<ChatMessage> contextHistory = new ArrayList<>(history);
        if (!processedPrompt.equals(prompt)) {
            ChatMessage augmented = new ChatMessage(conversation, "user", processedPrompt);
            contextHistory.set(contextHistory.size() - 1, augmented);
        }

        StringBuilder completeResponse = new StringBuilder();

        aiService.streamChatAnswer(contextHistory, chunk -> {
            completeResponse.append(chunk);
            chunkConsumer.accept(chunk);
        });

        // Save complete message after stream completes
        String fullAnswer = completeResponse.toString();
        int tokens = (prompt.length() + fullAnswer.length()) / 4;
        ChatMessage assistantMessage = new ChatMessage(conversation, "assistant", fullAnswer);
        assistantMessage.setTokensUsed(tokens);
        chatMessageRepository.save(assistantMessage);

        usageMetricRepository.save(new UsageMetric(user, "STREAM_CHAT", tokens, "Conversation #" + conversationId));
    }
}
