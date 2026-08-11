package com.example.calculator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.calculator.entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findTop20ByOrderByCreatedAtDesc();
    List<History> findTop20ByOperationOrderByCreatedAtDesc(String operation);
}
