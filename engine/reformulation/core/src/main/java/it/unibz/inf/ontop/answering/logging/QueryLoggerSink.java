package it.unibz.inf.ontop.answering.logging;

@FunctionalInterface
public interface QueryLoggerSink {
    void submit(String serializedQueryLogEntry);
}
