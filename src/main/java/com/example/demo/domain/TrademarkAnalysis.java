package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TRADEMARK_ANALYSIS")
public class TrademarkAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_description", length = 1000)
    private String serviceDescription;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "similarity_score")
    private String similarityScore;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "result_message", length = 2000)
    private String resultMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "trademarkAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SelectedClassification> selectedClassifications = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void addClassification(SelectedClassification classification) {
        this.selectedClassifications.add(classification);
        classification.setTrademarkAnalysis(this);
    }

    public TrademarkAnalysis() {}

    public Long getId() { return id; }
    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(String similarityScore) { this.similarityScore = similarityScore; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SelectedClassification> getSelectedClassifications() { return selectedClassifications; }
}