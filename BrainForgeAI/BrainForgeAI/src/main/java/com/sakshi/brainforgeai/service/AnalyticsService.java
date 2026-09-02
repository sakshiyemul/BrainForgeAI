package com.sakshi.brainforgeai.service;

import com.sakshi.brainforgeai.dto.AdminAnalyticsResponse;
import com.sakshi.brainforgeai.dto.UserAnalyticsResponse;
import com.sakshi.brainforgeai.entity.UsageMetric;
import com.sakshi.brainforgeai.entity.User;
import com.sakshi.brainforgeai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UsageMetricRepository usageMetricRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @Transactional(readOnly = true)
    public UserAnalyticsResponse getUserAnalytics() {
        User user = getAuthenticatedUser();

        long convCount = conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).size();
        long docCount = documentRepository.countByUserEmail(user.getEmail());
        long savedQCount = questionRepository.findByUserEmail(user.getEmail()).size();
        long totalTokens = usageMetricRepository.sumTokensByUserEmail(user.getEmail());

        List<UsageMetric> metrics = usageMetricRepository.findByUserEmailOrderByTimestampDesc(user.getEmail());
        long msgCount = metrics.stream().filter(m -> "CHAT".equals(m.getActionType()) || "STREAM_CHAT".equals(m.getActionType()) || "RAG_CHAT".equals(m.getActionType())).count();

        List<UserAnalyticsResponse.ActivityItem> recent = metrics.stream()
                .limit(10)
                .map(m -> new UserAnalyticsResponse.ActivityItem(m.getActionType(), m.getDetails(), m.getTokensEstimated(), m.getTimestamp()))
                .toList();

        return new UserAnalyticsResponse(
                convCount,
                msgCount,
                docCount,
                totalTokens,
                savedQCount,
                recent
        );
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse getAdminAnalytics() {
        long totalUsers = userRepository.count();
        long totalQuestions = questionRepository.count();
        long totalConversations = conversationRepository.count();
        long totalDocuments = documentRepository.count();
        long totalTokens = usageMetricRepository.sumAllTokens();

        List<UsageMetric> allMetrics = usageMetricRepository.findAll();
        Map<String, Long> actionCounts = new HashMap<>();

        for (UsageMetric m : allMetrics) {
            actionCounts.put(m.getActionType(), actionCounts.getOrDefault(m.getActionType(), 0L) + 1);
        }

        return new AdminAnalyticsResponse(
                totalUsers,
                totalQuestions,
                totalConversations,
                totalDocuments,
                totalTokens,
                actionCounts
        );
    }
}
