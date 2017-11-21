package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;

public class DatalogMTLRuleImpl implements DatalogMTLRule {
    DatalogMTLRuleImpl(DatalogMTLExpression head, DatalogMTLExpression body) {
        this.head = head;
        this.body = body;
    }

    private final DatalogMTLExpression head;
    private final DatalogMTLExpression body;

    @Override
    public DatalogMTLExpression getHead() {
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
