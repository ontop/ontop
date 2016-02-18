package unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.model.AtomPredicate;
import unibz.inf.ontop.model.NonGroundDataAtom;

import static unibz.inf.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

public class NonGroundDataAtomImpl extends DataAtomImpl implements NonGroundDataAtom {

    protected NonGroundDataAtomImpl(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundDataAtomImpl(AtomPredicate predicate, VariableOrGroundTerm... variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
