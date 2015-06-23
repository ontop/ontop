package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.IndempotentVar2VarSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

    private final Optional<ImmutableQueryModifiers> optionalModifiers;
    private final PureDataAtom dataAtom;
    private final ImmutableSubstitution<ImmutableTerm> substitution;

    /**
     * Without modifier
     */
    public ConstructionNodeImpl(PureDataAtom dataAtom, ImmutableSubstitution<ImmutableTerm> substitution) {
        this.dataAtom = dataAtom;
        this.substitution = substitution;
        this.optionalModifiers = Optional.absent();
    }

    public ConstructionNodeImpl(PureDataAtom dataAtom,
                                ImmutableSubstitution<ImmutableTerm> substitution,
                                ImmutableQueryModifiers queryModifiers) {
        this.dataAtom = dataAtom;
        this.substitution = substitution;
        this.optionalModifiers = Optional.of(queryModifiers);
    }

    public ConstructionNodeImpl(PureDataAtom dataAtom) {
        this.dataAtom = dataAtom;
        this.substitution = new ImmutableSubstitutionImpl<>(ImmutableMap.<VariableImpl, ImmutableTerm>of());
        this.optionalModifiers = Optional.absent();
    }

    @Override
    public PureDataAtom getProjectionAtom() {
        return dataAtom;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
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
    public ConstructionNode clone() {
        if (optionalModifiers.isPresent()) {
            return new ConstructionNodeImpl(dataAtom, substitution, optionalModifiers.get());
        }
        return new ConstructionNodeImpl(dataAtom, substitution);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }
}
