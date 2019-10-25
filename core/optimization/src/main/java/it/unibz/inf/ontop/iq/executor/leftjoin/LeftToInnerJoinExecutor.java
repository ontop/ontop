package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.ImmutableList;
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
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.*;

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
    private final SubstitutionFactory substitutionFactory;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final IQConverter iqConverter;

    @Inject
    private LeftToInnerJoinExecutor(LeftJoinRightChildNormalizationAnalyzer normalizer,
                                    IntermediateQueryFactory iqFactory,
                                    TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                    DBFunctionSymbolFactory dbFunctionSymbolFactory,
                                    CoreUtilsFactory coreUtilsFactory, IQConverter iqConverter) {
        this.normalizer = normalizer;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.iqConverter = iqConverter;
    }

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode> apply(LeftJoinOptimizationProposal proposal,
                                                              IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        LeftJoinNode leftJoinNode = proposal.getFocusNode();

        QueryNode leftChild = query.getChild(leftJoinNode, LEFT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a left child"));

        QueryNode rightChild = query.getChild(leftJoinNode, RIGHT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a right child"));

        /*
         * Only when the left can be reduced to set of joined data nodes
         */
        Optional<ImmutableList<ExtensionalDataNode>> optionalLeftDataNodes = extractLeftDataNodes(query, leftChild);
        if (optionalLeftDataNodes.isPresent()) {
            ImmutableList<ExtensionalDataNode> leftDataNodes = optionalLeftDataNodes.get();

            if (rightChild instanceof ExtensionalDataNode) {
                return optimizeRightDataNode(leftJoinNode, query, treeComponent, leftChild, leftDataNodes,
                        DataNodeAndSubstitution.extract((ExtensionalDataNode) rightChild));
            } else if (rightChild instanceof ConstructionNode) {

                return DataNodeAndSubstitution.extract((ConstructionNode) rightChild, query)
                        .map(n -> optimizeRightDataNode(leftJoinNode, query, treeComponent, leftChild, leftDataNodes, n))
                        .orElseGet(() -> new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode));
            }
            else if (rightChild instanceof UnionNode) {
                return optimizeRightUnion(leftJoinNode, query, treeComponent, leftChild, leftDataNodes, (UnionNode) rightChild);
            }
        }
        /*
         * No normalization
         *
         * TODO: support more cases (like joins on the right)
         */
        return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
    }

    private Optional<ImmutableList<ExtensionalDataNode>> extractLeftDataNodes(IntermediateQuery query, QueryNode leftNode) {
        if (leftNode instanceof ExtensionalDataNode)
            return Optional.of(ImmutableList.of((ExtensionalDataNode)leftNode));

        else if (leftNode instanceof InnerJoinNode) {
            ImmutableList<QueryNode> children = query.getChildren(leftNode);

            /*
             * ONLY if all the children of the join are extensional data nodes
             *
             * TODO: relax it
             */
            if (children.stream().allMatch(c -> c instanceof ExtensionalDataNode))
                return Optional.of(children.stream()
                        .map(c -> (ExtensionalDataNode) c)
                        .collect(ImmutableCollectors.toList()));
        }

        return Optional.empty();
    }


    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightDataNode(LeftJoinNode leftJoinNode,
                                                                                IntermediateQuery query,
                                                                                QueryTreeComponent treeComponent,
                                                                                QueryNode leftChild,
                                                                                ImmutableList<ExtensionalDataNode> leftChildren,
                                                                                DataNodeAndSubstitution rightComponent) {

        ImmutableSet<Variable> leftVariables = query.getVariables(leftChild);

        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(query.getKnownVariables());

        // Variable nullability at the level of the left-join sub-tree
        VariableNullability variableNullability = extractVariableNullability(query, leftJoinNode);

        LeftJoinRightChildNormalizationAnalysis analysis = normalizer.analyze(leftVariables, leftChildren,
                rightComponent.dataNode, variableGenerator, variableNullability);

        if (!analysis.isMatchingAConstraint())
            // No normalization
            return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);

        ImmutableSet<Variable> requiredVariablesAboveLJ = query.getVariablesRequiredByAncestors(leftJoinNode);

        /*
         * All the conditions that could be assigned to the LJ put together
         */
        Optional<ImmutableExpression> newLJCondition = termFactory.getConjunction(Stream.concat(
                    // Former condition
                    Stream.of(leftJoinNode.getOptionalFilterCondition(),
                            // New condition proposed by the analyser
                            analysis.getAdditionalExpression(),
                            // Former additional filter condition on the right
                            rightComponent.filterNode.map(FilterNode::getFilterCondition))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                    // Equalities extracted from the right substitution
                    rightComponent.constructionNode
                            .map(n -> extractEqualities(n.getSubstitution(), leftVariables))
                            .orElseGet(Stream::empty)));

        Optional<ImmutableSubstitution<ImmutableTerm>> remainingRightSubstitution = rightComponent.constructionNode
                .map(ConstructionNode::getSubstitution)
                .filter(s -> !s.isEmpty())
                .map(s -> substitutionFactory.getSubstitution(s.getImmutableMap().entrySet().stream()
                        .filter(e -> !leftVariables.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap())))
                .filter(s -> !s.isEmpty());


        /*
         * NB: removes the construction node if present, without inserting its substitution (will be propagated later)
         */
        LeftJoinNode normalizedLeftJoin = updateRightNodesAndLJ(leftJoinNode, rightComponent, newLJCondition,
                analysis.getProposedRightDataNode(), treeComponent);

        DataNode newRightChild = analysis.getProposedRightDataNode()
                .orElse(rightComponent.dataNode);

        LeftJoinNode leftJoinNodeToUpgrade = newLJCondition
                .map(ljCondition -> liftCondition(normalizedLeftJoin, leftChild, newRightChild, requiredVariablesAboveLJ,
                        treeComponent, remainingRightSubstitution, variableGenerator, query))
                // NB: here the normalized LJ is expected to be the initial left join
                .orElseGet(() -> remainingRightSubstitution
                        .map(s -> liftSubstitution(normalizedLeftJoin, s, query))
                        .orElse(normalizedLeftJoin));

        if (leftJoinNodeToUpgrade.getOptionalFilterCondition().isPresent())
            throw new MinorOntopInternalBugException("Bug: at this point the lj must not have a joining condition");

        /*
         * Replaces (upgrades) the left join by an inner join
         */
        InnerJoinNode innerJoinNode = iqFactory.createInnerJoinNode();
        treeComponent.replaceNode(leftJoinNodeToUpgrade, innerJoinNode);
        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(innerJoinNode));

    }

    private VariableNullability extractVariableNullability(IntermediateQuery query, LeftJoinNode leftJoinNode) {
        return iqConverter.convertTree(query, findAncestorForVariableNullability(query, leftJoinNode))
                .getVariableNullability();
    }

    /**
     * Recursive
     *
     * TODO: consider more cases? (Such as going beyond UNIONs when possible)
     */
    private QueryNode findAncestorForVariableNullability(IntermediateQuery query, QueryNode queryNode) {

        return query.getParent(queryNode)
                .filter(p -> ((p instanceof LeftJoinNode) && query.getOptionalPosition(p, queryNode)
                        .filter(pos -> pos.equals(LEFT))
                        .isPresent())
                        || (p instanceof InnerJoinNode)
                        || (p instanceof FilterNode))
                // Recursive
                .map(p -> findAncestorForVariableNullability(query, p))
                .orElse(queryNode);
    }


    /**
     * Extracts equalities involving a left variable from the substitution
     */
    private Stream<ImmutableExpression> extractEqualities(ImmutableSubstitution<ImmutableTerm> substitution,
                                                          ImmutableSet<Variable> leftVariables) {
        return substitution.getImmutableMap().entrySet().stream()
                .filter(e -> leftVariables.contains(e.getKey()) || leftVariables.contains(e.getValue()))
                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()));
    }

    /**
     * NB: removes the construction node if present, without inserting its substitution (will be propagated later)
     */
    private LeftJoinNode updateRightNodesAndLJ(LeftJoinNode leftJoinNode,
                                               DataNodeAndSubstitution rightComponent,
                                               Optional<ImmutableExpression> newLJCondition,
                                               Optional<DataNode> proposedRightDataNode,
                                               QueryTreeComponent treeComponent) {
        rightComponent.constructionNode
                .ifPresent(n -> treeComponent.replaceNodeByChild(n, Optional.empty()));
        rightComponent.filterNode
                .ifPresent(n -> treeComponent.replaceNodeByChild(n, Optional.empty()));
        proposedRightDataNode
                .ifPresent(n -> treeComponent.replaceNode(rightComponent.dataNode, n));
        LeftJoinNode newLeftJoinNode = leftJoinNode.changeOptionalFilterCondition(newLJCondition);
        treeComponent.replaceNode(leftJoinNode, newLeftJoinNode);

        return newLeftJoinNode;
    }

    private LeftJoinNode liftCondition(LeftJoinNode leftJoinNode, QueryNode leftChild, DataNode rightChild,
                                       ImmutableSet<Variable> requiredVariablesAboveLJ, QueryTreeComponent treeComponent,
                                       Optional<ImmutableSubstitution<ImmutableTerm>> remainingRightSubstitution,
                                       VariableGenerator variableGenerator, IntermediateQuery query) {
        ImmutableExpression ljCondition = leftJoinNode.getOptionalFilterCondition()
                .orElseThrow(() -> new IllegalArgumentException("The LJ is expected to have a joining condition"));

        ImmutableSet<Variable> leftVariables = query.getVariables(leftChild);
        ImmutableSet<Variable> requiredRightVariables = requiredVariablesAboveLJ.stream()
                .filter(v -> !leftVariables.contains(v))
                .collect(ImmutableCollectors.toSet());

        // Special case: ljCondition = IS_NOT_NULL(x) and x is a specific right variable
        // --> x will not be affected by the condition
        ImmutableSet<Variable> rightVariablesToUpdate = Optional.of(ljCondition)
                .filter(c -> c.getFunctionSymbol().equals(dbFunctionSymbolFactory.getDBIsNotNull()))
                .map(c -> c.getTerms().get(0))
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable) v)
                .map(specialVariable -> requiredRightVariables.stream()
                        .filter(v -> !v.equals(specialVariable))
                        .collect(ImmutableCollectors.toSet()))
                .orElse(requiredRightVariables);

        LeftJoinNode newLeftJoinNode = leftJoinNode.changeOptionalFilterCondition(Optional.empty());
        treeComponent.replaceNode(leftJoinNode, newLeftJoinNode);

        return (rightVariablesToUpdate.isEmpty() && (!remainingRightSubstitution.isPresent()))
                ? newLeftJoinNode
                : updateConditionalVariables(rightVariablesToUpdate, rightChild, newLeftJoinNode, ljCondition,
                                             query, treeComponent, remainingRightSubstitution, variableGenerator);
    }

    private LeftJoinNode updateConditionalVariables(ImmutableSet<Variable> rightVariablesToUpdate, DataNode rightChild,
                                                    LeftJoinNode newLeftJoinNode, ImmutableExpression ljCondition,
                                                    IntermediateQuery query, QueryTreeComponent treeComponent,
                                                    Optional<ImmutableSubstitution<ImmutableTerm>> remainingRightSubstitution,
                                                    VariableGenerator variableGenerator) {
        ImmutableMap<Variable, Variable> newVariableMap = rightVariablesToUpdate.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        variableGenerator::generateNewVariableFromVar));
        /*
         * Update the right child
         */
        InjectiveVar2VarSubstitution localSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(newVariableMap);
        DataNode newRightChild = rightChild.newAtom(localSubstitution.applyToDataAtom(rightChild.getProjectionAtom()));
        treeComponent.replaceNode(rightChild, newRightChild);

        ImmutableExpression newCondition = localSubstitution.applyToBooleanExpression(ljCondition);

        ImmutableSubstitution<ImmutableTerm> conditionalVarSubstitution = substitutionFactory.getSubstitution(
                newVariableMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> termFactory.getIfElseNull(newCondition, e.getValue())
                    )));

        ImmutableSubstitution<ImmutableTerm> substitutionToPropagate = remainingRightSubstitution
                .map(localSubstitution::applyRenaming)
                .map(s -> s.composeWith(conditionalVarSubstitution))
                .orElse(conditionalVarSubstitution);

        SubstitutionPropagationProposal<LeftJoinNode> proposal = new SubstitutionPropagationProposalImpl<>(
                newLeftJoinNode, substitutionToPropagate);

        try {
            return query.applyProposal(proposal, true)
                    .getNewNodeOrReplacingChild()
                    // Is expecting to be a ConstructionNode
                    .flatMap(query::getFirstChild)
                    .filter(n -> n instanceof LeftJoinNode)
                    .map(n -> (LeftJoinNode) n)
                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to keep the LJ node " +
                            "under a fresh construction node"));
        } catch (EmptyQueryException e) {
            throw new MinorOntopInternalBugException("This substitution propagation was not expected " +
                    "to make the query be empty");
        }
    }

    /**
     * Lifts the substitution in the absence of a LJ condition
     */
    private LeftJoinNode liftSubstitution(LeftJoinNode normalizedLeftJoin,
                                          ImmutableSubstitution<ImmutableTerm> remainingRightSubstitution,
                                          IntermediateQuery query) {
        SubstitutionPropagationProposal<LeftJoinNode> proposal = new SubstitutionPropagationProposalImpl<>(
                normalizedLeftJoin, remainingRightSubstitution);

        try {
            NodeCentricOptimizationResults<LeftJoinNode> results = query.applyProposal(proposal, true);
            return results
                    .getNewNodeOrReplacingChild()
                    // The LJ is expected to be the child of a construction node
                    .flatMap(query::getFirstChild)
                    .filter(n -> n instanceof LeftJoinNode)
                    .map(n -> (LeftJoinNode) n)
                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to insert a construction node " +
                            "followed by a LJ"));
        } catch (EmptyQueryException e) {
            throw new MinorOntopInternalBugException("This substitution propagation was not expected " +
                    "to make the query be empty");
        }
    }

    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightUnion(LeftJoinNode leftJoinNode,
                                                                             IntermediateQuery query,
                                                                             QueryTreeComponent treeComponent,
                                                                             QueryNode leftChild,
                                                                             ImmutableList<ExtensionalDataNode> leftDataNodes,
                                                                             UnionNode rightChild) {
        // NOT YET IMPLEMENTED --> no optimization YET
        return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
    }

    /**
     * May represent the right part of a LJ
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class DataNodeAndSubstitution {

        public final ExtensionalDataNode dataNode;
        public final Optional<FilterNode> filterNode;
        public final Optional<ConstructionNode> constructionNode;

        private DataNodeAndSubstitution(ExtensionalDataNode dataNode, Optional<FilterNode> filterNode,
                                        Optional<ConstructionNode> constructionNode) {
            this.dataNode = dataNode;
            this.filterNode = filterNode;
            this.constructionNode = constructionNode;
        }

        private DataNodeAndSubstitution(ExtensionalDataNode dataNode) {
            this.dataNode = dataNode;
            this.filterNode = Optional.empty();
            this.constructionNode = Optional.empty();
        }


        static Optional<DataNodeAndSubstitution> extract(ConstructionNode rightChild, IntermediateQuery query) {

            QueryNode grandChild = query.getFirstChild(rightChild).get();

            if (grandChild instanceof ExtensionalDataNode)
                return Optional.of(new DataNodeAndSubstitution((ExtensionalDataNode) grandChild, Optional.empty(),
                        Optional.of(rightChild)));
            else if (grandChild instanceof FilterNode) {
                FilterNode filterNode = (FilterNode) grandChild;

                return query.getFirstChild(grandChild)
                        .filter(n -> n instanceof ExtensionalDataNode)
                        .map(n -> (ExtensionalDataNode)n)
                        .map(n -> new DataNodeAndSubstitution(n, Optional.of(filterNode), Optional.of(rightChild)));
            }
            else
                return Optional.empty();
        }

        static DataNodeAndSubstitution extract(ExtensionalDataNode rightChild) {
            return new DataNodeAndSubstitution(rightChild);
        }

    }

}
