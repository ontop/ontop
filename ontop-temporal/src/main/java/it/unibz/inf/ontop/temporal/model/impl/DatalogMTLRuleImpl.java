package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;

public class DatalogMTLRuleImpl implements DatalogMTLRule {
    DatalogMTLRuleImpl(TemporalExpression head, TemporalExpression body) {
        this.head = head;
        this.body = body;
    }

    private final TemporalExpression head;
    private final TemporalExpression body;

    @Override
    public TemporalExpression getHead() {
        return head;
    }

    @Override
    public TemporalExpression getBody() {
        return body;
    }

    @Override
    public String render() {
        return String.format("%s :- %s .", head.render(), body.render());
    }
}
