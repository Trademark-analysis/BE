package com.example.demo.dto;

public class AnalysisResponse {
    private String status;                  // "SUCCESS" 또는 "FAIL"
    private TrademarkAnalysisDto result;    // DB 저장 값 + ML 분석 결과가 다 들어있는 DTO

    public AnalysisResponse(String status, TrademarkAnalysisDto result) {
        this.status = status;
        this.result = result;
    }

    // Getter, Setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public TrademarkAnalysisDto getResult() { return result; }
    public void setResult(TrademarkAnalysisDto result) { this.result = result; }
}