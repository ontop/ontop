package it.unibz.inf.ontop.temporal.model.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.TemporalAtomicExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class TemporalAtomicExpressionImpl implements TemporalAtomicExpression {

    private final AtomPredicate predicate;

    private final List<VariableOrGroundTerm> terms;


    TemporalAtomicExpressionImpl(AtomPredicate predicate, List<VariableOrGroundTerm> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }
    public TemporalAtomicExpressionImpl(AtomPredicate predicate, ImmutableList<VariableOrGroundTerm> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    TemporalAtomicExpressionImpl(AtomPredicate predicate, VariableOrGroundTerm... terms) {
        this.predicate = predicate;
        this.terms = Arrays.asList(terms);
    }

    @Override
    public ImmutableList<NonGroundTerm> extractVariables() {
        return terms.stream().filter(t -> t instanceof Variable).map(t -> (Variable)t).collect(ImmutableCollectors.toList());
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return Collections.<DatalogMTLExpression>emptyList();
    }

    @Override
    public ImmutableList<VariableOrGroundTerm> getAllVariableOrGroundTerms() {
        return ImmutableList.copyOf(terms);
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
    public String toString() {
        String subject = "";
        if(terms.get(0) instanceof Variable){
            subject = "?"+terms.get(0);
        } else {
            subject = terms.get(0).toString();
        }
        if(terms.size()==2) {
            if (terms.get(1) instanceof Variable) {
                return String.format("%s %s ?%s .", subject, predicate, terms.get(1));
            } else {
                return String.format("%s %s %s .", subject, predicate, terms.get(1));
            }
        } else if(terms.size()==1){
            return String.format("%s %s %s .", subject, RDF.TYPE.getIRIString(), predicate);
        }
        return String.format("%s(%s)", predicate, terms.stream().map(Term::toString).collect(joining(",")));
    }
}
