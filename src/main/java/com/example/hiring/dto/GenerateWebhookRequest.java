package com.example.hiring.dto;

public class GenerateWebhookRequest {
    private String name;
    private String regNo;
    private String email;

    public GenerateWebhookRequest(String name, String regNo, String email) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
    }

    // getters & setters
    public String getName() { return name; }
    public String getRegNo() { return regNo; }
    public String getEmail() { return email; }
}
