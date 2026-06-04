package com.example.demo.dto;

import com.example.demo.domain.TrademarkAnalysis;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TrademarkAnalysisDto {
    private Long id;
    private String serviceDescription;
    private String imageUrl;
    private String similarityScore;
    private Boolean isAvailable;
    private String resultMessage;
    private LocalDateTime createdAt;
    private List<String> selectedCodes;

    public TrademarkAnalysisDto(TrademarkAnalysis entity) {
        this.id = entity.getId();
        this.serviceDescription = entity.getServiceDescription();
        this.imageUrl = entity.getImageUrl();
        this.similarityScore = entity.getSimilarityScore();
        this.isAvailable = entity.getIsAvailable();
        this.resultMessage = entity.getResultMessage();
        this.createdAt = entity.getCreatedAt();
        this.selectedCodes = entity.getSelectedClassifications().stream()
                .map(c -> c.getClassificationCode())
                .collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public String getServiceDescription() { return serviceDescription; }
    public String getImageUrl() { return imageUrl; }
    public String getSimilarityScore() { return similarityScore; }
    public Boolean getIsAvailable() { return isAvailable; }
    public String getResultMessage() { return resultMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getSelectedCodes() { return selectedCodes; }
}