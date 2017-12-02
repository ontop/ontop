package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.StaticAtomicExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class StaticAtomicExpressionImpl implements StaticAtomicExpression {

    private final Predicate predicate;

    private final List<Term> terms;

    StaticAtomicExpressionImpl(Predicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    StaticAtomicExpressionImpl(Predicate predicate, Term... terms) {
        this.predicate = predicate;
        this.terms = Arrays.asList(terms);;
    }

    @Override
    public Predicate getPredicate() {
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
