package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.*;

public class ProjectionNodeImpl extends QueryNodeImpl implements ProjectionNode {

    private Optional<ImmutableQueryModifiers> optionalModifiers;
    private DataAtom dataAtom;

    public ProjectionNodeImpl(DataAtom dataAtom) {
        this.dataAtom = dataAtom;
        this.optionalModifiers = Optional.absent();
    }

    public ProjectionNodeImpl(DataAtom dataAtom, ImmutableQueryModifiers queryModifiers) {
        this.dataAtom = dataAtom;
        this.optionalModifiers = Optional.of(queryModifiers);
    }

    @Override
    public DataAtom getHeadAtom() {
        return dataAtom;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getOptionalModifiers() {
        return optionalModifiers;
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
