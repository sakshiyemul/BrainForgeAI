package com.sakshi.brainforgeai.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private String role;
    private String content;
    private Integer tokensUsed;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {}

    public ChatMessageResponse(Long id, String role, String content, Integer tokensUsed, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.tokensUsed = tokensUsed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
