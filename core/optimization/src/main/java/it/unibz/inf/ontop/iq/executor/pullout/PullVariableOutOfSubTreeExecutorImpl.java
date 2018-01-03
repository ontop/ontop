package it.unibz.inf.ontop.iq.executor.pullout;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeProposal;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeResults;
import it.unibz.inf.ontop.iq.proposal.impl.PullVariableOutOfSubTreeResultsImpl;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

/**
 * TODO: explain
 */
@Singleton
public class PullVariableOutOfSubTreeExecutorImpl<N extends JoinLikeNode> implements PullVariableOutOfSubTreeExecutor<N> {

    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;
    private final IQConverter iqConverter;

    @Inject
    private PullVariableOutOfSubTreeExecutorImpl(TermFactory termFactory, ImmutabilityTools immutabilityTools,
                                                 IQConverter iqConverter) {
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
        this.iqConverter = iqConverter;
    }

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
                .map(e -> termFactory.getImmutableExpression(EQ, e.getKey(), e.getValue()));

        Stream<ImmutableExpression> otherConditions = focusNode.getOptionalFilterCondition()
                .map(exp -> exp.flattenAND().stream())
                .orElseGet(Stream::of);

        return (N) focusNode.changeOptionalFilterCondition(
                immutabilityTools.foldBooleanExpressions(Stream.concat(otherConditions, newConditions)));
    }

    /**
     * TODO: explain
     */
    private QueryNode propagateRenamings(PullVariableOutOfSubTreeProposal<N> proposal, IntermediateQuery query,
                                         QueryTreeComponent treeComponent) {

        InjectiveVar2VarSubstitution renamingSubstitution = proposal.getRenamingSubstitution();
        QueryNode originalSubTreeNode = proposal.getSubTreeRootNode();

        return propagateDownSubstitution(originalSubTreeNode, renamingSubstitution, query, treeComponent);

    }

    private QueryNode propagateDownSubstitution(QueryNode originalSubTreeNode,
                                                ImmutableSubstitution<? extends Variable> substitution,
                                                IntermediateQuery query, QueryTreeComponent treeComponent) {

        IQTree propagatedSubTree = iqConverter.convertTree(query, originalSubTreeNode)
                .applyDescendingSubstitution(substitution, Optional.empty());

        return treeComponent.replaceSubTreeByIQ(originalSubTreeNode, propagatedSubTree);
    }

}
