package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.temporal.model.NormalizedRule;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalPredicate;

import java.util.ArrayList;
import java.util.List;

public class NormalizedRuleImpl implements NormalizedRule {
    @Override
    public TemporalExpression getHead() {
        return null;
    }

    @Override
    public TemporalExpression getBody() {
        return null;
    }
//
//    Predicate head;
//    ArrayList<Predicate> body;
//
//    public NormalizedRuleImpl(Predicate head, ArrayList<Predicate> body){
//        this.head = head;
//        this.body = body;
//    }
//
//    @Override
//    public TemporalPredicate getHead() {
//        return null;
//    }
//
//    @Override
//    public List<Predicate> getBody() {
//        return null;
//    }
}
