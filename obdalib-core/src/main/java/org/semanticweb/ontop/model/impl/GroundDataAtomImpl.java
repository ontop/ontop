package org.semanticweb.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.GroundFunctionalTerm;
import org.semanticweb.ontop.model.GroundTerm;

public class GroundDataAtomImpl extends DataAtomImpl implements GroundFunctionalTerm {

    protected GroundDataAtomImpl(AtomPredicate predicate, ImmutableList<GroundTerm> groundTerms) {
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
        return !getImmutableTerms().isEmpty();
    }

    @Override
    public ImmutableList<GroundTerm> getGroundTerms() {
        return (ImmutableList<GroundTerm>)(ImmutableList<?>)getImmutableTerms();
    }
}
