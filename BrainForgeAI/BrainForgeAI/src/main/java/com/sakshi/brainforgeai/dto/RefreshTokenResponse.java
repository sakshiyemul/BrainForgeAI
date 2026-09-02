package com.sakshi.brainforgeai.dto;

public class RefreshTokenResponse {

    private String token;

    public RefreshTokenResponse() {
    }

    public RefreshTokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}