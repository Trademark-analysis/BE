package com.example.demo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "SELECTED_CLASSIFICATION")
public class SelectedClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private TrademarkAnalysis trademarkAnalysis;

    @Column(name = "classification_code", nullable = false)
    private String classificationCode;

    public SelectedClassification() {}

    public SelectedClassification(String classificationCode) {
        this.classificationCode = classificationCode;
    }

    public Long getId() { return id; }
    public TrademarkAnalysis getTrademarkAnalysis() { return trademarkAnalysis; }
    public void setTrademarkAnalysis(TrademarkAnalysis trademarkAnalysis) { this.trademarkAnalysis = trademarkAnalysis; }
    public String getClassificationCode() { return classificationCode; }
    public void setClassificationCode(String classificationCode) { this.classificationCode = classificationCode; }
}