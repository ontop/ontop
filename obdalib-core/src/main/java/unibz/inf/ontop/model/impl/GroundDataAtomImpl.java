package unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.GroundDataAtom;
import unibz.inf.ontop.model.AtomPredicate;
import unibz.inf.ontop.model.GroundTerm;

public class GroundDataAtomImpl extends DataAtomImpl implements GroundDataAtom {

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
