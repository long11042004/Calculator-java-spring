package com.example.calculator.controller;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.calculator.entity.History;
import com.example.calculator.repository.HistoryRepository;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryRepository historyRepository;

    public HistoryController(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public List<History> getHistory(@RequestParam(required = false) String op) {
        if (op != null && !op.isBlank()) {
            String expectedOperation = normalizeOperation(op);
            return historyRepository.findTop20ByOrderByCreatedAtDesc()
                    .stream()
                    .filter(item -> Objects.equals(resolveOperation(item), expectedOperation))
                    .toList();
        }
        return historyRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @PostMapping
    public History saveHistory(@RequestParam String expression,
                                          @RequestParam String result,
                                          @RequestParam(required = false) String op) {
        return historyRepository.save(new History(expression, result, normalizeOperation(op)));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearHistory() {
        historyRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    private String normalizeOperation(String op) {
        if (op == null || op.isBlank()) {
            return null;
        }

        String normalized = op.trim();
        return switch (normalized.toLowerCase(Locale.ROOT)) {
            case "sqrt" -> "sqrt";
            case "inv" -> "inv";
            default -> normalized;
        };
    }

    private String resolveOperation(History item) {
        String stored = normalizeOperation(item.getOperation());
        if (stored != null) {
            return stored;
        }
        return extractOperationFromExpression(item.getExpression());
    }

    private String extractOperationFromExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        String expr = expression.trim();
        if (expr.startsWith("√(")) {
            return "sqrt";
        }
        if (expr.startsWith("1/x(")) {
            return "inv";
        }

        String[] binaryOps = {"+", "-", "*", "/", "%", "^"};
        for (String operator : binaryOps) {
            if (expr.contains(" " + operator + " ")) {
                return operator;
            }
        }
        return null;
    }
}
