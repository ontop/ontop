package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import java.util.AbstractMap;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfDataNodeProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfSubTreeProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfSubTreeResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PullVariableOutOfDataNodeProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PullVariableOutOfSubTreeProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.Set;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getNextNodeAndQuery;

/**
 * TODO: explain
 */
public class PullOutVariableOptimizer implements IntermediateQueryOptimizer {

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {
        try {
            return pullOutSomeVariables(query);
        } catch (EmptyQueryException e) {
            throw new IllegalStateException("Inconsistency: PullOutVariableOptimizer should not empty the query");
        }
    }

    /**
     * TODO: explain
     *
     * Depth-first exploration
     *
     */
    private IntermediateQuery pullOutSomeVariables(IntermediateQuery initialQuery)
            throws EmptyQueryException {
        // Non-final
        Optional<QueryNode> optionalCurrentNode = initialQuery.getFirstChild(initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalCurrentNode.isPresent()) {
            final QueryNode currentNode = optionalCurrentNode.get();

            /**
             * Targets: join-like nodes and data nodes
             */
            if (currentNode instanceof JoinLikeNode) {
                NextNodeAndQuery nextNodeAndQuery = optimizeJoinLikeNodeChildren(
                        currentQuery, (JoinLikeNode) currentNode);

                optionalCurrentNode = nextNodeAndQuery.getOptionalNextNode();
                currentQuery = nextNodeAndQuery.getNextQuery();
            }
            else if (currentNode instanceof DataNode) {
                NextNodeAndQuery nextNodeAndQuery = optimizeDataNode(currentQuery,
                        (DataNode) currentNode);
                optionalCurrentNode = nextNodeAndQuery.getOptionalNextNode();
                currentQuery = nextNodeAndQuery.getNextQuery();

            }
            else {
                optionalCurrentNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }

    /**
     *
     * Looks at the projected variables by all the child sub-trees and does the appropriate renamings.
     *
     */
    private NextNodeAndQuery optimizeJoinLikeNodeChildren(IntermediateQuery initialQuery, JoinLikeNode initialJoinLikeNode)
            throws EmptyQueryException {

        Optional<QueryNode> optionalFirstChild = initialQuery.getFirstChild(initialJoinLikeNode);

        // Non-final
        Optional<QueryNode> optionalNextChild = optionalFirstChild
                .flatMap(initialQuery::getNextSibling);

        /**
         * Less than 2 children: nothing to be done here
         */
        if (!optionalNextChild.isPresent()) {
            return new NextNodeAndQuery(getDepthFirstNextNode(initialQuery, initialJoinLikeNode),
                    initialQuery);
        }

        Set<Variable> variablesFromOtherSubTrees = new HashSet<>(initialQuery.getProjectedVariables(
                optionalFirstChild.get()));

        // Non-final variables
        IntermediateQuery currentQuery = initialQuery;
        JoinLikeNode currentJoinLikeNode = initialJoinLikeNode;


        /**
         * TODO: explain
         */
        while (optionalNextChild.isPresent()){

            QueryNode childNode = optionalNextChild.get();

            ImmutableSet<Variable> projectedVariablesByThisChild = currentQuery.getProjectedVariables(childNode);

            // To trick the compiler
            final IntermediateQuery query = currentQuery;

            ImmutableMap<Variable, Variable> substitutionMap = projectedVariablesByThisChild.stream()
                    .filter(variablesFromOtherSubTrees::contains)
                    .map(v -> new AbstractMap.SimpleEntry<>(v, query.generateNewVariable(v)))
                    .collect(ImmutableCollectors.toMap());

            if (substitutionMap.isEmpty()) {
                optionalNextChild = currentQuery.getNextSibling(childNode);
            }
            else {
                variablesFromOtherSubTrees.addAll(substitutionMap.keySet());

                InjectiveVar2VarSubstitution renamingSubstitution = new InjectiveVar2VarSubstitutionImpl(substitutionMap);

                PullVariableOutOfSubTreeProposal<JoinLikeNode> proposal = new PullVariableOutOfSubTreeProposalImpl<>(
                        currentJoinLikeNode, renamingSubstitution, childNode);

                PullVariableOutOfSubTreeResults<JoinLikeNode> results = currentQuery.applyProposal(proposal);

                /**
                 * Updates the "iterated" variables
                 */
                currentJoinLikeNode = results.getOptionalNewNode()
                        .orElseThrow(() -> new IllegalStateException("The JoinLikeNode was expected to be preserved"));
                currentQuery = results.getResultingQuery();

                optionalNextChild = currentQuery.getNextSibling(results.getNewSubTreeRoot());
            }
        }

        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentJoinLikeNode),
                currentQuery);
    }


    /**
     * May update alreadySeenVariables!
     *
     * TODO: explain it further
     */
    private Optional<PullVariableOutOfDataNodeProposal> buildProposal(DataNode dataNode,
                                                                      Set<Variable> alreadySeenVariables) {
        ImmutableList.Builder<Integer> variableIndexListBuilder = ImmutableList.builder();

        DataAtom dataAtom = dataNode.getProjectionAtom();
        ImmutableList<? extends VariableOrGroundTerm> arguments = dataAtom.getArguments();

        for (int i=0; i < arguments.size(); i++) {
            VariableOrGroundTerm argument = arguments.get(i);
            if (argument instanceof Variable) {
                Variable variable = (Variable) argument;

                /**
                 * Tracks the indexes of variables to "pull out"
                 */
                if (!alreadySeenVariables.add(variable)) {
                    variableIndexListBuilder.add(i);
                }
            }
        }

        ImmutableList<Integer> toReplaceVariableIndexes = variableIndexListBuilder.build();
        if (!toReplaceVariableIndexes.isEmpty()) {
            PullVariableOutOfDataNodeProposal proposal = new PullVariableOutOfDataNodeProposalImpl(dataNode,
                    toReplaceVariableIndexes);
            return Optional.of(proposal);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * TODO: explain
     *
     */
    private NextNodeAndQuery optimizeDataNode(IntermediateQuery currentQuery, DataNode currentNode)
            throws EmptyQueryException {

        Optional<PullVariableOutOfDataNodeProposal> optionalProposal = buildProposal(currentNode, new HashSet<>());

        if (optionalProposal.isPresent()) {
            PullVariableOutOfDataNodeProposal proposal = optionalProposal.get();
            NodeCentricOptimizationResults<DataNode> results = currentQuery.applyProposal(proposal);

            return getNextNodeAndQuery(results);
        }
        else {
            // NB: a DataNode is not expected to have a child
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }
}

