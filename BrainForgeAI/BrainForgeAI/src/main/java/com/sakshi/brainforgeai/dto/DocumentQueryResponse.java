package com.sakshi.brainforgeai.dto;

import java.util.List;

public class DocumentQueryResponse {

    private String answer;
    private List<String> relevantSnippets;

    public DocumentQueryResponse() {}

    public DocumentQueryResponse(String answer, List<String> relevantSnippets) {
        this.answer = answer;
        this.relevantSnippets = relevantSnippets;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getRelevantSnippets() {
        return relevantSnippets;
    }

    public void setRelevantSnippets(List<String> relevantSnippets) {
        this.relevantSnippets = relevantSnippets;
    }
}
