package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);

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
    public ConstructionNode newNodeWithAdditionalBindings(
            ImmutableSubstitution<ImmutableTerm> additionalBindingsSubstitution) throws InconsistentBindingException {
        ImmutableSet<Variable> projectedVariables = dataAtom.getVariables();

        /**
         * TODO: explain why the composition is too rich
         */
        ImmutableSubstitution<ImmutableTerm> composedSubstitution = additionalBindingsSubstitution.composeWith(substitution);
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();


        ImmutableMap<VariableImpl, ImmutableTerm> compositionMap = composedSubstitution.getImmutableMap();

        for(VariableImpl variable : compositionMap.keySet()) {
            ImmutableTerm term = compositionMap.get(variable);

            /**
             * If the variable is not projected, no need to be in the substitution
             */
            if (!projectedVariables.contains(variable)) {
                continue;
            }

            /**
             * Checks for contradictory bindings between
             * the previous one (still present in the composition)
             * and the additional ones.
             */
            if (additionalBindingsSubstitution.isDefining(variable)
                    && (!additionalBindingsSubstitution.get(variable).equals(term))) {
                throw new InconsistentBindingException("Contradictory bindings found");
            }

            substitutionMapBuilder.put(variable, term);
        }

        return new ConstructionNodeImpl(dataAtom, new ImmutableSubstitutionImpl<>(substitutionMapBuilder.build()),
                optionalModifiers);
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
        return CONSTRUCTION_NODE_STR + " " + dataAtom + " " + "[" + substitution + "]" ;
    }

}
