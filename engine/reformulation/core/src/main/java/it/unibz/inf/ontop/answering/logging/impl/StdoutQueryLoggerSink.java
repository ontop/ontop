package it.unibz.inf.ontop.answering.logging.impl;

import it.unibz.inf.ontop.answering.logging.QueryLoggerSink;

import javax.inject.Singleton;

@Singleton
public class StdoutQueryLoggerSink implements QueryLoggerSink {
    @Override
    public void submit(String serializedQueryLogEntry) {
        System.out.println(serializedQueryLogEntry);
    }
}
