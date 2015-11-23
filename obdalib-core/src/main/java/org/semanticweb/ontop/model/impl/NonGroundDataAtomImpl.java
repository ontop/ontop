package org.semanticweb.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.NonGroundDataAtom;
import org.semanticweb.ontop.model.NonGroundFunctionalTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

import static org.semanticweb.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

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
