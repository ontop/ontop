package it.unibz.inf.ontop.answering.logging.impl;

import it.unibz.inf.ontop.answering.logging.QueryLogSink;

import javax.inject.Singleton;

@Singleton
public class StdoutQueryLogSink implements QueryLogSink {
    @Override
    public void submit(String serializedQueryLogEntry) {
        System.out.println(serializedQueryLogEntry);
    }
}
