package org.semicolonlab.infrastructure.output.exceptionhandler.analyzers;

import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.AbstractExceptionAnalyzer;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class TestExceptionAnalyzer extends AbstractExceptionAnalyzer {

    private static final Set<String> TEST_EXCEPTIONS = Set.of(
            "org.junit.ComparisonFailure",
            "org.junit.jupiter.api.AssertionFailedError",
            "java.lang.AssertionError",
            "org.mockito.exceptions.base.MockitoException",
            "org.mockito.exceptions.misusing.MockitoConfigurationException",
            "org.mockito.exceptions.verification.VerificationInOrderFailure",
            "org.mockito.exceptions.verification.WantedButNotInvoked",
            "org.hamcrest.AssertionError",
            "org.testng.AssertionError"
    );

    public TestExceptionAnalyzer() {
        super(TEST_EXCEPTIONS, 75);
        initializeTestPatterns();
    }

    private void initializeTestPatterns() {
        // JUnit patterns
        addMessagePattern("(?i)expected.*but.*was");
        addMessagePattern("(?i)assertion.*failed");
        addMessagePattern("(?i)test.*failed");
        addMessagePattern("(?i)wanted but not invoked");
        addMessagePattern("(?i)never wanted here");
        addMessagePattern("(?i)mock.*cannot be returned");
        addMessagePattern("(?i)argument.*should be provided");
        addMessagePattern("(?i)failed to load.*applicationcontext");
        addMessagePattern("(?i)test.*execution.*failed");
    }

    @Override
    public ExceptionAnalysisResult analyze(Throwable t) {
        String rootCause = extractRootCause(t);
        TestFailureType failureType = categorizeTestFailure(t);
        Map<String, Object> context = buildTestContext(t);

        return ExceptionAnalysisResult.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode("TEST_" + failureType.name())
                .title("Test " + failureType.getDisplayName())
                .message(rootCause)
                .rootCause(rootCause)
                .context(context)
                .tags(Set.of("test", "failure", failureType.name().toLowerCase()))
                .category("TEST")
                .remediation(getTestRemediation(failureType, t))
                .isRecoverable(true)
                .build();
    }

    private TestFailureType categorizeTestFailure(Throwable t) {
        String className = t.getClass().getSimpleName();
        String message = Optional.ofNullable(t.getMessage()).orElse("").toLowerCase();

        if (className.contains("ComparisonFailure") || message.contains("expected") && message.contains("but")) {
            return TestFailureType.ASSERTION_FAILURE;
        } else if (className.contains("MockitoException")) {
            return TestFailureType.MOCK_FAILURE;
        } else if (message.contains("context") && message.contains("load")) {
            return TestFailureType.CONTEXT_LOAD_FAILURE;
        } else if (message.contains("verification")) {
            return TestFailureType.VERIFICATION_FAILURE;
        } else if (className.contains("AssertionError")) {
            return TestFailureType.ASSERTION_FAILURE;
        }

        return TestFailureType.GENERIC_TEST_FAILURE;
    }

    private Map<String, Object> buildTestContext(Throwable t) {
        Map<String, Object> context = buildContext(t);

        context.put("testFramework", detectTestFramework(t));
        context.put("failureType", categorizeTestFailure(t).name());

//        if (t instanceof org.junit.ComparisonFailure) {
//            org.junit.ComparisonFailure comparisonFailure = (org.junit.ComparisonFailure) t;
//            context.put("expected", comparisonFailure.getExpected());
//            context.put("actual", comparisonFailure.getActual());
//        }

        return context;
    }

    private String detectTestFramework(Throwable t) {
        String className = t.getClass().getName();
        if (className.startsWith("org.junit.jupiter")) return "JUnit 5";
        if (className.startsWith("org.junit")) return "JUnit 4";
        if (className.startsWith("org.testng")) return "TestNG";
        if (className.startsWith("org.mockito")) return "Mockito";
        return "Unknown";
    }

    private String getTestRemediation(TestFailureType failureType, Throwable t) {
        return switch (failureType) {
            case ASSERTION_FAILURE -> "Review test assertions and expected vs actual values. " +
                    "Check if the test logic matches the implementation behavior.";
            case MOCK_FAILURE -> "Review mock setup and configuration. Ensure mocks are properly initialized " +
                    "and behavior is correctly stubbed.";
            case VERIFICATION_FAILURE -> "Check mock interaction verification. Ensure the expected method calls " +
                    "are being made with correct parameters.";
            case CONTEXT_LOAD_FAILURE -> "Review Spring test configuration, component scanning, and test context setup. " +
                    "Check for missing beans or configuration issues.";
            case GENERIC_TEST_FAILURE -> "Review test implementation and check for logical errors or setup issues.";
        };
    }

    private enum TestFailureType {
        ASSERTION_FAILURE("Assertion Failure"),
        MOCK_FAILURE("Mock Configuration Failure"),
        VERIFICATION_FAILURE("Mock Verification Failure"),
        CONTEXT_LOAD_FAILURE("Test Context Load Failure"),
        GENERIC_TEST_FAILURE("Test Execution Failure");

        private final String displayName;

        TestFailureType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }
}

