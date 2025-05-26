package io.testomat.dto;

/**
 * DTO record representing a test result for JSON serialization.
 * <p>
 * e.g. from task description
 * 'POST <a href="https://app.testomat.io/api/reporter/a0b1c2d3/testrun?api_key=tstmt_your_api_key">...</a>
 * Content-Type: application/json
 * <p>
 * {
 * "title": "Should login successfully",
 * "status": "passed",
 * "suite_title": "Authentication Tests",
 * "test_id": "@Ta0b0c0d0",
 * "file": "tests/one.java"
 * "run_time": 0.5,
 * "stack": "Error: .... (complete exception trace)"
 * }'
 */
public record TestResultDto(
        String title,
        String testId,
        String suiteTitle,
        String file,
        String status,
        String message,
        String stack) {
}