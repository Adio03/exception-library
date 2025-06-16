package org.semicolonlab.infrastructure.output.exceptionhandler.analyzers;

import lombok.Getter;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.AbstractExceptionAnalyzer;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisResult;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TestContextExceptionAnalyzer extends AbstractExceptionAnalyzer {

    private static final Set<String> TEST_CONTEXT_EXCEPTIONS = Set.of(
            "org.springframework.test.context.TestContextException",
            "org.springframework.test.context.ContextLoadException",
            "org.springframework.beans.factory.BeanCreationException",
            "org.springframework.beans.factory.NoSuchBeanDefinitionException",
            "org.springframework.boot.test.context.SpringBootTestContextBootstrapper",
            "org.springframework.test.context.transaction.TransactionConfigurationError"
    );

    public TestContextExceptionAnalyzer() {
        super(TEST_CONTEXT_EXCEPTIONS, 80);
        initializeContextPatterns();
    }

    private void initializeContextPatterns() {
        addMessagePattern("(?i)failed to load.*applicationcontext");
        addMessagePattern("(?i)no qualifying bean");
        addMessagePattern("(?i)bean.*could not be found");
        addMessagePattern("(?i)circular dependency");
        addMessagePattern("(?i)test.*configuration.*error");
        addMessagePattern("(?i)@springboottest");
        addMessagePattern("(?i)test.*context.*failed");
    }

    @Override
    public ExceptionAnalysisResult analyze(Throwable t) {
        String rootCause = extractRootCause(t);
        TestContextFailureType failureType = categorizeContextFailure(t);
        Map<String, Object> context = buildTestContextDetails(t);

        return ExceptionAnalysisResult.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode("TEST_CONTEXT_" + failureType.name())
                .title("Test Context " + failureType.getDisplayName())
                .message(rootCause)
                .rootCause(rootCause)
                .context(context)
                .tags(Set.of("test", "context", "spring", failureType.name().toLowerCase()))
                .category("TEST_CONTEXT")
                .remediation(getContextRemediation(failureType))
                .isRecoverable(true)
                .build();
    }

    private TestContextFailureType categorizeContextFailure(Throwable t) {
        String message = Optional.ofNullable(t.getMessage()).orElse("").toLowerCase();
        String className = t.getClass().getSimpleName();

        if (message.contains("failed to load") && message.contains("context")) {
            return TestContextFailureType.CONTEXT_LOAD_FAILURE;
        } else if (className.contains("NoSuchBeanDefinitionException") || message.contains("no qualifying bean")) {
            return TestContextFailureType.BEAN_NOT_FOUND;
        } else if (className.contains("BeanCreationException")) {
            return TestContextFailureType.BEAN_CREATION_FAILURE;
        } else if (message.contains("circular dependency")) {
            return TestContextFailureType.CIRCULAR_DEPENDENCY;
        } else if (message.contains("transaction")) {
            return TestContextFailureType.TRANSACTION_CONFIG_ERROR;
        }

        return TestContextFailureType.GENERIC_CONTEXT_ERROR;
    }

    private Map<String, Object> buildTestContextDetails(Throwable t) {
        Map<String, Object> context = buildContext(t);

        context.put("contextFailureType", categorizeContextFailure(t).name());
        context.put("springTestContext", true);

        if (t.getMessage() != null) {
            String message = t.getMessage();
            if (message.contains("bean")) {
                context.put("beanRelated", true);
                if (message.contains("'") && message.lastIndexOf("'") > message.indexOf("'")) {
                    String beanName = message.substring(
                            message.indexOf("'") + 1,
                            message.lastIndexOf("'")
                    );
                    context.put("missingBean", beanName);
                }
            }
        }

        return context;
    }

    private String getContextRemediation(TestContextFailureType failureType) {
        return switch (failureType) {
            case CONTEXT_LOAD_FAILURE -> "Check @SpringBootTest configuration, component scanning paths, " +
                    "and ensure all required beans are available in test context.";
            case BEAN_NOT_FOUND -> "Add missing bean to test configuration or use @MockBean/@SpyBean " +
                    "to provide test doubles.";
            case BEAN_CREATION_FAILURE -> "Review bean dependencies and configuration. Check for missing " +
                    "properties or circular dependencies.";
            case CIRCULAR_DEPENDENCY -> "Refactor bean dependencies to remove circular references. " +
                    "Consider using @Lazy annotation or constructor injection.";
            case TRANSACTION_CONFIG_ERROR -> "Review @Transactional configuration and transaction manager setup " +
                    "in test context.";
            case GENERIC_CONTEXT_ERROR -> "Review test context configuration and Spring Boot test setup.";
        };
    }

    @Getter
    private enum TestContextFailureType {
        CONTEXT_LOAD_FAILURE("Load Failure"),
        BEAN_NOT_FOUND("Bean Not Found"),
        BEAN_CREATION_FAILURE("Bean Creation Failure"),
        CIRCULAR_DEPENDENCY("Circular Dependency"),
        TRANSACTION_CONFIG_ERROR("Transaction Configuration Error"),
        GENERIC_CONTEXT_ERROR("Configuration Error");

        private final String displayName;

        TestContextFailureType(String displayName) {
            this.displayName = displayName;
        }

    }
}
