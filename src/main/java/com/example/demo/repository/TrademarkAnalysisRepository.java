package com.example.demo.repository;

import com.example.demo.domain.TrademarkAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrademarkAnalysisRepository extends JpaRepository<TrademarkAnalysis, Long> {
}