package org.semicolonlab.infrastructure.output.exceptionhandler.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionAnalysisEngine {
    private final List<ExceptionAnalysisStrategy> analyzers;

    public ExceptionAnalysisResult analyzeException(Throwable exceptionThrown) {
        log.debug("Analyzing exception >>>>>>>>>>>>>>>>>>>: {}", exceptionThrown.getClass().getName());
        return analyzers.stream()
                .filter(a -> a.canAnalyze(exceptionThrown))
                .min(Comparator.comparingInt(ExceptionAnalysisStrategy::getPriority))
                .map(a -> {
                    try { return a.analyze(exceptionThrown); }
                    catch (Exception e) {
                        log.warn("Analyzer {} failed", a.getClass().getSimpleName(), e);
                        return null;
                    }
                })
                .orElseGet(() -> fallback(exceptionThrown));
    }

    private ExceptionAnalysisResult fallback(Throwable t) {
        return ExceptionAnalysisResult.builder()
                .httpStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode("UNKNOWN_ERROR")
                .title(t.getClass().getSimpleName())
                .message(t.getMessage())
                .rootCause(t.getClass().getSimpleName())
                .context(Map.of("exceptionType", t.getClass().getName()))
                .tags(Set.of("unknown","fallback"))
                .category("UNKNOWN")
                .remediation("Contact system administrator")
                .isRecoverable(false)
                .build();
    }
}