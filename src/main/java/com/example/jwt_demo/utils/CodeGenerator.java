package com.example.jwt_demo.utils;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.UUID;

public class CodeGenerator {

	private static final SecureRandom secureRandom = new SecureRandom();
    private static final DecimalFormat sixDigitFormat = new DecimalFormat("000000");

    // Generates a 6-digit numeric code
    public static String generate6DigitCode() {
        return sixDigitFormat.format(secureRandom.nextInt(1_000_000));
    }
    
    // Generate a UUID
    public static String generateUUID() {
    	return UUID.randomUUID().toString();
    }
}
