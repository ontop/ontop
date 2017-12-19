package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
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
    VariableOrGroundTerm leftTerm;
    VariableOrGroundTerm rightTerm;

    public ComparisonExpressionImpl(AtomPredicate predicate, VariableOrGroundTerm term1, VariableOrGroundTerm term2) {
        this.predicate = predicate;
        this.leftTerm = term1;
        this.rightTerm = term2;
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public ImmutableList<? extends Term> getImmutableTerms() {
        return ImmutableList.copyOf(Arrays.asList(leftTerm, rightTerm));
    }

    @Override
    public List<? extends Term> getTerms() {
        return Arrays.asList(leftTerm, rightTerm);
    }

    @Override
    public String render() {
        return String.format("(%s %s %s)", leftTerm, predicate, rightTerm);
    }

    @Override
    public Iterable<? extends DatalogMTLExpression> getChildNodes() {
        return Collections.<DatalogMTLExpression>emptyList();
    }

    @Override
    public VariableOrGroundTerm getLeftOperand() {
        return this.leftTerm;
    }

    @Override
    public VariableOrGroundTerm getRightOperand() {
        return this.rightTerm;
    }
}

