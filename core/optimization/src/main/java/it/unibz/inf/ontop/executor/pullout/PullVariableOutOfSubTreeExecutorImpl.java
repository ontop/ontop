package it.unibz.inf.ontop.executor.pullout;

import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SubstitutionResults;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeProposal;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeResults;
import it.unibz.inf.ontop.iq.proposal.impl.PullVariableOutOfSubTreeResultsImpl;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.executor.substitution.DescendingPropagationTools.propagateSubstitutionDown;
import static it.unibz.inf.ontop.model.predicate.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

/**
 * TODO: explain
 */
@Singleton
public class PullVariableOutOfSubTreeExecutorImpl<N extends JoinLikeNode> implements PullVariableOutOfSubTreeExecutor<N> {

    @Override
    public PullVariableOutOfSubTreeResults<N> apply(PullVariableOutOfSubTreeProposal<N> proposal,
                                                    IntermediateQuery query,
                                                    QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        /**
         * TODO: check for obvious misuse of the proposal
         */

        N newFocusNode = createNewFocusNodeWithAdditionalConditions(proposal);
        treeComponent.replaceNode(proposal.getFocusNode(), newFocusNode);

        QueryNode newSubTreeRoot = propagateRenamings(proposal, query, treeComponent);


        return new PullVariableOutOfSubTreeResultsImpl<>(query, newFocusNode, newSubTreeRoot);

    }

    private N createNewFocusNodeWithAdditionalConditions(PullVariableOutOfSubTreeProposal<N> proposal) {
        N focusNode = proposal.getFocusNode();

        Stream<ImmutableExpression> newConditions = proposal.getRenamingSubstitution().getImmutableMap().entrySet().stream()
                .map(e -> DATA_FACTORY.getImmutableExpression(EQ, e.getKey(), e.getValue()));

        Stream<ImmutableExpression> otherConditions = focusNode.getOptionalFilterCondition()
                .map(exp -> exp.flattenAND().stream())
                .orElseGet(Stream::of);

        return (N) focusNode.changeOptionalFilterCondition(
                ImmutabilityTools.foldBooleanExpressions(Stream.concat(otherConditions, newConditions)));
    }

    /**
     * TODO: explain
     */
    private QueryNode propagateRenamings(PullVariableOutOfSubTreeProposal<N> proposal, IntermediateQuery query,
                                         QueryTreeComponent treeComponent) {

        InjectiveVar2VarSubstitution renamingSubstitution = proposal.getRenamingSubstitution();
        QueryNode originalSubTreeNode = proposal.getSubTreeRootNode();

        SubstitutionResults<? extends QueryNode> rootRenamingResults = originalSubTreeNode
                .applyDescendingSubstitution(renamingSubstitution, query);

        Optional<? extends QueryNode> optionalUpdatedNode = rootRenamingResults.getOptionalNewNode();

        if (optionalUpdatedNode.isPresent()) {
            treeComponent.replaceNode(originalSubTreeNode, optionalUpdatedNode.get());
        }

        QueryNode newSubTreeRootNode = optionalUpdatedNode
                .map(n -> (QueryNode) n)
                .orElse(originalSubTreeNode);

        /**
         * Updates the tree component
         */
        try {
            propagateSubstitutionDown(newSubTreeRootNode, renamingSubstitution, query, treeComponent);
        } catch (EmptyQueryException e) {
            throw new IllegalStateException("PullVariableOutOfSubTree should not generate a QueryEmptyNodeException");
        }

        // TODO: make sure the root node has not been modified (would be a bug)
        return newSubTreeRootNode;
    }
}
