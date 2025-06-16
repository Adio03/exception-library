package org.semicolonlab.infrastructure.output.exceptionhandler.analysis;

public interface ExceptionAnalysisStrategy {
    boolean canAnalyze(Throwable throwable);
    ExceptionAnalysisResult analyze(Throwable throwable);
    int getPriority();
}
