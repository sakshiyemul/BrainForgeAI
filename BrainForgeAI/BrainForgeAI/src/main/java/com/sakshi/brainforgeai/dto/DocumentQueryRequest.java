package com.sakshi.brainforgeai.dto;

import jakarta.validation.constraints.NotBlank;

public class DocumentQueryRequest {

    @NotBlank(message = "Query is required")
    private String query;

    public DocumentQueryRequest() {}

    public DocumentQueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
