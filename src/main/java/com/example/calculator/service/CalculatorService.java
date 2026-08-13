package com.example.calculator.service;

import java.math.BigDecimal;

public interface CalculatorService {

    BigDecimal calculate(BigDecimal first, BigDecimal second, String op);

    default BigDecimal calculate(String first, String second, String op) {
        return calculate(new BigDecimal(first), new BigDecimal(second), op);
    }

    String calculateAsString(BigDecimal first, BigDecimal second, String op);

    default String calculateAsString(String first, String second, String op) {
        return calculateAsString(new BigDecimal(first), new BigDecimal(second), op);
    }
}
