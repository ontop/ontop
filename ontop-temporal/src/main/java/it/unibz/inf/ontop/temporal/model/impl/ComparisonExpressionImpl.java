package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.model.ComparisonExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ComparisonExpressionImpl implements ComparisonExpression{

    AtomPredicate predicate;
    VariableOrGroundTerm term1;
    VariableOrGroundTerm term2;

    public ComparisonExpressionImpl(AtomPredicate predicate, VariableOrGroundTerm term1, VariableOrGroundTerm term2) {
        this.predicate = predicate;
        this.term1 = term1;
        this.term2 = term2;
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public List<? extends Term> getTerms() {
        return Arrays.asList(term1, term2);
    }

    @Override
    public String render() {
        return String.format("(%s %s %s)", term1, predicate, term2);
    }

    @Override
    public Iterable<? extends DatalogMTLExpression> getChildNodes() {
        return Collections.<DatalogMTLExpression>emptyList();
    }
}
