package com.example.calculator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.calculator.entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h FROM History h ORDER BY h.createdAt DESC")
    List<History> findRecentHistory();

    @Query("SELECT h FROM History h WHERE h.operation = :operation ORDER BY h.createdAt DESC")
    List<History> findRecentHistoryByOperation(@Param("operation") String operation);
}
