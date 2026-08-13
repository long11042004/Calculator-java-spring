package com.example.calculator.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class DefaultCalculatorService implements CalculatorService {

    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext SQRT_CONTEXT = new MathContext(SCALE + 6, ROUNDING_MODE);

    @Override
    public BigDecimal calculate(BigDecimal first, BigDecimal second, String op) {
        return switch (op) {
            case "+" -> normalize(first.add(second));
            case "-" -> normalize(first.subtract(second));
            case "*" -> normalize(first.multiply(second));
            case "/" -> {
                if (second.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Khong the chia cho 0");
                }
                yield first.divide(second, SCALE, ROUNDING_MODE);
            }
            case "%" -> {
                if (second.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Khong the lay du khi so chia la 0");
                }
                yield normalize(first.remainder(second));
            }
            case "^" -> {
                double value = Math.pow(first.doubleValue(), second.doubleValue());
                if (Double.isNaN(value)) {
                    throw new ArithmeticException("Luy thua khong hop le voi du lieu dau vao");
                }
                if (!Double.isFinite(value)) {
                    throw new ArithmeticException("Ket qua qua lon");
                }
                yield normalize(BigDecimal.valueOf(value));
            }
            case "sqrt" -> {
                if (first.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ArithmeticException("Khong the tinh can bac hai cua so am");
                }
                yield normalize(first.sqrt(SQRT_CONTEXT));
            }
            case "inv" -> {
                if (first.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Khong the tinh nghich dao cua 0");
                }
                yield BigDecimal.ONE.divide(first, SCALE, ROUNDING_MODE);
            }
            default -> throw new ArithmeticException("Invalid operator: " + op);
        };
    }

    @Override
    public BigDecimal calculate(String first, String second, String op) {
        try {
            return calculate(new BigDecimal(first), new BigDecimal(second), op);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Du lieu dau vao khong hop le", e);
        }
    }

    @Override
    public String calculateAsString(BigDecimal first, BigDecimal second, String op) {
        BigDecimal result = calculate(first, second, op);
        return result.stripTrailingZeros().toPlainString();
    }

    @Override
    public String calculateAsString(String first, String second, String op) {
        try {
            return calculateAsString(new BigDecimal(first), new BigDecimal(second), op);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Du lieu dau vao khong hop le", e);
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }
}
