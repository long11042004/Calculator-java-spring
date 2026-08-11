package com.example.calculator.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "calculation_history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String expression;

    @Column(nullable = false)
    private String result;

    @Column
    private String operation;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public History() {}

    public History(String expression, String result, String operation) {
        this.expression = expression;
        this.result = result;
        this.operation = operation;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getExpression() { return expression; }
    public String getResult() { return result; }
    public String getOperation() { return operation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
