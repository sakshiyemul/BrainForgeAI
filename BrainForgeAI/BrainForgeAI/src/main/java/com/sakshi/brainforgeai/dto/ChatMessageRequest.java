package com.sakshi.brainforgeai.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    private boolean useKnowledgeBase = false;

    public ChatMessageRequest() {}

    public ChatMessageRequest(String content, boolean useKnowledgeBase) {
        this.content = content;
        this.useKnowledgeBase = useKnowledgeBase;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUseKnowledgeBase() {
        return useKnowledgeBase;
    }

    public void setUseKnowledgeBase(boolean useKnowledgeBase) {
        this.useKnowledgeBase = useKnowledgeBase;
    }
}
