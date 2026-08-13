package com.example.calculator.controller;

import java.util.List;
import java.util.Locale;

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
        String normalizedOperation = normalizeOperation(op);

        if (normalizedOperation != null && !normalizedOperation.isBlank()) {
            return historyRepository.findRecentHistoryByOperation(normalizedOperation);
        }

        return historyRepository.findRecentHistory();
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
}
