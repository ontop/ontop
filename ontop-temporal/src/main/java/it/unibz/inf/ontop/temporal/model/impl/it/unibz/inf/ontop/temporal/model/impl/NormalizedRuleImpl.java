package it.unibz.inf.ontop.temporal.model.impl.it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.NormalizedRule;
import it.unibz.inf.ontop.temporal.model.TemporalPredicate;

import java.util.ArrayList;
import java.util.List;

public class NormalizedRuleImpl implements NormalizedRule {

    TemporalPredicate head;
    ArrayList<TemporalPredicate> body;

    public NormalizedRuleImpl(TemporalPredicate head, ArrayList<TemporalPredicate> body){
        this.head = head;
        this.body = body;
    }

    @Override
    public TemporalPredicate getHead() {
        return null;
    }

    @Override
    public List<TemporalPredicate> getBody() {
        return null;
    }
}
