package com.example.demo.dto;

import com.example.demo.domain.TrademarkAnalysis;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrademarkAnalysisDto {
    private Long id;
    private String trademarkName; // 상표명 필드
    private String serviceDescription;
    private String imageUrl;
    private String similarityScore;
    private Boolean isAvailable;
    private String resultMessage;
    private LocalDateTime createdAt;
    private List<String> selectedCodes;

    // [무한 루프 탈출 ]: 파이썬이 뱉은 유사 상표 후보 리스트(similar_trademark)를 보관할 주머니 추가
    private List<Map<String, Object>> similar_trademark;

    public TrademarkAnalysisDto(TrademarkAnalysis entity) {
        this.id = entity.getId();
        this.trademarkName = entity.getTrademarkName();
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

    // 추가된 similar_trademark의 Getter와 Setter (자바 서비스단과 프론트엔드 통신 싱크용)
    public List<Map<String, Object>> getSimilar_trademark() {
        return similar_trademark;
    }

    public void setSimilarTrademark(List<Map<String, Object>> similar_trademark) {
        this.similar_trademark = similar_trademark;
    }

    // trademarkName의 Getter
    public String getTrademarkName() { return trademarkName; }

    public Long getId() { return id; }
    public String getServiceDescription() { return serviceDescription; }
    public String getImageUrl() { return imageUrl; }
    public String getSimilarityScore() { return similarityScore; }
    public Boolean getIsAvailable() { return isAvailable; }
    public String getResultMessage() { return resultMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getSelectedCodes() { return selectedCodes; }
}