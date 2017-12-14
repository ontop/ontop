package it.unibz.inf.ontop.temporal.model.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.TemporalAtomicExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class TemporalAtomicExpressionImpl implements TemporalAtomicExpression {

    TemporalAtomicExpressionImpl(AtomPredicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }
    public TemporalAtomicExpressionImpl(AtomPredicate predicate, ImmutableList<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    TemporalAtomicExpressionImpl(AtomPredicate predicate, Term... terms) {
        this.predicate = predicate;
        this.terms = Arrays.asList(terms);
    }

    private final AtomPredicate predicate;

    private final List<? extends Term> terms;

    @Override
    public String render() {
        return String.format("%s(%s)", predicate, terms.stream().map(Term::toString).collect(joining(",")));
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return Collections.<DatalogMTLExpression>emptyList();
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public List<? extends Term> getTerms() {
        return terms;
    }
}
