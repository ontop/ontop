package it.unibz.inf.ontop.executor.merging;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodesProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodesProposalImpl;
import it.unibz.inf.ontop.utils.FunctionalTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static it.unibz.inf.ontop.pivotalrepr.impl.IntermediateQueryUtils.generateNotConflictingRenaming;
import static it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools.extractProjectedVariables;

public class QueryMergingExecutor implements InternalProposalExecutor<QueryMergingProposal, ProposalResults> {

    /**
     * TODO: explain
     */
    private static class Transformation {

        private static class AnalysisResults {
            public final QueryNode nodeFromSubQuery;
            public final QueryNode transformedNode;
            public final Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitutionToPropagate;

            private AnalysisResults(QueryNode nodeFromSubQuery, QueryNode transformedNode,
                                    Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitutionToPropagate) {
                this.nodeFromSubQuery = nodeFromSubQuery;
                this.transformedNode = transformedNode;
                this.optionalSubstitutionToPropagate = optionalSubstitutionToPropagate;
            }
        }


        private final QueryNode nodeFromSubQuery;
        private final QueryNode transformedParent;
        private final QueryNode transformedNode;

        private final Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition;

        /**
         * Substitution to propagate to the children
         */
        private final Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToPropagate;

        private Transformation(IntermediateQuery query, QueryNode originalNode,
                               Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToApply,
                               HomogeneousQueryNodeTransformer renamer, QueryNode transformedParent,
                               Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition) {
            this.transformedParent = transformedParent;
            this.optionalPosition = optionalPosition;

            /**
             * May be recursive because some consecutive nodes in the sub-query may not be needed
             * (for instance construction nodes without any remaining binding)
             */
            AnalysisResults analysisResults = analyze(query, originalNode, substitutionToApply, renamer);
            this.nodeFromSubQuery = analysisResults.nodeFromSubQuery;
            this.transformedNode = analysisResults.transformedNode;
            this.substitutionToPropagate = analysisResults.optionalSubstitutionToPropagate;
        }

        /**
         * TODO: find a better name
         * Recursive
         */
        private static AnalysisResults analyze(
                IntermediateQuery query, QueryNode originalNode,
                Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToApply,
                HomogeneousQueryNodeTransformer renamer) {
            try {
                QueryNode renamedNode = originalNode.acceptNodeTransformer(renamer);

                if (substitutionToApply.isPresent()) {
                    SubstitutionResults<? extends QueryNode> results = renamedNode.applyDescendingSubstitution(
                            substitutionToApply.get(), query);

                    switch (results.getLocalAction()) {
                        case NO_CHANGE:
                            return new AnalysisResults(originalNode, renamedNode,
                                    results.getSubstitutionToPropagate());

                        case NEW_NODE:
                            QueryNode newNode = results.getOptionalNewNode().get();
                            if (newNode == originalNode) {
                                throw new IllegalStateException("NEW_NODE action must not return the same node. " +
                                        "Use NO_CHANGE instead.");
                            }
                            return new AnalysisResults(originalNode, newNode,
                                    results.getSubstitutionToPropagate());
                        /**
                         * Recursive
                         */
                        case REPLACE_BY_CHILD:
                            QueryNode replacingChild = results.getOptionalReplacingChildPosition()
                                    .flatMap(position -> query.getChild(originalNode, position))
                                    .orElseGet(() -> query.getFirstChild(originalNode)
                                            .orElseThrow(() -> new IllegalStateException("No replacing child is available")));
                            return analyze(query, replacingChild, results.getSubstitutionToPropagate(), renamer);

                        case INSERT_CONSTRUCTION_NODE:
                            throw new IllegalStateException("Construction node insertion not expected during query merging");

                        case DECLARE_AS_EMPTY:
                            return new AnalysisResults(originalNode,
                                    new EmptyNodeImpl(extractProjectedVariables(query, originalNode)),
                                    Optional.empty());
                        default:
                            throw new IllegalStateException("Unknown local action:" + results.getLocalAction());
                    }
                }
                else {
                    // Empty
                    return new AnalysisResults(originalNode, renamedNode, Optional.empty());
                }
            } catch (NotNeededNodeException e) {
                throw new IllegalStateException("Unexpected exception: " + e);
            }
        }

        public Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> getSubstitutionToPropagate() {
            return substitutionToPropagate;
        }

        public QueryNode getTransformedNode() {
            return transformedNode;
        }

        public QueryNode getNodeFromSubQuery() {
            return nodeFromSubQuery;
        }

        public QueryNode getTransformedParent() {
            return transformedParent;
        }

        public Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition() {
            return optionalPosition;
        }
    }


    /**
     * Main method
     */
    @Override
    public ProposalResults apply(QueryMergingProposal proposal, IntermediateQuery mainQuery,
                                 QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        IntermediateQuery subQuery = proposal.getSubQuery();

        List<IntensionalDataNode> localDataNodes = findIntensionalDataNodes(mainQuery, subQuery.getProjectionAtom());

        for (IntensionalDataNode localDataNode : localDataNodes) {
            mergeSubQuery(treeComponent, subQuery, localDataNode);
        }

        // Removes the empty nodes (in-place operation)
        RemoveEmptyNodesProposalImpl<ConstructionNode> cleaningProposal = new RemoveEmptyNodesProposalImpl<>(
                mainQuery.getRootConstructionNode());
        mainQuery.applyProposal(cleaningProposal, true);

        return new ProposalResultsImpl(mainQuery);
    }

