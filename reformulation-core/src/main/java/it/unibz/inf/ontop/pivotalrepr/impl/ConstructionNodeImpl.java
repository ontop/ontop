package it.unibz.inf.ontop.pivotalrepr.impl;


import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibz.inf.ontop.pivotalrepr.*;

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
        this.substitution = new ImmutableSubstitutionImpl<>(ImmutableMap.<Variable, ImmutableTerm>of());
        this.optionalModifiers = Optional.empty();
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
    public ImmutableSet<Variable> getVariables() {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        for (VariableOrGroundTerm term : dataAtom.getArguments()) {
            if (term instanceof Variable)
                collectedVariableBuilder.add((Variable)term);
        }

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
    public SubstitutionResults<ConstructionNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToApply,
            QueryNode descendantNode, IntermediateQuery query) {

        ImmutableSubstitution<ImmutableTerm> localSubstitution = getSubstitution();
        ImmutableSet<Variable> boundVariables = localSubstitution.getImmutableMap().keySet();

        if (substitutionToApply.getImmutableMap().keySet().stream().anyMatch(boundVariables::contains)) {
            throw new IllegalArgumentException("An ascendent substitution MUST NOT include variables bound by" +
                    "the substitution of the current construction node");
        }

        ImmutableSubstitution<ImmutableTerm> compositeSubstitution = substitutionToApply.composeWith(localSubstitution);

        /**
         * Cleans the composite substitution by removing non-projected variables
         */
        ImmutableSet<Variable> projectedVariables = getProjectionAtom().getVariables();

        ImmutableMap.Builder<Variable, ImmutableTerm> newSubstitutionMapBuilder = ImmutableMap.builder();
        compositeSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> projectedVariables.contains(e.getKey()))
                .forEach(newSubstitutionMapBuilder::put);

        ImmutableSubstitutionImpl<ImmutableTerm> newSubstitution = new ImmutableSubstitutionImpl<>(
                newSubstitutionMapBuilder.build());

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(getProjectionAtom(),
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
    public SubstitutionResults<ConstructionNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution)
            throws QueryNodeSubstitutionException {
        DataAtom newProjectionAtom = substitution.applyToDataAtom(getProjectionAtom());

        try {
            /**
             * TODO: explain why it makes sense (interface)
             */
            SubQueryUnificationTools.ConstructionNodeUnification constructionNodeUnification =
                    SubQueryUnificationTools.unifyConstructionNode(this, newProjectionAtom);

            ConstructionNode newConstructionNode = constructionNodeUnification.getUnifiedNode();
            ImmutableSubstitution<VariableOrGroundTerm> newSubstitutionToPropagate =
                    constructionNodeUnification.getSubstitutionToPropagate();

            /**
             * If the substitution has changed, throws the new substitution
             * and the new construction node so that the "client" can continue
             * with the new substitution (for the children nodes).
             */
            if (!getSubstitution().equals(newSubstitutionToPropagate)) {
                return new SubstitutionResultsImpl<>(newConstructionNode, newSubstitutionToPropagate);
            }

            /**
             * Otherwise, continues with the current substitution
             */
            return new SubstitutionResultsImpl<>(newConstructionNode, substitution);

        } catch (SubQueryUnificationTools.SubQueryUnificationException e) {
            throw new QueryNodeSubstitutionException(e.getMessage());
        }
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
