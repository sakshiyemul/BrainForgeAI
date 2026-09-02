package com.sakshi.brainforgeai.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserAnalyticsResponse {

    private long totalConversations;
    private long totalMessages;
    private long totalDocuments;
    private long totalTokensUsed;
    private long totalSavedQuestions;
    private List<ActivityItem> recentActivity;

    public UserAnalyticsResponse() {}

    public UserAnalyticsResponse(long totalConversations, long totalMessages, long totalDocuments, long totalTokensUsed, long totalSavedQuestions, List<ActivityItem> recentActivity) {
        this.totalConversations = totalConversations;
        this.totalMessages = totalMessages;
        this.totalDocuments = totalDocuments;
        this.totalTokensUsed = totalTokensUsed;
        this.totalSavedQuestions = totalSavedQuestions;
        this.recentActivity = recentActivity;
    }

    public static class ActivityItem {
        private String action;
        private String details;
        private int tokens;
        private LocalDateTime timestamp;

        public ActivityItem() {}

        public ActivityItem(String action, String details, int tokens, LocalDateTime timestamp) {
            this.action = action;
            this.details = details;
            this.tokens = tokens;
            this.timestamp = timestamp;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public int getTokens() {
            return tokens;
        }

        public void setTokens(int tokens) {
            this.tokens = tokens;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public long getTotalConversations() {
        return totalConversations;
    }

    public void setTotalConversations(long totalConversations) {
        this.totalConversations = totalConversations;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getTotalTokensUsed() {
        return totalTokensUsed;
    }

    public void setTotalTokensUsed(long totalTokensUsed) {
        this.totalTokensUsed = totalTokensUsed;
    }

    public long getTotalSavedQuestions() {
        return totalSavedQuestions;
    }

    public void setTotalSavedQuestions(long totalSavedQuestions) {
        this.totalSavedQuestions = totalSavedQuestions;
    }

    public List<ActivityItem> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityItem> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
