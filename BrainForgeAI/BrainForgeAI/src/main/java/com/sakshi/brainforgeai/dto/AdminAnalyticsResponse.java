package com.sakshi.brainforgeai.dto;

import java.util.Map;

public class AdminAnalyticsResponse {

    private long totalUsers;
    private long totalQuestions;
    private long totalConversations;
    private long totalDocuments;
    private long totalTokensConsumed;
    private Map<String, Long> actionCounts;

    public AdminAnalyticsResponse() {}

    public AdminAnalyticsResponse(long totalUsers, long totalQuestions, long totalConversations, long totalDocuments, long totalTokensConsumed, Map<String, Long> actionCounts) {
        this.totalUsers = totalUsers;
        this.totalQuestions = totalQuestions;
        this.totalConversations = totalConversations;
        this.totalDocuments = totalDocuments;
        this.totalTokensConsumed = totalTokensConsumed;
        this.actionCounts = actionCounts;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public long getTotalConversations() {
        return totalConversations;
    }

    public void setTotalConversations(long totalConversations) {
        this.totalConversations = totalConversations;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getTotalTokensConsumed() {
        return totalTokensConsumed;
    }

    public void setTotalTokensConsumed(long totalTokensConsumed) {
        this.totalTokensConsumed = totalTokensConsumed;
    }

    public Map<String, Long> getActionCounts() {
        return actionCounts;
    }

    public void setActionCounts(Map<String, Long> actionCounts) {
        this.actionCounts = actionCounts;
    }
}
