package org.semicolonlab.infrastructure.output.exceptionhandler.analyzers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.semicolonlab.infrastructure.output.config.ExceptionProperties;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisResult;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisStrategy;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UniversalStartupFailureAnalyzer  extends AbstractFailureAnalyzer<Throwable> {

    private final List<ExceptionAnalysisStrategy> analyzers;
    private final ExceptionProperties properties;

    public UniversalStartupFailureAnalyzer(List<ExceptionAnalysisStrategy> strategies) {
        this.analyzers = strategies.stream()
                .sorted(Comparator.comparingInt(ExceptionAnalysisStrategy::getPriority).reversed())
                .collect(Collectors.toList());
        this.properties = null;
        log.info("UniversalStartupFailureAnalyzer initialized with {} analyzers", analyzers.size());
    }

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, Throwable cause) {
        try {
            ExceptionAnalysisResult result = analyzeException(cause);
            log.error("🚨 Application startup failed - {}: {}", result.getErrorCode(), result.getRootCause());

            return new FailureAnalysis(
                    formatDescription(result),
                    formatAction(result),
                    cause
            );
        } catch (Exception e) {
            log.error("Error during exception analysis", e);
            return fallbackAnalysis(cause);
        }
    }

    private ExceptionAnalysisResult analyzeException(Throwable throwable) {
        return analyzers.stream()
                .filter(analyzer -> analyzer.canAnalyze(throwable))
                .findFirst()
                .map(analyzer -> {
                    log.debug("Using analyzer: {}", analyzer.getClass().getSimpleName());
                    return analyzer.analyze(throwable);
                })
                .orElseGet(() -> defaultAnalysis(throwable));
    }

    private String formatDescription(ExceptionAnalysisResult result) {
        StringBuilder description = new StringBuilder();
        description.append("🔴 ").append(result.getTitle()).append("\n\n");
        description.append("📋 Error Code: ").append(result.getErrorCode()).append("\n");
        description.append("📂 Category: ").append(result.getCategory()).append("\n");
        description.append("🔍 Root Cause: ").append(result.getRootCause()).append("\n");

        if (result.getContext()  != null && !result.getContext().isEmpty()) {
            description.append("\n📊 Context:\n");
            result.getContext().forEach((key, value) ->
                    description.append("  • ").append(key).append(": ").append(value).append("\n"));
        }

        return description.toString();
    }

    private String formatAction(ExceptionAnalysisResult result) {
        StringBuilder action = new StringBuilder();
        action.append("🛠️ Recommended Actions:\n\n");
        action.append("1. ").append(result.getRemediation()).append("\n");

        if (result.isRecoverable()) {
            action.append("2. This issue is potentially recoverable - retry after fixing\n");
        } else {
            action.append("2. This is a critical issue requiring immediate attention\n");
        }

        if (result.getTags() != null && !result.getTags().isEmpty()) {
            action.append("3. Related tags: ").append(String.join(", ", result.getTags())).append("\n");
        }

        return action.toString();
    }

    private FailureAnalysis fallbackAnalysis(Throwable cause) {
        return new FailureAnalysis(
                "🚨 Unexpected Startup Failure\n\nAn unexpected error occurred during application startup.",
                "🛠️ Check the full stack trace and application configuration.",
                cause
        );
    }

    private ExceptionAnalysisResult defaultAnalysis(Throwable ex) {
        return ExceptionAnalysisResult.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode("UNKNOWN_STARTUP_FAILURE")
                .title("Unknown Startup Failure")
                .message(ex.getMessage())
                .rootCause(extractRootCause(ex))
                .category("STARTUP")
                .remediation("Check application logs, configuration, and dependencies")
                .isRecoverable(false)
                .tags(Set.of("startup", "unknown"))
                .build();
    }

    private String extractRootCause(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " +
                Optional.ofNullable(root.getMessage()).orElse("No message available");
    }
}



