package it.unibz.inf.ontop.iq.executor.projection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.*;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.ProjectionShrinkingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


public class ProjectionShrinkingExecutorImpl implements ProjectionShrinkingExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private ProjectionShrinkingExecutorImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public NodeCentricOptimizationResults<ExplicitVariableProjectionNode> apply(ProjectionShrinkingProposal proposal,
                                                                                IntermediateQuery query,
                                                                                QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        ExplicitVariableProjectionNode focusNode = proposal.getFocusNode();
        ImmutableSet<Variable> retainedVariables = proposal.getRetainedVariables();
        if (focusNode instanceof UnionNode) {
            UnionNode replacingNode = iqFactory.createUnionNode(retainedVariables);
            treeComponent.replaceNode(focusNode, replacingNode);
            return new NodeCentricOptimizationResultsImpl<>(query, replacingNode);
        }

        if (focusNode instanceof ConstructionNode) {
            if (retainedVariables.size() > 0) {

                ImmutableMap<Variable, ImmutableTerm> shrinkedMap =
                        ((ConstructionNode) focusNode).getSubstitution().getImmutableMap().entrySet().stream().
                                filter(e -> retainedVariables.contains(e.getKey()))
                                .collect(ImmutableCollectors.toMap());

                ConstructionNode replacingNode = iqFactory.createConstructionNode(
                        retainedVariables,
                        substitutionFactory.getSubstitution(shrinkedMap)
                );
                treeComponent.replaceNode(focusNode, replacingNode);
                return new NodeCentricOptimizationResultsImpl<>(query, replacingNode);
            }
            if (query.getFirstChild(focusNode).isPresent()) {
                QueryNode replacingNode = treeComponent.removeOrReplaceNodeByUniqueChild(focusNode);
                return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(replacingNode));
            }
            TrueNode replacingNode = iqFactory.createTrueNode();
            treeComponent.replaceNode(focusNode, replacingNode);
            /**
             * This is a dirty trick (the TrueNode is considered as the child of the construction node)
             *
             * TODO: find a cleaner way (refactor the NodeCentricOptimizationResultsImpl ?)
             */
            return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(replacingNode));
        }
        throw new IllegalStateException("a projection shrinking proposal can only be made for a Union or Construction node");
    }
}
