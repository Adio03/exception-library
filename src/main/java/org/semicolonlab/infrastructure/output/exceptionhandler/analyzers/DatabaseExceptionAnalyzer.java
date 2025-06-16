package org.semicolonlab.infrastructure.output.exceptionhandler.analyzers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.AbstractExceptionAnalyzer;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisResult;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class DatabaseExceptionAnalyzer extends AbstractExceptionAnalyzer {

    private static final Set<String> DB_EXCEPTIONS = Set.of(
            "org.springframework.dao.DataAccessException",
            "java.sql.SQLException",
            "javax.persistence.PersistenceException",
            "org.hibernate.HibernateException",
            "com.mongodb.MongoException",
            "org.springframework.jdbc.CannotGetJdbcConnectionException",
            "org.springframework.dao.DataIntegrityViolationException"
    );

    public DatabaseExceptionAnalyzer() {
        super(DB_EXCEPTIONS, 100);
        initializePatterns();
    }

    private void initializePatterns() {
        addMessagePattern("(?i)connection.*refused");
        addMessagePattern("(?i)timeout");
        addMessagePattern("(?i)duplicate.*key");
        addMessagePattern("(?i)table.*doesn't exist");
        addMessagePattern("(?i)access denied");
        addMessagePattern("(?i)unknown database");
        addMessagePattern("(?i)syntax error");
    }

    @Override
    public ExceptionAnalysisResult analyze(Throwable t) {
        String rootCause = extractRootCause(t);
        Map<String, Object> context = buildContext(t);

        DatabaseErrorType errorType = categorizeError(t);

        return ExceptionAnalysisResult.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode("DB_" + errorType.name())
                .title("Database " + errorType.getDisplayName())
                .message(rootCause)
                .rootCause(rootCause)
                .context(context)
                .tags(Set.of("database", errorType.name().toLowerCase()))
                .category("DATABASE")
                .remediation(getRemediation(errorType))
                .isRecoverable(errorType.isRecoverable())
                .build();
    }

    private DatabaseErrorType categorizeError(Throwable t) {
        String message = Optional.ofNullable(t.getMessage()).orElse("").toLowerCase();

        if (message.contains("connection") && (message.contains("refused") || message.contains("timeout"))) {
            return DatabaseErrorType.CONNECTION_FAILURE;
        } else if (message.contains("duplicate") || message.contains("constraint")) {
            return DatabaseErrorType.CONSTRAINT_VIOLATION;
        } else if (message.contains("access denied") || message.contains("authentication")) {
            return DatabaseErrorType.AUTHENTICATION_FAILURE;
        } else if (message.contains("table") && message.contains("exist")) {
            return DatabaseErrorType.SCHEMA_ERROR;
        } else if (message.contains("syntax")) {
            return DatabaseErrorType.QUERY_ERROR;
        }

        return DatabaseErrorType.GENERIC_ERROR;
    }

    private String getRemediation(DatabaseErrorType errorType) {
        return switch (errorType) {
            case CONNECTION_FAILURE -> "Check database server status, connection URL, and network connectivity";
            case AUTHENTICATION_FAILURE -> "Verify database credentials and user permissions";
            case CONSTRAINT_VIOLATION -> "Check data integrity constraints and unique key violations";
            case SCHEMA_ERROR -> "Verify database schema and table existence";
            case QUERY_ERROR -> "Review SQL syntax and query structure";
            case GENERIC_ERROR -> "Check database configuration and connectivity";
        };
    }

    @Getter
    private enum DatabaseErrorType {
        CONNECTION_FAILURE("Connection Failure", true),
        AUTHENTICATION_FAILURE("Authentication Failure", false),
        CONSTRAINT_VIOLATION("Data Constraint Violation", true),
        SCHEMA_ERROR("Schema Error", false),
        QUERY_ERROR("Query Error", true),
        GENERIC_ERROR("Database Error", false);

        private final String displayName;
        private final boolean recoverable;

        DatabaseErrorType(String displayName, boolean recoverable) {
            this.displayName = displayName;
            this.recoverable = recoverable;
        }

    }
}



