package org.semicolonlab.infrastructure.output.exceptionhandler;

import lombok.RequiredArgsConstructor;

import org.semicolonlab.domain.exceptions.SemicolonLabException;
import org.semicolonlab.domain.message.ErrorResponse;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisEngine;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class UniversalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UniversalExceptionHandler.class);
    private final ExceptionAnalysisEngine analysisEngine;
    private final Environment environment;

    private static final Set<String> DEVELOPMENT_PROFILES = Set.of("dev", "local", "test", "development");

    @ExceptionHandler(SemicolonLabException.class)
    public ResponseEntity<ErrorResponse> handleDomain(SemicolonLabException ex, WebRequest req) {
        return buildResponse(ex.getStatus(), ex.getErrorCode(), ex.getTitle(), ex.getMessage(), ex.getContext(), req, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex, WebRequest req) {
        log.error("Unhandled exception>>>>>>>>>>>>>>: {}", ex.getMessage(), ex);
        ExceptionAnalysisResult res = analysisEngine.analyzeException(ex);
        return buildResponse(res.getHttpStatus(), res.getErrorCode(), res.getTitle(), res.getMessage(), res.getContext(), req, ex);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest req) {
        ExceptionAnalysisResult res = analysisEngine.analyzeException(ex);
        ResponseEntity<ErrorResponse> resp = buildResponse(res.getHttpStatus(), res.getErrorCode(), res.getTitle(), res.getMessage(), res.getContext(), req, ex);
        return new ResponseEntity<>(resp.getBody(), headers, statusCode);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String title, String detail, Map<String, Object> exceptionContext, WebRequest webRequest, Exception exception) {
        HttpStatus httpStatus = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, Object> context = new LinkedHashMap<>(Optional.ofNullable(exceptionContext).orElse(Collections.emptyMap()));
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(DEVELOPMENT_PROFILES::contains)) {
            context.put("debug", Map.of(
                    "exceptionType", exception.getClass().getName(),
                    "stackTrace", Arrays.stream(exception.getStackTrace()).limit(5).map(Object::toString).collect(Collectors.joining(" -> "))
            ));
        }
        ErrorResponse er = ErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(httpStatus.value())
                .code(code)
                .title(title)
                .detail(detail)
                .instance(extractPath(webRequest))
                .debugInformation(context.isEmpty() ? null : context)
                .build();
        return er.toResponseEntity(httpStatus);
    }

    private String extractPath(WebRequest req) {
        if (req instanceof ServletWebRequest sw) return sw.getRequest().getRequestURI();
        return req.getDescription(false).replace("uri=", "");
    }
}