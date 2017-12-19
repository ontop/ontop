package it.unibz.inf.ontop.iq.executor.substitution;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.executor.substitution.LocalPropagationTools.SubstitutionApplicationResults;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.executor.substitution.AscendingPropagationTools.propagateSubstitutionUp;
import static it.unibz.inf.ontop.iq.executor.substitution.DescendingPropagationTools.propagateSubstitutionDown;
import static it.unibz.inf.ontop.iq.executor.substitution.LocalPropagationTools.applySubstitutionToNode;

/**
 * TODO: explain
 */
@Singleton
public class SubstitutionPropagationExecutorImpl<N extends QueryNode>
        implements SubstitutionPropagationExecutor<N> {

    private final ImmutableSubstitutionTools substitutionTools;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private SubstitutionPropagationExecutorImpl(ImmutableSubstitutionTools substitutionTools,
                                                IntermediateQueryFactory iqFactory,
                                                SubstitutionFactory substitutionFactory) {
        this.substitutionTools = substitutionTools;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public NodeCentricOptimizationResults<N> apply(SubstitutionPropagationProposal<N> proposal,
                                                   IntermediateQuery query,
                                                   QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        try {
            return applySubstitution(proposal, query, treeComponent);
        }
        catch (QueryNodeSubstitutionException e) {
            throw new InvalidQueryOptimizationProposalException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     *
     * TODO: refactor
     *
     */
    private NodeCentricOptimizationResults<N> applySubstitution(SubstitutionPropagationProposal<N> proposal,
                                                                        IntermediateQuery query,
                                                                        QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException, EmptyQueryException {
        N originalFocusNode = proposal.getFocusNode();
        ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate = normalizeInputSubstitution(
                originalFocusNode, query, proposal.getSubstitution());

        /*
         * First to the focus node
         */
        SubstitutionApplicationResults<N> localApplicationResults = applySubstitutionToFocusNode(originalFocusNode,
                substitutionToPropagate, query, treeComponent);

        QueryNode newFocusOrReplacingChildNode = localApplicationResults.getNewNodeOrReplacingChild()
                .orElseThrow(() -> new InvalidQueryOptimizationProposalException(
                        "A SubstitutionPropagationProposal must provide a substitution " +
                                "that is directly applicable to the focus node (the focus node should not reject it)"));

        /*
         * Then propagates up
         *
         * NB: this can remove the focus node (or its replacing child) but not altered it and its sub-tree.
         *
         */
        NodeCentricOptimizationResults<QueryNode> ascendingPropagationResults = propagateSubstitutionUp(
                newFocusOrReplacingChildNode,
                substitutionToPropagate, query, treeComponent, iqFactory, substitutionFactory, Optional.empty());

        /*
         * If some ancestors are removed, does not go further
         */
        if (!ascendingPropagationResults.getOptionalNewNode().isPresent()) {
            if (ascendingPropagationResults.getOptionalReplacingChild().isPresent()) {
                throw new IllegalStateException("The focus node is not expected to be replaced " +
                        "by its child while propagating the substitution up");
            }

            return new NodeCentricOptimizationResultsImpl<>(query,
                    ascendingPropagationResults.getOptionalNextSibling(),
                    ascendingPropagationResults.getOptionalClosestAncestor());
        }

        if (ascendingPropagationResults.getOptionalNewNode().get() != newFocusOrReplacingChildNode) {
            throw new IllegalStateException("The original focus node was not expected to changed");
        }

        /*
         * Finally, propagates down and returns the results
         *
         * NB: localApplicationResults should still be valid after propagating the substitution up
         */
        return propagateDown(query, treeComponent, localApplicationResults);


    }

    /**
     * Normalizes the input substitution so as to avoid projected variables to be renamed into variables
     * that are NOT PROJECTED by the closest ExplicitVariableProjectionNode.
     *
     * Such an ancestor BLOCKS ascending substitutions and may thus be forced to incorporate some bindings
     * of the substitution (if it is a ConstructionNode) or to insert a construction node out of a fragment
     * of the substitution (if it is a UnionNode).
     *
     * Without this normalization, blocking the substitution could cause the insertion of illegal bindings
     * (projected variable to non-projected variable) into a construction node, producing an invalid IQ.
     *
     * An alternative solution to normalization would have been to let the ancestors push down a renaming
     * substitution, but this complicates the substitution propagation mechanisms.
     *
     */
    private ImmutableSubstitution<? extends ImmutableTerm> normalizeInputSubstitution(
            N originalFocusNode, IntermediateQuery query, ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        if (query.getRootNode() == originalFocusNode)
            return substitution;

        ImmutableSet<Variable> priorityVariables = query.getAncestors(originalFocusNode).stream()
                .filter(a -> a instanceof ExplicitVariableProjectionNode)
                .map(a -> (ExplicitVariableProjectionNode) a)
                .findFirst()
                .map(ExplicitVariableProjectionNode::getVariables)
                .orElseGet(() -> query.getProjectionAtom().getVariables());

        return substitutionTools.prioritizeRenaming(substitution, priorityVariables);
    }

    /**
     * In case the focus is replaced by a child, applies the substitution recursively
     */
    private SubstitutionApplicationResults<N> applySubstitutionToFocusNode(N originalFocusNode,
                                                                           ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
                                                                           IntermediateQuery query,
                                                                           QueryTreeComponent treeComponent) throws EmptyQueryException {

        SubstitutionApplicationResults<N> localApplicationResults = applySubstitutionToNode(originalFocusNode,
                substitutionToPropagate, query, treeComponent, Optional.empty());

        if (localApplicationResults.getOptionalReplacingChild().isPresent()) {
            QueryNode replacingChild = localApplicationResults.getOptionalReplacingChild().get();

            Optional<ImmutableSubstitution<? extends ImmutableTerm>> newSubstitution = localApplicationResults.getOptionalSubstitution();
            Optional<NodeTracker> optionalTracker = localApplicationResults.getOptionalTracker();

            /*
             * Applies the substitution to the replacing child (recursive)
             */
            if (newSubstitution.isPresent()) {
                SubstitutionApplicationResults<QueryNode> replacingChildResults = applySubstitutionToNode(
                        replacingChild, newSubstitution.get(), query, treeComponent, optionalTracker);

                // The replacing child of the replacing child is the new replacing child
                Optional<QueryNode> optionalNewReplacingChild = replacingChildResults.getNewNodeOrReplacingChild();
                if (optionalNewReplacingChild.isPresent()) {
                    return new SubstitutionApplicationResults<>(query, optionalNewReplacingChild.get(),
                            replacingChildResults.getOptionalSubstitution(), true, optionalTracker);
                }
                /*
                 * No replacing child after applying the substitution (--> is empty)
                 */
                else {
                    return new SubstitutionApplicationResults<>(query, replacingChildResults);
                }
            }
        }

        /*
         * By default, no recursion
         */
        return localApplicationResults;
    }


    private NodeCentricOptimizationResults<N> propagateDown(IntermediateQuery query, QueryTreeComponent treeComponent,
                                                            SubstitutionApplicationResults<N> localApplicationResults)
            throws EmptyQueryException {

        if (localApplicationResults.getNewNodeOrReplacingChild().isPresent()) {

            /*
             * Still a substitution to propagate down
             */
            if (localApplicationResults.getOptionalSubstitution().isPresent()) {
                ImmutableSubstitution<? extends ImmutableTerm> newSubstitution = localApplicationResults.getOptionalSubstitution().get();

                Optional<N> optionalNewFocusNode = localApplicationResults.getOptionalNewNode();

                if (optionalNewFocusNode.isPresent()) {
                    return propagateSubstitutionDown(optionalNewFocusNode.get(), newSubstitution, query, treeComponent);
                }
                /*
                 * When the focus has already been replaced by its child
                 */
                else  {
                    QueryNode replacingNode = localApplicationResults.getOptionalReplacingChild().get();

                    /*
                     * The results have to be converted
                     */
                    NodeCentricOptimizationResults<QueryNode> descendingResults = propagateSubstitutionDown(
                            replacingNode, newSubstitution, query, treeComponent);

                    if (descendingResults.getNewNodeOrReplacingChild().isPresent()) {
                        // Declares as replacing child
                        return new NodeCentricOptimizationResultsImpl<>(query, descendingResults.getNewNodeOrReplacingChild());
                    } else {
                        return new NodeCentricOptimizationResultsImpl<>(query, descendingResults.getOptionalNextSibling(),
                                descendingResults.getOptionalClosestAncestor());
                    }
                }
            }
            /*
             * No propagation down
             */
            else {
                return localApplicationResults.getOptionalNewNode()
                        .map(focus -> new NodeCentricOptimizationResultsImpl<>(query, focus))
                        .orElseGet(() -> new NodeCentricOptimizationResultsImpl<>(query,
                                localApplicationResults.getOptionalReplacingChild()));
            }
        }
        /*
         *  The focus node has removed by the local application
         *
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalNextSibling(),
                    localApplicationResults.getOptionalClosestAncestor());
        }
    }
}
