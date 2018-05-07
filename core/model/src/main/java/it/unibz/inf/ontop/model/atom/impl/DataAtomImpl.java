package it.unibz.inf.ontop.model.atom.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public class DataAtomImpl<P extends AtomPredicate> extends AbstractDataAtomImpl<P> {

    protected DataAtomImpl(P predicate, ImmutableList<? extends VariableOrGroundTerm> variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
    }

    protected DataAtomImpl(P predicate, VariableOrGroundTerm... variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
    }
}
