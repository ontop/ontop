package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.temporal.model.TemporalAtomicExpression;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class TemporalAtomicExpressionImpl implements TemporalAtomicExpression {

    TemporalAtomicExpressionImpl(Predicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    TemporalAtomicExpressionImpl(Predicate predicate, Term... terms) {
        this.predicate = predicate;
        this.terms = Arrays.asList(terms);
    }

    private final Predicate predicate;

    private final List<? extends Term> terms;

    @Override
    public String render() {
        return String.format("%s(%s)", predicate, terms.stream().map(Term::toString).collect(joining(",")));
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public List<? extends Term> getTerms() {
        return terms;
    }
}
