package it.unibz.inf.ontop.model.atom.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.areGroundTerms;

public class DataAtomImpl extends AbstractDataAtomImpl {

    protected DataAtomImpl(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
    }

    protected DataAtomImpl(AtomPredicate predicate, VariableOrGroundTerm... variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
    }

    @Override
    public boolean isGround() {
        return areGroundTerms(getArguments());
    }
}
