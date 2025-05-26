package io.testomat.extension;

import io.testomat.annotations.TestId;
import io.testomat.annotations.Title;
import io.testomat.dto.TestResultDto;
import io.testomat.requester.TestomatApiClient;
import io.testomat.utils.TestResultParser;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Handles test lifecycle callbacks and delegates reporting of test results to {@link TestomatApiClient}.
 */
public class TestomatExtension implements BeforeAllCallback, AfterAllCallback, TestWatcher, BeforeTestExecutionCallback {

    /**
     * Prefix added to fallback test run title when package name is missing or empty.
     */
    private static final String FALLBACK_PROJECT_TITLE = "JUnit Test Run (stub-case)";

    /**
     * Message prefix for test result reporting logs.
     */
    private static final String TEST_ID_RESULT_LOG_PREFIX = "tid://@";
    protected static final String TEST_TITLE_RESULT_LOG_PREFIX = "title://";

    private final TestomatApiClient apiClient = new TestomatApiClient();
    private String runUid;
    private long runStartTime;

    /**
     * Called before all tests in a container.
     * Initializes the test run by creating a new test run on Testomat.io.
     *
     * @param context the current extension context, providing access to test class metadata
     * @throws Exception if unable to start the test run (e.g. API key missing)
     */
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        runStartTime = System.currentTimeMillis();

        // Attempt to get package name as project title
        String projectTitle = context.getRequiredTestClass().getPackageName();
        if (projectTitle == null || projectTitle.isEmpty()) {
            projectTitle = FALLBACK_PROJECT_TITLE;
        }

        // Create test run on Testomat.io and store its UID
        runUid = apiClient.createTestRun(projectTitle)
                .orElseThrow(() -> new IllegalStateException("Cannot start Testomat test run"));

        System.out.println("Test run started, UID: " + runUid);
    }

    /**
     * Called before each test execution.
     * Logs test metadata if available.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Optional<Method> testMethod = context.getTestMethod();

        testMethod.ifPresent(method -> {
            Optional<TestId> testId = Optional.ofNullable(method.getAnnotation(TestId.class));
            Optional<Title> title = Optional.ofNullable(method.getAnnotation(Title.class));

            testId.ifPresent(id -> System.out.println(TEST_ID_RESULT_LOG_PREFIX + id.value()));
            title.ifPresent(t -> System.out.println(TEST_TITLE_RESULT_LOG_PREFIX + t.value()));
        });
    }

    /**
     * Called after all tests in a container.
     * Finalizes the test run on Testomat.io, reporting total duration.
     *
     * @param context the current extension context
     * @throws Exception if finalizing the test run fails
     */
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (runUid == null) return;

        double durationSec = (System.currentTimeMillis() - runStartTime) / 1000.0;

        if (apiClient.isFinishedTestRun(runUid, durationSec)) {
            System.out.println("Test run finished successfully. Total duration (s): " + durationSec);
        } else {
            System.err.println("Failed to finish test run.");
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        reportTestResult(context, "passed", null);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        reportTestResult(context, "failed", cause);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        reportTestResult(context, "skipped", cause);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        reportTestResult(context, "skipped", null);
    }

    /**
     * Reports a single test result via {@link TestomatApiClient}.
     *
     * @param context   current extension context with test method and class info
     * @param status    test result status (passed, failed, skipped)
     * @param throwable optional throwable cause if test failed or aborted
     */
    private void reportTestResult(ExtensionContext context, String status, Throwable throwable) {
        if (runUid == null) return;

        Method testMethod = context.getRequiredTestMethod();

        Optional<TestId> testIdAnnotation = Optional.ofNullable(testMethod.getAnnotation(TestId.class));
        Optional<Title> titleAnnotation = Optional.ofNullable(testMethod.getAnnotation(Title.class));

        // testId     the external test identifier (can be null)
        String testId = testIdAnnotation.map(TestId::value).orElse(null);

        // title      the display title/name of the test
        String testTitle = titleAnnotation.map(Title::value).orElse(testMethod.getName());

        // suiteTitle the name of the test suite or class
        String suiteTitle = context.getRequiredTestClass().getSimpleName();

        // file       the source file name where the test is located
        String fileName = suiteTitle + ".java";

        // message    optional message (usually error message on failure)
        String message = "Test finished with status: " + status;

        // stack      optional stack trace in case of failure
        String stack = null;

        if (throwable != null) {
            message = throwable.getMessage();
            stack = TestResultParser.getStackTrace(throwable);
        }

        // init DTO
        TestResultDto dto = new TestResultDto(
                testTitle,
                testId,
                suiteTitle,
                fileName,
                status,
                message,
                stack
        );

        apiClient.reportTestResult(runUid, dto);
    }
}
