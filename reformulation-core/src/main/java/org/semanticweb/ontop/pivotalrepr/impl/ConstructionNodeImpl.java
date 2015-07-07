package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

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
    public ConstructionNode acceptNodeTransformer(QueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ConstructionNode newNodeWithAdditionalBindings(ImmutableSubstitution<ImmutableTerm> additionalBindings) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public ConstructionNode newNodeWithLessBindings(ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryNodeOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + dataAtom + " " + "[" +substitution + "]" ;
    }

}
