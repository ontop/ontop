package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.AtomicExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import org.mapdb.Atomic;

public class DatalogMTLRuleImpl implements DatalogMTLRule {
    DatalogMTLRuleImpl(AtomicExpression head, DatalogMTLExpression body) {
        this.head = head;
        this.body = body;
    }

    private final AtomicExpression head;
    private final DatalogMTLExpression body;

    @Override
    public AtomicExpression getHead() {
        return head;
    }

    @Override
    public DatalogMTLExpression getBody() {
        return body;
    }

    @Override
    public String render() {
        return String.format("%s :- %s .", head.render(), body.render());
    }
}
