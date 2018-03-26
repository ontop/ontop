package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.StaticAtomicExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class StaticAtomicExpressionImpl implements StaticAtomicExpression {

    private final AtomPredicate predicate;

    private final List<VariableOrGroundTerm> terms;

    StaticAtomicExpressionImpl(AtomPredicate predicate, List<VariableOrGroundTerm> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    public StaticAtomicExpressionImpl(AtomPredicate predicate, ImmutableList<VariableOrGroundTerm> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    StaticAtomicExpressionImpl(AtomPredicate predicate, VariableOrGroundTerm... terms) {
        this.predicate = predicate;
        this.terms = Arrays.asList(terms);;
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public ImmutableList<? extends Term> getImmutableTerms() {
        return ImmutableList.copyOf(terms);
    }

    @Override
    public List<Term> getTerms() {
        return terms.stream().map(t -> (Term)t).collect(Collectors.toList());
    }

    @Override
    public ImmutableList<VariableOrGroundTerm> getVariableOrGroundTerms() {
        return ImmutableList.copyOf(terms);
    }

    @Override
    public ImmutableList<NonGroundTerm> extractVariables() {
        return terms.stream().filter(t -> t instanceof Variable).map(t -> (Variable)t).collect(ImmutableCollectors.toList());
    }

    @Override
    public String render() {
        return String.format("%s(%s)", predicate, terms.stream().map(Term::toString).collect(joining(",")));
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return Collections.<DatalogMTLExpression>emptyList();
    }

    @Override
    public ImmutableList<VariableOrGroundTerm> getAllVariableOrGroundTerms() {
        return ImmutableList.copyOf(terms);
    }
}
