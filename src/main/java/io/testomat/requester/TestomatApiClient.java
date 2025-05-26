package io.testomat.requester;

import io.testomat.dto.TestResultDto;
import io.testomat.utils.TestResultParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * HTTP client to interact with Testomat.io API.
 * Executes HTTP requests to create, update, and complete test runs.
 */
public class TestomatApiClient {

    /**
     * Product API key for authenticating requests to Testomat.io API.
     * This key is retrieved from the environment variable {@code TESTOMATIO}.
     * It is required for all API interactions (e.g., creating test runs, reporting results).
     * The key typically starts with {@code tstmt_}, indicating a product-level API key
     * that is linked to a specific workspace or project in Testomat.io
     *
     * Never hardcode this value in the source code or share it in public repositories.
     * It must be stored securely, such as in CI/CD environment variables or secret vaults.
     *
     * @see <a href="https://docs.testomat.io/api/test-run/">Testomat.io API documentation</a>
     */
    private final String API_KEY = System.getenv("TESTOMATIO");

    private static final String BASE_URL = "https://app.testomat.io/api/reporter";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_JSON = "application/json";
    private static final String API_URL_SUFFIX = "?api_key=";
    private static final String TESTRUN_API_KEY_URL_SUFFIX = "/testrun?api_key=";
    private static final int HTTP_STATUS_OK = 200;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final TestResultParser parser = new TestResultParser();

    /**
     * Creates a new test run in Testomat API
     *
     * @param title the title/name of the test run; usually project or suite name
     * @return the unique identifier (UID) of the created test run, or empty Optional if creation failed
     */
    public Optional<String> createTestRun(String title) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("Environment variable TESTOMATIO is not set");
        }
        // Формуємо JSON з title
        String json = "{\"title\": \"" + TestResultParser.escapeJson(title) + "\"}";
        String url = BASE_URL + API_URL_SUFFIX + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HTTP_STATUS_OK) {
                return Optional.ofNullable(parser.parseRunUid(response.body()));
            } else {
                System.err.println("Failed to create test run: HTTP " + response.statusCode() + " " + response.body());
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            //Good point to use SaaS decision to fetch specific catch cases, e.g. Sentry.io with purpose to collect
            // amount of catch calls and organize some ideas to handle them
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Reports a single test result to Testomat.io.
     *
     * @param runUid     the UID of the current test run obtained from {@link #createTestRun(String)}
     */
    public void reportTestResult(String runUid, TestResultDto dto) {
        String jsonBody = parser.toJson(dto);
        System.out.println("JsonBody for reportTestResult(): " + jsonBody);

        String url = BASE_URL + "/" + runUid + TESTRUN_API_KEY_URL_SUFFIX + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HTTP_STATUS_OK) {
                System.err.println("Failed to report test result for " + dto.title() + ". Status: " + response.statusCode());
            } else {
                System.out.println("Test run report successful: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            //Good point to use SaaS decision to fetch specific catch cases, e.g. Sentry.io with purpose to collect
            // amount of catch calls and organize some ideas to handle them
            e.printStackTrace();
        }
    }


    /**
     * Sends a final PUT request to mark the test run as finished with total duration.
     *
     * @param runUid      the UID of the current test run (result of {@link #createTestRun(String)})
     * @param durationSec total duration of the test run in seconds
     * @return {@code true} if the operation succeeded (HTTP 200), {@code false} otherwise
     */
    public boolean isFinishedTestRun(String runUid, double durationSec) {
        String json = "{\"status_event\": \"finish\", \"duration\": " + durationSec + "}";
        String url = BASE_URL + "/" + runUid + "?api_key=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HTTP_STATUS_OK) {
                return true;
            } else {
                System.err.println("Failed to finish test run: HTTP " + response.statusCode() + " " + response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            //Good point to use SaaS decision to fetch specific catch cases, e.g. Sentry.io with purpose to collect
            // amount of catch calls and organize some ideas to handle them
            e.printStackTrace();
            return false;
        }
    }
}
