package it.unibz.inf.ontop.answering.logging.impl;

import it.unibz.inf.ontop.answering.logging.QueryLogSink;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class StdoutQueryLogSink implements QueryLogSink {
    @Override
    public void submit(UUID queryId, String serializedQueryLogEntry) {
        System.out.println(serializedQueryLogEntry);
    }
}
