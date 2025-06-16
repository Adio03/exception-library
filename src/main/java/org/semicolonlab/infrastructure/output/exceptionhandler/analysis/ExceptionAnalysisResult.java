package org.semicolonlab.infrastructure.output.exceptionhandler.analysis;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;
@Data
@Builder
public class ExceptionAnalysisResult {
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String title;
    private final String message;
    private final String rootCause;
    private final Map<String, Object> context;
    private final Set<String> tags;
    private final String category;
    private final String remediation;
    private final boolean isRecoverable;
}
