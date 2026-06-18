package it.unibz.inf.ontop.answering.logging;

import java.util.UUID;

@FunctionalInterface
public interface QueryLogSink {
    void submit(UUID queryId, String serializedQueryLogEntry);
}
