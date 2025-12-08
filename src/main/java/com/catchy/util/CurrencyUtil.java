package com.catchy.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class for currency conversions and formatting
 * Handles Indian Rupees (INR) currency operations
 */
public class CurrencyUtil {
    
    private static final String CURRENCY_CODE = "INR";
    private static final String CURRENCY_SYMBOL = "₹";
    private static final BigDecimal PAISE_MULTIPLIER = BigDecimal.valueOf(100);

    /**
     * Convert INR amount to paise (smallest unit for Stripe API)
     * @param amountInr - Amount in Indian Rupees
     * @return Amount in paise as long (for Stripe API)
     */
    public static long convertInrToPaise(BigDecimal amountInr) {
        if (amountInr == null || amountInr.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        return amountInr.multiply(PAISE_MULTIPLIER).longValue();
    }

    /**
     * Convert paise to INR amount
     * @param paiseAmount - Amount in paise
     * @return Amount in Indian Rupees as BigDecimal
     */
    public static BigDecimal convertPaiseToInr(long paiseAmount) {
        return BigDecimal.valueOf(paiseAmount).divide(PAISE_MULTIPLIER);
    }

    /**
     * Format amount with INR currency symbol
     * @param amount - Amount in INR
     * @return Formatted string with currency symbol
     */
    public static String formatInr(BigDecimal amount) {
        if (amount == null) {
            return CURRENCY_SYMBOL + " 0.00";
        }
        Locale indiaLocale = new Locale.Builder().setLanguage("en").setRegion("IN").build();
        NumberFormat nf = NumberFormat.getCurrencyInstance(indiaLocale);
        nf.setCurrency(Currency.getInstance(CURRENCY_CODE));
        return nf.format(amount.doubleValue());
    }

    /**
     * Format amount with INR symbol and specified decimal places
     * @param amount - Amount in INR
     * @param decimalPlaces - Number of decimal places
     * @return Formatted string
     */
    public static String formatInr(BigDecimal amount, int decimalPlaces) {
        if (amount == null) {
            return CURRENCY_SYMBOL + " 0.00";
        }
        BigDecimal rounded = amount.setScale(decimalPlaces, java.math.RoundingMode.HALF_UP);
        return CURRENCY_SYMBOL + " " + rounded.toPlainString();
    }

    /**
     * Get currency code
     * @return "INR"
     */
    public static String getCurrencyCode() {
        return CURRENCY_CODE;
    }

    /**
     * Get currency symbol
     * @return "₹"
     */
    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }

    /**
     * Validate if amount is positive and valid for payment
     * @param amount - Amount in INR
     * @return true if valid, false otherwise
     */
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Round amount to 2 decimal places (standard for INR)
     * @param amount - Amount to round
     * @return Rounded amount
     */
    public static BigDecimal roundAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
