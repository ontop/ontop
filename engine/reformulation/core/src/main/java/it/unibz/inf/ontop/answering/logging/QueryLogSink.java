package it.unibz.inf.ontop.answering.logging;

@FunctionalInterface
public interface QueryLogSink {
    void submit(String serializedQueryLogEntry);
}
