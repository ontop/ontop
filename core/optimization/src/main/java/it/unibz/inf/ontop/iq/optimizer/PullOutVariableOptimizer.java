package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfDataNodeProposal;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeProposal;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeResults;
import it.unibz.inf.ontop.iq.proposal.impl.PullVariableOutOfDataNodeProposalImpl;
import it.unibz.inf.ontop.iq.proposal.impl.PullVariableOutOfSubTreeProposalImpl;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.getNextNodeAndQuery;

/**
 * TODO: explain
 */
@Singleton
public class PullOutVariableOptimizer implements IntermediateQueryOptimizer {

    private static class ParentNextChild {
        final Optional<QueryNode> nextChild;
        final JoinLikeNode parent;

        private ParentNextChild(Optional<QueryNode> nextChild, JoinLikeNode parent) {
            this.nextChild = nextChild;
            this.parent = parent;
        }
    }

    private final SubstitutionFactory substitutionFactory;

    @Inject
    private PullOutVariableOptimizer(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }


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
        Optional<QueryNode> optionalCurrentNode = initialQuery.getFirstChild(initialQuery.getRootNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalCurrentNode.isPresent()) {
            final QueryNode currentNode = optionalCurrentNode.get();

            /*
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
    private NextNodeAndQuery optimizeJoinLikeNodeChildren(IntermediateQuery query, JoinLikeNode initialJoinLikeNode)
            throws EmptyQueryException {

        // Non-final
        Optional<QueryNode> optionalCurrentChild = query.getFirstChild(initialJoinLikeNode);
        JoinLikeNode currentJoinLikeNode = initialJoinLikeNode;

        while (optionalCurrentChild.isPresent()) {
            ParentNextChild parentNextChild = optimizeFollowingChildren(query, optionalCurrentChild.get(), currentJoinLikeNode);
            optionalCurrentChild = parentNextChild.nextChild;
            currentJoinLikeNode = parentNextChild.parent;
        }

        return new NextNodeAndQuery(getDepthFirstNextNode(query, currentJoinLikeNode),
                query);
    }

    /**
     * For one child, renames the variables of the following ones if they conflicting with its own variables
     */
    private ParentNextChild optimizeFollowingChildren(IntermediateQuery query, QueryNode focusChild,
                                                          JoinLikeNode initialJoinLikeNode)
            throws EmptyQueryException {
        // Non-final
        Optional<QueryNode> optionalNextChild = query.getNextSibling(focusChild);

        /*
         * Less than 2 children: nothing to be done here
         */
        if (!optionalNextChild.isPresent()) {
            return new ParentNextChild(Optional.empty(), initialJoinLikeNode);
        }

        Set<Variable> variablesFromOtherSubTrees = new HashSet<>(query.getVariables(
                focusChild));

        // Non-final variables
        JoinLikeNode currentJoinLikeNode = initialJoinLikeNode;


        /*
         * Loops over the following children
         */
        while (optionalNextChild.isPresent()){

            QueryNode childNode = optionalNextChild.get();

            ImmutableSet<Variable> projectedVariablesByThisChild = query.getVariables(childNode);

            ImmutableMap<Variable, Variable> substitutionMap = projectedVariablesByThisChild.stream()
                    .filter(variablesFromOtherSubTrees::contains)
                    .map(v -> new AbstractMap.SimpleEntry<>(v, query.generateNewVariable(v)))
                    .collect(ImmutableCollectors.toMap());

            if (substitutionMap.isEmpty()) {
                optionalNextChild = query.getNextSibling(childNode);
            }
            else {
                variablesFromOtherSubTrees.addAll(substitutionMap.keySet());

                InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap);

                PullVariableOutOfSubTreeProposal<JoinLikeNode> proposal = new PullVariableOutOfSubTreeProposalImpl<>(
                        currentJoinLikeNode, renamingSubstitution, childNode);

                PullVariableOutOfSubTreeResults<JoinLikeNode> results = query.applyProposal(proposal);

                /*
                 * Updates the "iterated" variables
                 */
                currentJoinLikeNode = results.getOptionalNewNode()
                        .orElseThrow(() -> new IllegalStateException("The JoinLikeNode was expected to be preserved"));

                optionalNextChild = query.getNextSibling(results.getNewSubTreeRoot());
            }
        }

        return new ParentNextChild(query.getNextSibling(focusChild), currentJoinLikeNode);
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

                /*
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

            return getNextNodeAndQuery(currentQuery, results);
        }
        else {
            // NB: a DataNode is not expected to have a child
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }
}

