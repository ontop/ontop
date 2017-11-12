package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.iq.executor.leftjoin.LeftJoinRightChildNormalizationAnalyzer.LeftJoinRightChildNormalizationAnalysis;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.iq.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.SUBSTITUTION_FACTORY;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IF_ELSE_NULL;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IS_NOT_NULL;


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
    private final TermFactory termFactory;

    @Inject
    private LeftToInnerJoinExecutor(LeftJoinRightChildNormalizationAnalyzer normalizer,
                                    IntermediateQueryFactory iqFactory,
                                    TermFactory termFactory) {
        this.normalizer = normalizer;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
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
         *
         * TODO: also support join (if it works with one "left child", that's fine)
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
                .map(ljCondition -> liftCondition(normalizedLeftJoin, leftChild, newRightChild, query, treeComponent,
                        variableGenerator))
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
                                       IntermediateQuery query, QueryTreeComponent treeComponent,
                                       VariableGenerator variableGenerator) {
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

        LeftJoinNode newLeftJoinNode = leftJoinNode.changeOptionalFilterCondition(Optional.empty());
        treeComponent.replaceNode(leftJoinNode, newLeftJoinNode);

        return rightVariablesToUpdate.isEmpty()
                ? newLeftJoinNode
                : updateConditionalVariables(rightVariablesToUpdate, rightChild, newLeftJoinNode, ljCondition,
                                             query, treeComponent, variableGenerator);
    }

    private LeftJoinNode updateConditionalVariables(ImmutableSet<Variable> rightVariablesToUpdate, DataNode rightChild,
                                                    LeftJoinNode newLeftJoinNode, ImmutableExpression ljCondition,
                                                    IntermediateQuery query, QueryTreeComponent treeComponent,
                                                    VariableGenerator variableGenerator) {
        ImmutableMap<Variable, Variable> newVariableMap = rightVariablesToUpdate.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        variableGenerator::generateNewVariableFromVar));
        /*
         * Update the right child
         */
        InjectiveVar2VarSubstitution localSubstitution = SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(newVariableMap);
        DataNode newRightChild = rightChild.newAtom(localSubstitution.applyToDataAtom(rightChild.getProjectionAtom()));
        treeComponent.replaceNode(rightChild, newRightChild);

        ImmutableExpression newCondition = localSubstitution.applyToBooleanExpression(ljCondition);

        ImmutableSubstitution<ImmutableFunctionalTerm> substitutionToPropagate = SUBSTITUTION_FACTORY.getSubstitution(
                newVariableMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> termFactory.getImmutableFunctionalTerm(IF_ELSE_NULL, newCondition, e.getValue())
                    )));

        SubstitutionPropagationProposal<LeftJoinNode> proposal = new SubstitutionPropagationProposalImpl<>(
                newLeftJoinNode, substitutionToPropagate);

        try {
            return query.applyProposal(proposal, true)
                    .getOptionalNewNode()
                    .orElseThrow(() -> new MinorOntopInternalBugException("Was not expected to modify the LJ node"));
        } catch (EmptyQueryException e) {
            throw new MinorOntopInternalBugException("This substitution propagation was not expected " +
                    "to make the query be empty");
        }
    }

    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightUnion(LeftJoinNode leftJoinNode,
                                                                             IntermediateQuery query,
                                                                             QueryTreeComponent treeComponent,
                                                                             DataNode leftDataNode, UnionNode rightChild) {
        // NOT YET IMPLEMENTED --> no optimization YET
        return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
    }
}
