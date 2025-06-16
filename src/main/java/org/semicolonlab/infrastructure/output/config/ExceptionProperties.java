package org.semicolonlab.infrastructure.output.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@ConfigurationProperties(prefix = "exception.library")
@Data
public class ExceptionProperties {
    private boolean enabled = true;
    private StartupAnalyzer startupAnalyzer = new StartupAnalyzer();
    private DatabaseAnalyzer databaseAnalyzer = new DatabaseAnalyzer();
    private TestAnalyzer testAnalyzer = new TestAnalyzer();
    private  TestContextAnalyzer testContextAnalyzer = new TestContextAnalyzer();
    @Data
    public static class StartupAnalyzer {
        private boolean enabled = true;
        private int priority = 1000;
    }

    @Data
    public static class DatabaseAnalyzer {
        private boolean enabled = true;
        private int priority = 100;
        private int timeoutThreshold = 30000;
    }
    @Data
    public static class TestAnalyzer {
        private boolean enabled = true;
        private int priority = 75;
        private boolean includeStackTrace = true;
    }

    @Data
    public static class TestContextAnalyzer {
        private boolean enabled = true;
        private int priority = 80;
        private boolean showContextDetails = true;
    }




}
