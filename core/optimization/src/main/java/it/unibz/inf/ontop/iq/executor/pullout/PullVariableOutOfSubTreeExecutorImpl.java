package it.unibz.inf.ontop.iq.executor.pullout;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SubstitutionResults;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
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

import static it.unibz.inf.ontop.iq.executor.substitution.DescendingPropagationTools.propagateSubstitutionDown;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

/**
 * TODO: explain
 */
@Singleton
public class PullVariableOutOfSubTreeExecutorImpl<N extends JoinLikeNode> implements PullVariableOutOfSubTreeExecutor<N> {

    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private PullVariableOutOfSubTreeExecutorImpl(TermFactory termFactory, ImmutabilityTools immutabilityTools) {
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
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

    /**
     * UGLY!
     *
     * Because DescendingPropagationTools.propagateSubstitutionDown() does not apply the substitution to
     * the root, this method contains the logic to apply it to the root
     */
    private QueryNode propagateDownSubstitution(QueryNode originalSubTreeNode,
                                                ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                IntermediateQuery query, QueryTreeComponent treeComponent) {

        SubstitutionResults<? extends QueryNode> rootRenamingResults = originalSubTreeNode
                .applyDescendingSubstitution(substitution, query);

        Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> newSubstitution =
                rootRenamingResults.getSubstitutionToPropagate();

        QueryNode newSubTreeRootNode;
        switch (rootRenamingResults.getLocalAction()) {
            case NO_CHANGE:
                newSubTreeRootNode = originalSubTreeNode;
                break;
            case NEW_NODE:
                newSubTreeRootNode = rootRenamingResults.getOptionalNewNode().get();
                treeComponent.replaceNode(originalSubTreeNode, newSubTreeRootNode);
                break;
            case REPLACE_BY_CHILD:
                newSubTreeRootNode = treeComponent.replaceNodeByChild(originalSubTreeNode,
                        rootRenamingResults.getOptionalReplacingChildPosition());
                // Recursive
                return newSubstitution
                        // Recursive
                        .map(s -> propagateDownSubstitution(newSubTreeRootNode, s, query, treeComponent))
                        .orElse(newSubTreeRootNode);
            default:
                throw new MinorOntopInternalBugException("Unexpected local action for propagating renamings down to a " +
                        "subtree " + rootRenamingResults.getLocalAction());
        }

        if (newSubstitution.isPresent()) {
        /*
         * Updates the tree component
         */
            try {
                propagateSubstitutionDown(newSubTreeRootNode, newSubstitution.get(), query, treeComponent);
            } catch (EmptyQueryException e) {
                throw new IllegalStateException("PullVariableOutOfSubTree should not generate a QueryEmptyNodeException");
            }
        }

        // TODO: make sure the root node has not been modified (would be a bug)
        return newSubTreeRootNode;
    }

}
