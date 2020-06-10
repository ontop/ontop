package it.unibz.inf.ontop.answering.logging.impl;

import it.unibz.inf.ontop.answering.logging.QueryLogger;

import java.io.OutputStream;
import java.util.UUID;

public abstract class AbstractQueryLogger implements QueryLogger {
    private final UUID queryId;

    protected AbstractQueryLogger(UUID queryId, OutputStream outputStream) {
        this.queryId = queryId;
    }

    @Override
    public UUID getQueryId() {
        return queryId;
    }

    @Override
    public void serialize() {
        throw new RuntimeException("TODO: implement");
    }
}
