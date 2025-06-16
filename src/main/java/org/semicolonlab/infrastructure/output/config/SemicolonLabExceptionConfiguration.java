package org.semicolonlab.infrastructure.output.config;

import jakarta.annotation.PostConstruct;
import org.semicolonlab.infrastructure.output.exceptionhandler.UniversalExceptionHandler;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisEngine;
import org.semicolonlab.infrastructure.output.exceptionhandler.analysis.ExceptionAnalysisStrategy;
import org.semicolonlab.infrastructure.output.exceptionhandler.analyzers.DatabaseExceptionAnalyzer;
import org.semicolonlab.infrastructure.output.exceptionhandler.analyzers.TestContextExceptionAnalyzer;
import org.semicolonlab.infrastructure.output.exceptionhandler.analyzers.TestExceptionAnalyzer;
import org.semicolonlab.infrastructure.output.exceptionhandler.analyzers.UniversalStartupFailureAnalyzer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;


@Configuration
@ConditionalOnProperty(
        name = "exception.library.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnClass(FailureAnalyzer.class)
@EnableConfigurationProperties(ExceptionProperties.class)
public class SemicolonLabExceptionConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SemicolonLabExceptionConfiguration.class);
    @PostConstruct
    public void init() {
        log.info("SemicolonLabExceptionConfiguration loaded!=========================>>>>>>>>>>>>>");

    }
    @Bean
    @ConditionalOnMissingBean(UniversalStartupFailureAnalyzer.class)
    @ConditionalOnProperty(name = "exception.library.startup-analyzer.enabled", matchIfMissing = true)
    public UniversalStartupFailureAnalyzer universalStartupFailureAnalyzer(
            List<ExceptionAnalysisStrategy> strategies) {
        return new UniversalStartupFailureAnalyzer(strategies);
    }
    @Bean
    @ConditionalOnProperty(name = "exception.library.database-analyzer.enabled", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.dao.DataAccessException")
    public DatabaseExceptionAnalyzer databaseExceptionAnalyzer() {
        return new DatabaseExceptionAnalyzer();
    }

    @Bean
    @ConditionalOnProperty(name = "exception.library.test-analyzer.enabled", matchIfMissing = true)
    @ConditionalOnClass(name = "org.junit.jupiter.api.Test")
    public TestExceptionAnalyzer testExceptionAnalyzer() {
        return new TestExceptionAnalyzer();
    }

    @Bean
    @ConditionalOnProperty(name = "exception.library.test-context-analyzer.enabled", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.test.context.TestContext")
    public TestContextExceptionAnalyzer testContextExceptionAnalyzer() {
        return new TestContextExceptionAnalyzer();
    }


}
