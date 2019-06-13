package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public abstract class RedundantSelfJoinExecutor extends SelfJoinLikeExecutor implements InnerJoinExecutor {

    /**
     * Safety, to prevent infinite loops
     */
    private static final int MAX_ITERATIONS = 100;
    private final IntermediateQueryFactory iqFactory;

    protected RedundantSelfJoinExecutor(IntermediateQueryFactory iqFactory,
                                        SubstitutionFactory substitutionFactory,
                                        ImmutableUnificationTools unificationTools) {
        super(substitutionFactory, unificationTools);
        this.iqFactory = iqFactory;
    }


    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(final InnerJoinOptimizationProposal highLevelProposal,
                                                               final IntermediateQuery query,
                                                               final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        // Non-final
        InnerJoinNode topJoinNode = highLevelProposal.getFocusNode();

        ImmutableMultimap<RelationPredicate, ExtensionalDataNode> initialMap = extractDataNodes(query.getChildren(topJoinNode));

        /*
         * Tries to optimize if there are data nodes
         */
        int i=0;
        while (!initialMap.isEmpty() && (i++ < MAX_ITERATIONS)) {

            ImmutableList<Variable> priorityVariables = prioritizeVariables(query, topJoinNode);

            try {
                Optional<ConcreteProposal> optionalConcreteProposal = propose(topJoinNode, initialMap, priorityVariables,
                        query);

                if (!optionalConcreteProposal.isPresent()) {
                    break;
                }
                else {
                    ConcreteProposal concreteProposal = optionalConcreteProposal.get();

                    // SIDE-EFFECT on the tree component (and thus on the query)
                    NodeCentricOptimizationResults<InnerJoinNode> result = applyOptimization(query, treeComponent,
                            topJoinNode, concreteProposal);

                    /*
                     * No change --> breaks the loop
                     */
                    if (result.getOptionalNewNode().isPresent()) {
                        int oldSize = initialMap.size();
                        initialMap = extractDataNodes(query.getChildren(
                                result.getOptionalNewNode().get()));
                        int newSize = initialMap.size();

                        if (oldSize == newSize) {
                            return result;
                        } else if (oldSize < newSize) {
                            throw new IllegalStateException("The number of data atoms was expected to decrease, not increase");
                        }
                        // else, continue
                        topJoinNode = result.getOptionalNewNode().get();

                    } else {
                        return result;
                    }
                }
                /*
                 * No unification --> empty result
                 */
            } catch (AtomUnificationException e) {
                return declareSubTreeAsEmpty(query, treeComponent, topJoinNode);
            }
        }

        /*
         * Safety
         */
        if (i >= MAX_ITERATIONS) {
            throw new IllegalStateException("Redundant self-join elimination loop has reached " +
                    "the max iteration threshold (" + MAX_ITERATIONS + ")");
        }

        // No optimization
        return new NodeCentricOptimizationResultsImpl<>(query, topJoinNode);
    }

    /**
     * Throws an AtomUnificationException when the results are guaranteed to be empty
     */
    private Optional<ConcreteProposal> propose(InnerJoinNode joinNode, ImmutableMultimap<RelationPredicate, ExtensionalDataNode> initialDataNodeMap,
                                               ImmutableList<Variable> priorityVariables,
                                               IntermediateQuery query)
            throws AtomUnificationException {

        ImmutableList.Builder<PredicateLevelProposal> proposalListBuilder = ImmutableList.builder();

        for (RelationPredicate predicate : initialDataNodeMap.keySet()) {
            ImmutableCollection<ExtensionalDataNode> initialNodes = initialDataNodeMap.get(predicate);
            Optional<PredicateLevelProposal> predicateProposal = proposePerPredicate(joinNode, initialNodes, predicate,
                    priorityVariables, query);
            predicateProposal.ifPresent(proposalListBuilder::add);
        }

        return createConcreteProposal(proposalListBuilder.build(), priorityVariables);
    }

    protected abstract Optional<PredicateLevelProposal> proposePerPredicate(InnerJoinNode joinNode, ImmutableCollection<ExtensionalDataNode> initialNodes,
                                                                            RelationPredicate predicate,
                                                                            ImmutableList<Variable> priorityVariables,
                                                                            IntermediateQuery query) throws AtomUnificationException;

    /**
     * Assumes that the data atoms are leafs.
     *
     *
     *
     */
    private NodeCentricOptimizationResults<InnerJoinNode> applyOptimization(IntermediateQuery query,
                                                                                     QueryTreeComponent treeComponent,
                                                                                     InnerJoinNode topJoinNode,
                                                                                     ConcreteProposal proposal)
            throws EmptyQueryException {
        /*
         * First, add and remove non-top nodes
         */
        proposal.getDataNodesToRemove()
                .forEach(treeComponent::removeSubTree);

        return updateJoinNodeAndPropagateSubstitution(query, treeComponent, topJoinNode, proposal);
    }
    
    private NodeCentricOptimizationResults<InnerJoinNode> declareSubTreeAsEmpty(IntermediateQuery query,
                                                                                QueryTreeComponent treeComponent,
                                                                                InnerJoinNode topJoinNode) {
        /*
         * Replaces by an EmptyNode
         */
        EmptyNode emptyNode = iqFactory.createEmptyNode(query.getVariables(topJoinNode));
        treeComponent.replaceSubTree(topJoinNode, emptyNode);

        /*
         * If the query is not empty, changes the type of the results
         */
        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(emptyNode));
    }

}
