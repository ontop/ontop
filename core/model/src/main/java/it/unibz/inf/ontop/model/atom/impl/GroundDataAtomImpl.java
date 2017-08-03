package it.unibz.inf.ontop.model.atom.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableDataAtom;
import it.unibz.inf.ontop.model.term.GroundTerm;

public class GroundDataAtomImpl extends AbstractDataAtomImpl implements DistinctVariableDataAtom {

    protected GroundDataAtomImpl(AtomPredicate predicate, ImmutableList<? extends GroundTerm> groundTerms) {
        super(predicate, groundTerms);
    }

    protected GroundDataAtomImpl(AtomPredicate predicate, GroundTerm... groundTerms) {
        super(predicate, groundTerms);
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public boolean containsGroundTerms() {
        return !getArguments().isEmpty();
    }

    @Override
    public ImmutableList<? extends GroundTerm> getArguments() {
        return (ImmutableList<? extends GroundTerm>)super.getArguments();
    }
}
