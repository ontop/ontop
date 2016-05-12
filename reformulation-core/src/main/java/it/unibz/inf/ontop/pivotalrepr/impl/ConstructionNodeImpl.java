package it.unibz.inf.ontop.pivotalrepr.impl;


import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.SubQueryUnificationTools.NewSubstitutionPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.pivotalrepr.impl.SubQueryUnificationTools.computeNewProjectedVariables;
import static it.unibz.inf.ontop.pivotalrepr.impl.SubQueryUnificationTools.traverseConstructionNode;
import static it.unibz.inf.ontop.pivotalrepr.impl.SubQueryUnificationTools.updateOptionalModifiers;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);
    private static int CONVERGENCE_BOUND = 5;

    private final Optional<ImmutableQueryModifiers> optionalModifiers;
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSubstitution<ImmutableTerm> substitution;

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";

    public ConstructionNodeImpl(ImmutableSet<Variable> projectedVariables, ImmutableSubstitution<ImmutableTerm> substitution,
                                Optional<ImmutableQueryModifiers> optionalQueryModifiers) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.optionalModifiers = optionalQueryModifiers;
    }

    /**
     * Without modifiers nor substitution.
     */
    public ConstructionNodeImpl(ImmutableSet<Variable> projectedVariables) {
        this.projectedVariables = projectedVariables;
        this.substitution = new ImmutableSubstitutionImpl<>(ImmutableMap.<Variable, ImmutableTerm>of());
        this.optionalModifiers = Optional.empty();
    }

    @Override
    public ImmutableSet<Variable> getProjectedVariables() {
        return projectedVariables;
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
        return new ConstructionNodeImpl(projectedVariables, substitution, optionalModifiers);
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
    public ImmutableSet<Variable> getVariables() {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        collectedVariableBuilder.addAll(projectedVariables);

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm)term).getVariables());
            }
        }

        return collectedVariableBuilder.build();
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

    /**
     * Creates a new ConstructionNode with a new substitution.
     * This substitution is obtained by composition and then cleaned (only defines the projected variables)
     *
     * Stops the propagation.
     */
    @Override
    public SubstitutionResults<ConstructionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitutionToApply,
            QueryNode descendantNode, IntermediateQuery query) {

        ImmutableSubstitution<ImmutableTerm> localSubstitution = getSubstitution();
        ImmutableSet<Variable> boundVariables = localSubstitution.getImmutableMap().keySet();

        if (substitutionToApply.getImmutableMap().keySet().stream().anyMatch(boundVariables::contains)) {
            throw new IllegalArgumentException("An ascending substitution MUST NOT include variables bound by" +
                    "the substitution of the current construction node");
        }

        ImmutableSubstitution<ImmutableTerm> compositeSubstitution = substitutionToApply.composeWith(localSubstitution);

        /**
         * Cleans the composite substitution by removing non-projected variables
         */

        ImmutableMap.Builder<Variable, ImmutableTerm> newSubstitutionMapBuilder = ImmutableMap.builder();
        compositeSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> projectedVariables.contains(e.getKey()))
                .forEach(newSubstitutionMapBuilder::put);

        ImmutableSubstitutionImpl<ImmutableTerm> newSubstitution = new ImmutableSubstitutionImpl<>(
                newSubstitutionMapBuilder.build());

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectedVariables,
                newSubstitution, getOptionalModifiers());

        /**
         * Stops to propagate the substitution
         */
        return new SubstitutionResultsImpl<>(newConstructionNode);
    }

    /**
     * TODO: explain
     */
    @Override
    public SubstitutionResults<ConstructionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution)
            throws QueryNodeSubstitutionException {

        ImmutableSet<Variable> newProjectedVariables = computeNewProjectedVariables(descendingSubstitution,
                getProjectedVariables());

        try {
            NewSubstitutionPair newSubstitutions = traverseConstructionNode(descendingSubstitution, substitution,
                    projectedVariables);

            ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate = newSubstitutions.propagatedSubstitution;

            Optional<ImmutableQueryModifiers> newOptionalModifiers = updateOptionalModifiers(optionalModifiers,
                    descendingSubstitution, substitutionToPropagate);

            ConstructionNode newConstructionNode = new ConstructionNodeImpl(newProjectedVariables,
                    newSubstitutions.bindings, newOptionalModifiers);

            return new SubstitutionResultsImpl<>(newConstructionNode, substitutionToPropagate);


        } catch (SubQueryUnificationTools.UnificationException e) {
            throw new QueryNodeSubstitutionException("The descending substitution " + descendingSubstitution
                    + " is incompatible with " + this);
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ConstructionNode)
                && ((ConstructionNode) node).getProjectedVariables().equals(projectedVariables);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + projectedVariables + " " + "[" + substitution + "]" ;
    }

}
