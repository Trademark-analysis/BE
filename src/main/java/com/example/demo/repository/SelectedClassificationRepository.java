package com.example.demo.repository;

import com.example.demo.domain.SelectedClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectedClassificationRepository extends JpaRepository<SelectedClassification, Long> {
}