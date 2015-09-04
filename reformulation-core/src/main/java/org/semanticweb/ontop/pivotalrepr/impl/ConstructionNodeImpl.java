package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);
    private static int CONVERGENCE_BOUND = 5;

    private final Optional<ImmutableQueryModifiers> optionalModifiers;
    private final DataAtom dataAtom;
    private final ImmutableSubstitution<ImmutableTerm> substitution;

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";

    public ConstructionNodeImpl(DataAtom dataAtom, ImmutableSubstitution<ImmutableTerm> substitution,
                                Optional<ImmutableQueryModifiers> optionalQueryModifiers) {
        this.dataAtom = dataAtom;
        this.substitution = substitution;
        this.optionalModifiers = optionalQueryModifiers;
    }

    /**
     * Without modifiers nor substitution.
     */
    public ConstructionNodeImpl(DataAtom dataAtom) {
        this.dataAtom = dataAtom;
        this.substitution = new ImmutableSubstitutionImpl<>(ImmutableMap.<VariableImpl, ImmutableTerm>of());
        this.optionalModifiers = Optional.absent();
    }

    @Override
    public DataAtom getProjectionAtom() {
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
        return new ConstructionNodeImpl(dataAtom, substitution, optionalModifiers);
    }

    @Override
    public ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getDirectBindingSubstitution() {
        if (substitution.isEmpty())
            return substitution;

        // Non-final
        ImmutableSubstitution<ImmutableTerm> previousSubstitution;
        // Non-final
        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitution;

        int i = 0;
        do {
            previousSubstitution = newSubstitution;
            newSubstitution = newSubstitution.composeWith(substitution);
            i++;
        } while ((i < CONVERGENCE_BOUND) && (!previousSubstitution.equals(newSubstitution)));

        if (i == CONVERGENCE_BOUND) {
            LOGGER.warn(substitution + " has not converged after " + CONVERGENCE_BOUND + " recursions over itself");
        }

        return newSubstitution;

    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + dataAtom + " " + "[" + substitution + "]" ;
    }

}
