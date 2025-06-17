package org.semicolonlab.infrastructure.output.exceptionhandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semicolonlab.domain.exceptions.SemicolonLabException;
import org.semicolonlab.domain.message.ErrorResponse;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisEngine;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@Slf4j
class UniversalExceptionHandlerTest {
    private UniversalExceptionHandler exceptionHandler;
    private ExceptionAnalysisEngine mockAnalysisEngine;
    private Environment mockEnvironment;
    private WebRequest mockWebRequest;
    private ServletWebRequest mockServletWebRequest;
    private HttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() {
        mockAnalysisEngine = mock(ExceptionAnalysisEngine.class);
        mockEnvironment = mock(Environment.class);
        mockWebRequest = mock(WebRequest.class);
        mockServletWebRequest = mock(ServletWebRequest.class);
        mockHttpServletRequest = mock(HttpServletRequest.class);
        exceptionHandler = new UniversalExceptionHandler(mockAnalysisEngine, mockEnvironment);
    }

    @Test
    @DisplayName("Should handle SemicolonLabException with all properties correctly")
    void handleSemicolonLabExceptionTest() {
        SemicolonLabException exception = new SemicolonLabException(
                "User already exists",
                HttpStatus.CONFLICT,
                "signup_error"
        );
        when(mockWebRequest.getDescription(false)).thenReturn("uri=/api/users");
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        ResponseEntity<ErrorResponse> result = exceptionHandler.handleDomain(exception, mockWebRequest);
        log.info(result.toString());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getBody()).isNotNull();
        ErrorResponse errorResponse = result.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(409);
        assertThat(errorResponse.getTitle()).isEqualTo("signup_error");
        assertThat(errorResponse.getInstance()).isEqualTo("/api/users");
    }



}