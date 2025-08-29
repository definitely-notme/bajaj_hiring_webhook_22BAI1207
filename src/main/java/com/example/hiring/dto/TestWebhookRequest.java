package com.example.hiring.dto;

public class TestWebhookRequest {
    private String finalQuery;

    public TestWebhookRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() { return finalQuery; }
}
