package it.unibz.inf.ontop.pivotalrepr.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.NO_CHANGE;

public class UnionNodeImpl extends QueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";
    private final ImmutableSet<Variable> projectedVariables;

    public UnionNodeImpl(ImmutableSet<Variable> projectedVariables) {
        this.projectedVariables = projectedVariables;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public UnionNode clone() {
        return new UnionNodeImpl(projectedVariables);
    }

    @Override
    public UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    /**
     * Blocks an ascending substitution by inserting a construction node.
     */
    @Override
    public SubstitutionResults<UnionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        if (substitution.isEmpty()) {
            return new SubstitutionResultsImpl<>(NO_CHANGE);
        }
        /**
         * Asks for inserting a construction node between the descendant node and this node.
         * Such a construction node will contain the substitution.
         */
        else {
            ConstructionNode newParentOfDescendantNode = new ConstructionNodeImpl(projectedVariables,
                    (ImmutableSubstitution<ImmutableTerm>) substitution, Optional.empty());
            return new SubstitutionResultsImpl<>(this, newParentOfDescendantNode, descendantNode);
        }
    }

    @Override
    public SubstitutionResults<UnionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        ImmutableSet<Variable> newProjectedVariables = ConstructionNodeTools.computeNewProjectedVariables(substitution,
                projectedVariables);

        /**
         * Stops the substitution if does not affect the projected variables
         */
        if (newProjectedVariables.equals(projectedVariables)) {
            return new SubstitutionResultsImpl<>(NO_CHANGE);
        }
        /**
         * Otherwise, updates the projected variables and propagates the substitution down.
         */
        else {
            UnionNode newNode = new UnionNodeImpl(newProjectedVariables);
            return new SubstitutionResultsImpl<>(newNode, substitution);
        }
    }

    @Override
    public ImmutableSet<Variable> getProjectedVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof UnionNode);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.of();
    }

    @Override
    public String toString() {
        return UNION_NODE_STR + " " + projectedVariables;
    }
}
