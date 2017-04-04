package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.executor.leftjoin.LeftJoinRightChildNormalizationAnalyzer.LeftJoinRightChildNormalizationAnalysis;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.VariableGenerator;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

import static it.unibz.inf.ontop.model.ExpressionOperation.IS_NOT_NULL;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Tries to transform the left join into a inner join node
 * TODO: describe them
 *
 * TODO: explicit the assumptions
 *
 */
@Singleton
public class LeftToInnerJoinExecutor implements SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    private final LeftJoinRightChildNormalizationAnalyzer normalizer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private LeftToInnerJoinExecutor(LeftJoinRightChildNormalizationAnalyzer normalizer,
                                    IntermediateQueryFactory iqFactory) {
        this.normalizer = normalizer;
        this.iqFactory = iqFactory;
    }

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode> apply(LeftJoinOptimizationProposal proposal,
                                                              IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        LeftJoinNode leftJoinNode = proposal.getFocusNode();

        QueryNode leftChild = query.getChild(leftJoinNode, LEFT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a left child"));

        QueryNode rightChild = query.getChild(leftJoinNode, RIGHT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a right child"));

        /*
         * No normalization (a DataNode is expected on the left)
         */
        if (!(leftChild instanceof DataNode))
            return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);

        DataNode leftDataNode = (DataNode) leftChild;

        if (rightChild instanceof DataNode) {
            return optimizeRightDataNode(leftJoinNode, query, treeComponent, leftDataNode, (DataNode) rightChild);
        } else if (rightChild instanceof UnionNode) {
            return optimizeRightUnion(leftJoinNode, query, treeComponent, leftDataNode, (UnionNode) rightChild);
        }
        /*
         * No normalization
         *
         * TODO: support more cases (like joins on the right)
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
        }
    }


    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightDataNode(LeftJoinNode leftJoinNode,
                                                                                IntermediateQuery query,
                                                                                QueryTreeComponent treeComponent,
                                                                                DataNode leftChild, DataNode rightChild) {

        VariableGenerator variableGenerator = new VariableGenerator(query.getKnownVariables());

        LeftJoinRightChildNormalizationAnalysis analysis = normalizer.analyze(leftChild, rightChild,
                query.getDBMetadata(), variableGenerator);

        if (!analysis.isMatchingAConstraint())
            // No normalization
            return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);

        Optional<ImmutableExpression> newLJCondition = leftJoinNode.getOptionalFilterCondition()
                .map(c1 -> analysis.getAdditionalExpression()
                        .map(c2 -> ImmutabilityTools.foldBooleanExpressions(c1, c2))
                        .orElseGet(() -> Optional.of(c1)))
                .orElseGet(analysis::getAdditionalExpression);

        LeftJoinNode normalizedLeftJoin = analysis.getProposedRightDataNode()
                .map(proposedRightDataNode -> normalize(leftJoinNode, rightChild,
                        newLJCondition.orElseThrow(() -> new MinorOntopInternalBugException(
                                "A condition was expected for a modified data node")),
                        proposedRightDataNode, treeComponent))
                .orElse(leftJoinNode);

        DataNode newRightChild = analysis.getProposedRightDataNode()
                .orElse(rightChild);

        LeftJoinNode leftJoinNodeToUpgrade = newLJCondition
                .map(ljCondition -> liftCondition(normalizedLeftJoin, leftChild, newRightChild, query, treeComponent))
                // NB: here the normalized LJ is expected to be the initial left join
                .orElse(normalizedLeftJoin);

        if (leftJoinNodeToUpgrade.getOptionalFilterCondition().isPresent())
            throw new MinorOntopInternalBugException("Bug: at this point the lj must not have a joining condition");

        /*
         * Replaces (upgrades) the left join by an inner join
         */
        InnerJoinNode innerJoinNode = iqFactory.createInnerJoinNode();
        treeComponent.replaceNode(leftJoinNodeToUpgrade, innerJoinNode);
        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(innerJoinNode));

    }

    private LeftJoinNode normalize(LeftJoinNode leftJoinNode, DataNode formerRightChild, ImmutableExpression newLJCondition,
                                   DataNode proposedRightDataNode, QueryTreeComponent treeComponent) {
        treeComponent.replaceNode(formerRightChild, proposedRightDataNode);
        LeftJoinNode newLeftJoinNode = leftJoinNode.changeOptionalFilterCondition(Optional.of(newLJCondition));
        treeComponent.replaceNode(leftJoinNode, newLeftJoinNode);

        return newLeftJoinNode;
    }

    private LeftJoinNode liftCondition(LeftJoinNode leftJoinNode, DataNode leftChild, DataNode rightChild,
                                       IntermediateQuery query, QueryTreeComponent treeComponent) {
        ImmutableExpression ljCondition = leftJoinNode.getOptionalFilterCondition()
                .orElseThrow(() -> new IllegalArgumentException("The LJ is expected to have a joining condition"));

        ImmutableSet<Variable> requiredVariablesAboveLJ = query.getVariablesRequiredByAncestors(leftJoinNode);
        ImmutableSet<Variable> leftVariables = query.getVariables(leftChild);
        ImmutableSet<Variable> requiredRightVariables = requiredVariablesAboveLJ.stream()
                .filter(v -> !leftVariables.contains(v))
                .collect(ImmutableCollectors.toSet());

        // Special case: ljCondition = IS_NOT_NULL(x) and x is a specific right variable
        // --> x will not be affected by the condition
        ImmutableSet<Variable> rightVariablesToUpdate = Optional.of(ljCondition)
                .filter(c -> c.getFunctionSymbol().equals(IS_NOT_NULL))
                .map(c -> c.getArguments().get(0))
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable) v)
                .map(specialVariable -> requiredRightVariables.stream()
                        .filter(v -> !v.equals(specialVariable))
                        .collect(ImmutableCollectors.toSet()))
                .orElse(requiredRightVariables);

        if (!rightVariablesToUpdate.isEmpty()) {
            throw new RuntimeException("TODO: support lifting condition to variables");
        }

        LeftJoinNode newLeftJoinNode = leftJoinNode.changeOptionalFilterCondition(Optional.empty());
        treeComponent.replaceNode(leftJoinNode, newLeftJoinNode);

        return newLeftJoinNode;
    }

    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightUnion(LeftJoinNode leftJoinNode,
                                                                             IntermediateQuery query,
                                                                             QueryTreeComponent treeComponent,
                                                                             DataNode leftDataNode, UnionNode rightChild) {
        throw new RuntimeException("TODO: support the normalization with a right union node");
    }
}
