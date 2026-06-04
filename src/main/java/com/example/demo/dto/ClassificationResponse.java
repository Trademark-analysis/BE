package com.example.demo.dto;

public class ClassificationResponse {
    private String code;        // "제9류", "제42류" 등
    private String description; // "컴퓨터, 소프트웨어", "IT 서비스" 등

    public ClassificationResponse(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter, Setter
    public String getCode() { return code; }
    public String getDescription() { return description; }
}
