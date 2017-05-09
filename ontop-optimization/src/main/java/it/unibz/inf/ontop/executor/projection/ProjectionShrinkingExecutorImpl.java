package it.unibz.inf.ontop.executor.projection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProjectionShrinkingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class ProjectionShrinkingExecutorImpl implements ProjectionShrinkingExecutor {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private ProjectionShrinkingExecutorImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public NodeCentricOptimizationResults<ExplicitVariableProjectionNode> apply(ProjectionShrinkingProposal proposal,
                                                                                IntermediateQuery query,
                                                                                QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

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
                        DATA_FACTORY.getSubstitution(shrinkedMap),
                        ((ConstructionNode) focusNode).getOptionalModifiers()
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
