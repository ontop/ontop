package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.NonGroundFunctionalTermImpl;
import org.semanticweb.ontop.pivotalrepr.*;

import static org.semanticweb.ontop.model.impl.GroundTermTools.isGroundTerm;

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
    public SubstitutionResults<GroupNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return applyDescendentSubstitution(substitution);
    }

    @Override
    public SubstitutionResults<GroupNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
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
            return new SubstitutionResultsImpl<>(substitution);
        }

        GroupNode newNode = new GroupNodeImpl(newGroupingTerms);

        return new SubstitutionResultsImpl<>(newNode, substitution);
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
