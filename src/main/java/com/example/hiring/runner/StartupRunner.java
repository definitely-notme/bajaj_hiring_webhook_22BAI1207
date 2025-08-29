package com.example.hiring.runner;

import com.example.hiring.dto.GenerateWebhookRequest;
import com.example.hiring.dto.GenerateWebhookResponse;
import com.example.hiring.dto.TestWebhookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final RestTemplate restTemplate;

    @Value("${app.generate-webhook-url}")
    private String generateWebhookUrl;

    @Value("${app.name}")
    private String name;

    @Value("${app.regNo}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    public StartupRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Step 1: Call generateWebhook
        log.info("Requesting webhook...");
        GenerateWebhookRequest req = new GenerateWebhookRequest(name, regNo, email);
        ResponseEntity<GenerateWebhookResponse> resp =
                restTemplate.postForEntity(generateWebhookUrl, req, GenerateWebhookResponse.class);

        GenerateWebhookResponse body = resp.getBody();
        if (body == null || body.getWebhook() == null) {
            log.error("Failed to get webhook from response: {}", resp);
            return;
        }

        String webhookUrl = body.getWebhook();
        String accessToken = body.getAccessToken();
        log.info("Received webhook URL: {}", webhookUrl);

        // Step 2: Pick correct SQL file (odd/even)
        int lastTwo = extractLastTwoDigits(regNo);
        boolean even = (lastTwo % 2 == 0);
        String sql = even ? loadSql("question2.sql") : loadSql("question1.sql");
        log.info("Chosen SQL: {}", sql);

        // Step 3: Send SQL to webhook
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken); // API might require "Bearer " + token, check once

        TestWebhookRequest bodyReq = new TestWebhookRequest(sql);
        HttpEntity<TestWebhookRequest> entity = new HttpEntity<>(bodyReq, headers);

        ResponseEntity<String> submitResp = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
        log.info("Webhook response: {}", submitResp.getBody());
    }

    private int extractLastTwoDigits(String regNo) {
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() < 2) return Integer.parseInt(digits);
        return Integer.parseInt(digits.substring(digits.length() - 2));
    }

    private String loadSql(String filename) {
        try {
            return new String(getClass().getClassLoader().getResourceAsStream(filename).readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("SQL file not found: " + filename);
        }
    }
}
