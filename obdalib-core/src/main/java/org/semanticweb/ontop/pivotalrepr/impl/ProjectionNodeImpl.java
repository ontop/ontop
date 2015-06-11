package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.pivotalrepr.*;

public class ProjectionNodeImpl extends QueryNodeImpl implements ProjectionNode {

    private ImmutableSubstitution substitution;
    private Optional<ImmutableQueryModifiers> optionalModifiers;
    private DataAtom dataAtom;

    public ProjectionNodeImpl(DataAtom dataAtom, ImmutableSubstitution substitution) {
        this.dataAtom = dataAtom;
        this.optionalModifiers = Optional.absent();
        this.substitution = substitution;
    }

    public ProjectionNodeImpl(DataAtom dataAtom, ImmutableSubstitution substitution,
                              ImmutableQueryModifiers queryModifiers) {
        this.dataAtom = dataAtom;
        this.optionalModifiers = Optional.of(queryModifiers);
        this.substitution = substitution;
    }

    @Override
    public DataAtom getHeadAtom() {
        return dataAtom;
    }

    @Override
    public ImmutableSubstitution getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getOptionalModifiers() {
        return optionalModifiers;
    }

    /**
     * Immutable fields, can be shared.
     */
    @Override
    public ProjectionNode clone() {
        if (optionalModifiers.isPresent()) {
            return new ProjectionNodeImpl(dataAtom, substitution, optionalModifiers.get());
        }
        return new ProjectionNodeImpl(dataAtom, substitution);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