    /**
     * Finds intensional data nodes that matches a data atom.
     */
    private ImmutableList<IntensionalDataNode> findIntensionalDataNodes(IntermediateQuery query,
                                                                        DataAtom subsumingDataAtom) {
        return query.getNodesInTopDownOrder().stream()
                .filter(n -> n instanceof IntensionalDataNode)
                .map(n -> (IntensionalDataNode)n)
                .filter(n -> subsumingDataAtom.hasSamePredicateAndArity(n.getProjectionAtom()))
                .collect(ImmutableCollectors.toList());
    }

    /**
     * TODO: explain
     *
     */
    protected static void mergeSubQuery(QueryTreeComponent treeComponent, IntermediateQuery subQuery,
                                        IntensionalDataNode intensionalDataNode) throws EmptyQueryException {
        insertSubQuery(treeComponent, subQuery, intensionalDataNode);
    }

    /**
     * TODO: explain
     */
    private static void insertSubQuery(final QueryTreeComponent treeComponent, final IntermediateQuery subQuery,
                                       final IntensionalDataNode intensionalDataNode) {

        /**
         * Gets the parent of the intensional node and remove the latter
         */
        QueryNode parentOfTheIntensionalNode = treeComponent.getParent(intensionalDataNode)
                .orElseThrow(()-> new IllegalStateException("Bug: the intensional does not have a parent"));
        Optional<NonCommutativeOperatorNode.ArgumentPosition> topOptionalPosition = treeComponent.getOptionalPosition(
                parentOfTheIntensionalNode, intensionalDataNode);
        treeComponent.removeSubTree(intensionalDataNode);


        VariableGenerator variableGenerator = new VariableGenerator(treeComponent.getKnownVariables());
        InjectiveVar2VarSubstitution renamingSubstitution = generateNotConflictingRenaming(variableGenerator,
                subQuery.getKnownVariables());

        HomogeneousQueryNodeTransformer renamer = createRenamer(renamingSubstitution);

        /**
         * Starting node: the root of the sub-query
         */
        ConstructionNode rootNode = subQuery.getRootConstructionNode();
        Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalTau = Optional.of(extractSubstitution(
                renamingSubstitution.applyToDistinctVariableOnlyDataAtom(subQuery.getProjectionAtom()),
                intensionalDataNode.getProjectionAtom()))
                .filter(s -> !s.isEmpty());

        Queue<Transformation> originalNodesToVisit = new LinkedList<>();
        originalNodesToVisit.add(new Transformation(subQuery, rootNode, optionalTau, renamer,
                parentOfTheIntensionalNode, topOptionalPosition));


        /**
         * TODO: explain
         */
        while(!originalNodesToVisit.isEmpty()) {
            Transformation transformation = originalNodesToVisit.poll();

            /**
             * The node to insert is not guaranteed not to be already present in the tree
             */
            QueryNode nodeToInsert = getNodeToInsert(transformation, treeComponent);

            /**
             * Adds the transformed node to the tree.
             */
            treeComponent.addChild(transformation.getTransformedParent(),
                    nodeToInsert,
                    transformation.getOptionalPosition(), false);

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToPropagate =
                    transformation.getSubstitutionToPropagate();

            /**
             * Puts the children into the queue except if the transformed node is unsatisfied
             */
            if (!(nodeToInsert instanceof EmptyNode)) {
                QueryNode originalNode = transformation.getNodeFromSubQuery();

                subQuery.getChildren(originalNode).stream()
                        .forEach(child ->
                                originalNodesToVisit.add(new Transformation(subQuery, child,
                                        substitutionToPropagate, renamer, nodeToInsert,
                                        subQuery.getOptionalPosition(originalNode, child)
                                )));
            }
        }
    }


    private static HomogeneousQueryNodeTransformer createRenamer(InjectiveVar2VarSubstitution renamingSubstitution) {
        if (renamingSubstitution.isEmpty()) {
            return new IdentityQueryNodeTransformer();
        }
        else {
            return new QueryNodeRenamer(renamingSubstitution);
        }
    }


    private static ImmutableSubstitution<VariableOrGroundTerm> extractSubstitution(DistinctVariableOnlyDataAtom sourceAtom,
                                                                                   DataAtom targetAtom) {
        if (!sourceAtom.getPredicate().equals(targetAtom.getPredicate())) {
            throw new IllegalStateException("Incompatible predicates");
        }
        else if (sourceAtom.getEffectiveArity() != targetAtom.getEffectiveArity()) {
            throw new IllegalStateException("Different arities");
        }

        ImmutableMap<Variable, VariableOrGroundTerm> newMap = FunctionalTools.zip(
                sourceAtom.getArguments(),
                (ImmutableList<VariableOrGroundTerm>) targetAtom.getArguments()).stream()
                .collect(ImmutableCollectors.toMap());

        return new ImmutableSubstitutionImpl<>(newMap);
    }

    /**
     * TODO: find a better name
     */
    private static QueryNode getNodeToInsert(Transformation transformation, QueryTreeComponent treeComponent) {
        QueryNode possiblyTransformedNode = transformation.getTransformedNode();

        return treeComponent.contains(possiblyTransformedNode)
                ? possiblyTransformedNode.clone()
                : possiblyTransformedNode;
    }

}
