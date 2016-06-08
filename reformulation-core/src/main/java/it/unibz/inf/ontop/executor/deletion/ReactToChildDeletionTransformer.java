package it.unibz.inf.ontop.executor.deletion;

import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.NodeTransformationProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.SubstitutionResultsImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.foldBooleanExpressions;
import static it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.computeNullSubstitution;
import static it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState.*;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.*;
import static it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools.extractProjectedVariables;

/**
 * TODO: explain
 */
public class ReactToChildDeletionTransformer implements HeterogeneousQueryNodeTransformer {

    private final IntermediateQuery query;
    private final Optional<ArgumentPosition> optionalPositionOfDeletedChild;
    private final ImmutableSet<Variable> variablesProjectedByDeletedChild;

    public ReactToChildDeletionTransformer(IntermediateQuery query,
                                           Optional<ArgumentPosition> optionalPositionOfDeletedChild,
                                           ImmutableSet<Variable> variablesProjectedByDeletedChild) {
        this.query = query;
        this.optionalPositionOfDeletedChild = optionalPositionOfDeletedChild;
        this.variablesProjectedByDeletedChild = variablesProjectedByDeletedChild;
    }

    @Override
    public NodeTransformationProposal transform(FilterNode filterNode) {
        return new NodeTransformationProposalImpl(DELETE, variablesProjectedByDeletedChild);
    }

    @Override
    public NodeTransformationProposal transform(ExtensionalDataNode extensionalDataNode){
        throw new UnsupportedOperationException("A TableNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal transform(LeftJoinNode leftJoinNode){
        ArgumentPosition positionOfDeletedChild = optionalPositionOfDeletedChild
                .orElseThrow(() -> new IllegalStateException("The deleted child of a LJ must have a position"));

        QueryNode otherChild = query.getChild(leftJoinNode, (positionOfDeletedChild == LEFT) ? RIGHT : LEFT)
                .orElseThrow(() -> new IllegalStateException("The other child of a LJ is missing"));

        ImmutableSet<Variable> variablesProjectedByOtherChild = extractProjectedVariables(query, otherChild);

        ImmutableSet<Variable> nullVariables;

        switch(positionOfDeletedChild) {
            case LEFT:
                nullVariables = union(variablesProjectedByOtherChild, variablesProjectedByDeletedChild);
                return new NodeTransformationProposalImpl(DELETE, nullVariables);

            case RIGHT:
                nullVariables = variablesProjectedByDeletedChild.stream()
                        .filter(v -> !(variablesProjectedByOtherChild.contains(v)))
                        .collect(ImmutableCollectors.toSet());
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_CHILD,
                        otherChild, nullVariables);
            default:
                throw new IllegalStateException("Unknown position: " + positionOfDeletedChild);
        }
    }

    @Override
    public NodeTransformationProposal transform(UnionNode unionNode) {
        ImmutableList<QueryNode> children = query.getChildren(unionNode);
        switch (children.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DELETE,
                        variablesProjectedByDeletedChild);
            case 1:
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_CHILD, children.get(0),
                        ImmutableSet.of());
            default:
                return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE,
                        ImmutableSet.of());
        }
    }

    @Override
    public NodeTransformationProposal transform(IntensionalDataNode intensionalDataNode) {
        throw new UnsupportedOperationException("A OrdinaryDataNode is not expected to have a child");
    }

    /**
     * TODO: update
     */
    @Override
    public NodeTransformationProposal transform(InnerJoinNode innerJoinNode) {
        ImmutableList<QueryNode> remainingChildren = query.getChildren(innerJoinNode);

        ImmutableSet<Variable> otherNodesProjectedVariables = extractProjectedVariables(query, innerJoinNode);

        /**
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(variablesProjectedByDeletedChild::contains)) {
            return rejectInnerJoin(otherNodesProjectedVariables);
        }

        Optional<ImmutableExpression> formerCondition = innerJoinNode.getOptionalFilterCondition();

        Optional<ExpressionEvaluator.Evaluation> optionalEvaluation = formerCondition
                .map(cond -> computeNullSubstitution(variablesProjectedByDeletedChild).applyToBooleanExpression(cond))
                .map(cond -> new ExpressionEvaluator(query.getMetadata().getUriTemplateMatcher())
                        .evaluateExpression(cond));

        /**
         * The new condition is not satisfied anymore
         */
        if (optionalEvaluation
                .filter(ExpressionEvaluator.Evaluation::isFalse)
                .isPresent()) {
            // Reject
            return rejectInnerJoin(otherNodesProjectedVariables);
        }
        /**
         * The condition still holds
         */
        else {
            Optional<ImmutableExpression> newCondition = optionalEvaluation
                    .flatMap(ExpressionEvaluator.Evaluation::getOptionalExpression);

            switch (remainingChildren.size()) {
                case 0:
                    return new NodeTransformationProposalImpl(DELETE,
                            variablesProjectedByDeletedChild);
                case 1:
                    if (newCondition.isPresent()) {
                        return new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                                new FilterNodeImpl(newCondition.get()),
                                variablesProjectedByDeletedChild);
                    } else {
                        return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_CHILD, remainingChildren.get(0),
                                variablesProjectedByDeletedChild);
                    }
                default:
                    if (newCondition.equals(formerCondition)) {
                        return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE, variablesProjectedByDeletedChild);
                    } else {
                        return new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                                new InnerJoinNodeImpl(newCondition),
                                variablesProjectedByDeletedChild);
                    }
            }
        }
    }

    private NodeTransformationProposal rejectInnerJoin(ImmutableSet<Variable> otherNodesProjectedVariables) {
        return new NodeTransformationProposalImpl(DELETE,
                union(otherNodesProjectedVariables, variablesProjectedByDeletedChild));
    }

    @Override
    public NodeTransformationProposal transform(ConstructionNode constructionNode) {
        /**
         * A construction node has only one child
         */
        return new NodeTransformationProposalImpl(DELETE, constructionNode.getProjectedVariables());
    }

    /**
     * TODO: implement
     */
    @Override
    public NodeTransformationProposal transform(GroupNode groupNode) {
        /**
         * A group node has only one child
         *
         * TODO: what is really projected by a group node?
         */
        return new NodeTransformationProposalImpl(DELETE, variablesProjectedByDeletedChild);
    }

    @Override
    public NodeTransformationProposal transform(EmptyNode emptyNode) {
        return new NodeTransformationProposalImpl(DELETE,
                emptyNode.getProjectedVariables());
    }

    private static ImmutableSet<Variable> union(ImmutableSet<Variable> set1, ImmutableSet<Variable> set2) {
        return Stream.concat(
                set1.stream(),
                set2.stream())
                .collect(ImmutableCollectors.toSet());
    }
}
