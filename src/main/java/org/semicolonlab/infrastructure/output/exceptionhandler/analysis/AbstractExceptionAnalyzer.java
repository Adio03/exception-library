package org.semicolonlab.infrastructure.output.exceptionhandler.analysis;

import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractExceptionAnalyzer implements ExceptionAnalysisStrategy {
        protected final Set<String> supportedExceptions;
        protected final Set<Pattern> messagePatterns = new HashSet<>();
        protected final int priority;

        protected AbstractExceptionAnalyzer(Set<String> supportedExceptions, int priority) {
            this.supportedExceptions = supportedExceptions;
            this.priority = priority;
        }

        protected void addMessagePattern(String regex) {
            messagePatterns.add(Pattern.compile(regex));
        }

        @Override
        public boolean canAnalyze(Throwable throwable) {
            return isInExceptionHierarchy(throwable) || matchesMessagePattern(throwable);
        }

        private boolean isInExceptionHierarchy(Throwable throwable) {
            while (throwable != null) {
                if (supportedExceptions.contains(throwable.getClass().getName())) return true;
                throwable = throwable.getCause();
            }
            return false;
        }

        private boolean matchesMessagePattern(Throwable throwable) {
            String message = Optional.ofNullable(throwable.getMessage()).orElse("");
            return messagePatterns.stream().anyMatch(messagePattern -> messagePattern.matcher(message).find());
        }

        protected String extractRootCause(Throwable throwable) {
            Throwable rootCause = throwable;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            return rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage();
        }

        protected Map<String, Object> buildContext(Throwable throwable) {
            Map<String, Object> exceptionResponseLog = new LinkedHashMap<>();
            exceptionResponseLog.put("exceptionType", throwable.getClass().getName());

            List<String> causeChain = new ArrayList<>();
            Throwable currentExceptionThrown = throwable;
            while (currentExceptionThrown != null && causeChain.size() < 5) {
                causeChain.add(currentExceptionThrown.getClass().getSimpleName());
                currentExceptionThrown = currentExceptionThrown.getCause();
            }
            exceptionResponseLog.put("causeChain", causeChain);

            return exceptionResponseLog;
        }

        @Override
        public int getPriority() {
            return priority;
        }

}
