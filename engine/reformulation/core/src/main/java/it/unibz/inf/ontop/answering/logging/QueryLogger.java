package it.unibz.inf.ontop.answering.logging;

import java.util.UUID;

/**
 * Logs for a concrete query
 */
public interface QueryLogger {

    UUID getQueryId();

    void serialize();
}
