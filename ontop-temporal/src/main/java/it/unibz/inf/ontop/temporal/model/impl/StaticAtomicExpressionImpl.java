package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.StaticAtomicExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class StaticAtomicExpressionImpl implements StaticAtomicExpression {

    private final AtomPredicate predicate;

    private final List<Term> terms;

    StaticAtomicExpressionImpl(AtomPredicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    public StaticAtomicExpressionImpl(AtomPredicate predicate, ImmutableList<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    StaticAtomicExpressionImpl(AtomPredicate predicate, Term... terms) {
        this.predicate = predicate;
        this.terms = Arrays.asList(terms);;
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public List<? extends Term> getTerms() {
        return terms;
    }

    @Override
    public String render() {
        return String.format("%s(%s)", predicate, terms.stream().map(Term::toString).collect(joining(",")));
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return Collections.<DatalogMTLExpression>emptyList();
    }
}
