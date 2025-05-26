package io.testomat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.testomat.dto.TestResultDto;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class for parsing and serializing test results JSON.
 * Uses Jackson ObjectMapper for JSON processing.
 */
public class TestResultParser {

    protected static final String UID_FIELD_NAME = "uid";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serializes the test result DTO to JSON string.
     */
    public String toJson(TestResultDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            //Good point to use SaaS decision to fetch specific catch cases, e.g. Sentry.io with purpose to collect
            // amount of catch calls and organize some ideas to handle them
            throw new RuntimeException("Failed to serialize test result to JSON", e);
        }
    }

    /**
     * Parses the run UID from Testomat API create test run response.
     * @param jsonResponse JSON response string from API
     * @return run UID string if present; null otherwise
     */
    public String parseRunUid(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.has(UID_FIELD_NAME)) {
                return root.get(UID_FIELD_NAME).asText();
            }
        } catch (JsonProcessingException e) {
            //Good point to use SaaS decision to fetch specific catch cases, e.g. Sentry.io with purpose to collect
            // amount of catch calls and organize some ideas to handle them
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Escapes JSON special characters in a string.
     */
    public static String escapeJson(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Converts a Throwable's stack trace to string.
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
