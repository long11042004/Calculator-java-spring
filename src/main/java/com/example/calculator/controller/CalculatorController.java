package com.example.calculator.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.calculator.service.CalculatorService;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/")
    public String calculator() {
        return "redirect:/index.html";
    }

    @GetMapping("/api/calc")
    @ResponseBody
    public ResponseEntity<String> calculateApi(@RequestParam(name = "first") BigDecimal first,
                                               @RequestParam(name = "second", defaultValue = "0") BigDecimal second,
                                               @RequestParam(name = "op") String op) {
        try {
            return ResponseEntity.ok(calculatorService.calculateAsString(first, second, op));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error");
        }
    }
}
