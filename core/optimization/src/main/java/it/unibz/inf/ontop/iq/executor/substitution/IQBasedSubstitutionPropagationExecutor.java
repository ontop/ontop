package it.unibz.inf.ontop.iq.executor.substitution;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

/**
 * TEMPORARY CODE!
 *
 * Substitution propagation executor for the transition from mutable Intermediate Queries to immutable IQs.
 *
 * Does NOT propagate UP but insert instead a ConstructionNode
 *
 * Propagates to the focus node and DOWN.
 *
 * Restriction: non ground functional terms cannot be propagated DOWN, but they can be inserted in the "ascending"
 * construction node.
 *
 */
@Singleton
public class IQBasedSubstitutionPropagationExecutor<N extends QueryNode> implements SubstitutionPropagationExecutor<N> {

    private final IQConverter iqConverter;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private IQBasedSubstitutionPropagationExecutor(IQConverter iqConverter, IntermediateQueryFactory iqFactory) {
        this.iqConverter = iqConverter;
        this.iqFactory = iqFactory;
    }


    @Override
    public NodeCentricOptimizationResults<N> apply(SubstitutionPropagationProposal<N> proposal,
                                                   IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        N focusNode = proposal.getFocusNode();
        IQTree propagatedSubTree = propagate(iqConverter.convertTree(query, focusNode),
                proposal.getSubstitution());

        treeComponent.replaceSubTreeByIQ(focusNode, propagatedSubTree);

        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(propagatedSubTree.getRootNode()));
    }

    private IQTree propagate(IQTree subTree, ImmutableSubstitution<? extends ImmutableTerm> substitution)
            throws InvalidQueryOptimizationProposalException {

        IQTree newSubTree = subTree.applyDescendingSubstitution(substitution.getVariableOrGroundTermFragment(),
                Optional.empty());

        if (substitution.getNonGroundFunctionalTermFragment().getDomain().stream()
                .anyMatch(v -> newSubTree.getVariables().contains(v)))
            throw new InvalidQueryOptimizationProposalException("Non ground functional terms are not supported for propagation down " +
                    "(only for up)");

        ConstructionNode constructionNode = iqFactory.createConstructionNode(
                Sets.union(newSubTree.getVariables(), substitution.getDomain()).immutableCopy(),
                (ImmutableSubstitution<ImmutableTerm>) substitution);

        return iqFactory.createUnaryIQTree(constructionNode, newSubTree);
    }
}
