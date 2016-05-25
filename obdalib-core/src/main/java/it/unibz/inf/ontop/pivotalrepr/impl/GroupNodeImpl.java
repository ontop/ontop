package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.NonGroundFunctionalTermImpl;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.Optional;

import static it.unibz.inf.ontop.model.impl.GroundTermTools.isGroundTerm;

public class GroupNodeImpl extends QueryNodeImpl implements GroupNode {

    private static final String GROUP_NODE_STR = "GROUP BY";
    private final ImmutableList<NonGroundTerm> groupingTerms;

    public GroupNodeImpl(ImmutableList<NonGroundTerm> groupingTerms) {
        if (groupingTerms.isEmpty()) {
            throw new IllegalArgumentException("At least one group condition must be given");
        }
        this.groupingTerms = groupingTerms;
    }

    @Override
    public ImmutableList<NonGroundTerm> getGroupingTerms() {
        return groupingTerms;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public GroupNode clone() {
        return new GroupNodeImpl(groupingTerms);
    }

    @Override
    public GroupNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException,
            NotNeededNodeException {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<GroupNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return applyDescendingSubstitution(substitution);
    }

    @Override
    public SubstitutionResults<GroupNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        ImmutableList.Builder<NonGroundTerm> termBuilder = ImmutableList.builder();
        for (NonGroundTerm term : getGroupingTerms()) {

            ImmutableTerm newTerm = substitution.apply(term);
            if (newTerm instanceof Variable) {
                termBuilder.add((Variable)newTerm);
            }
            /**
             * Functional term: adds it if remains a non-ground term.
             */
            else if (!isGroundTerm(newTerm)) {
                NonGroundFunctionalTerm functionalTerm = new NonGroundFunctionalTermImpl(
                        (ImmutableFunctionalTerm)newTerm);
                termBuilder.add(functionalTerm);
            }
        }

        ImmutableList<NonGroundTerm> newGroupingTerms = termBuilder.build();
        if (newGroupingTerms.isEmpty()) {
            /**
             * The group node is not needed anymore because no grouping term remains
             */
            return new SubstitutionResultsImpl<>(substitution, Optional.empty());
        }

        GroupNode newNode = new GroupNodeImpl(newGroupingTerms);

        return new SubstitutionResultsImpl<>(newNode, substitution);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof GroupNode)
                && ((GroupNode) node).getGroupingTerms().equals(groupingTerms);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        for (NonGroundTerm term : groupingTerms) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm) term).getVariables());
            }
        }
        return collectedVariableBuilder.build();
    }

    @Override
    public String toString() {
        return GROUP_NODE_STR + " " + groupingTerms;
    }
}
