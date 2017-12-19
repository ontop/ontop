package it.unibz.inf.ontop.iq.executor.merging;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.*;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.iq.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.iq.proposal.impl.RemoveEmptyNodeProposalImpl;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.FunctionalTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Singleton
public class QueryMergingExecutorImpl implements QueryMergingExecutor {

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

        private final Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition;

        /**
         * Substitution to propagate to the children
         */
        private final Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToPropagate;

        private Transformation(IntermediateQuery query, QueryNode originalNode,
                               Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToApply,
                               QueryNode transformedParent,
                               Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) {
            this.transformedParent = transformedParent;
            this.optionalPosition = optionalPosition;

            /**
             * May be recursive because some consecutive nodes in the sub-query may not be needed
             * (for instance construction nodes without any remaining binding)
             */
            AnalysisResults analysisResults = analyze(query, originalNode, substitutionToApply);
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
                Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> substitutionToApply) {

            if (substitutionToApply.isPresent()) {
                SubstitutionResults<? extends QueryNode> results = originalNode.applyDescendingSubstitution(
                        substitutionToApply.get(), query);

                switch (results.getLocalAction()) {
                    case NO_CHANGE:
                        return new AnalysisResults(originalNode, originalNode,
                                results.getSubstitutionToPropagate());

                    case NEW_NODE:
                        QueryNode newNode = results.getOptionalNewNode().get();
                        if (newNode == originalNode) {
                            throw new IllegalStateException("NEW_NODE action must not return the same node. " +
                                    "Use NO_CHANGE instead.");
                        }
                        return new AnalysisResults(originalNode, newNode,
                                results.getSubstitutionToPropagate());

                    case DECLARE_AS_TRUE:
                        return new AnalysisResults(originalNode, query.getFactory().createTrueNode(),
                                Optional.empty());
                    /**
                     * Recursive
                     */
                    case REPLACE_BY_CHILD:
                        QueryNode replacingChild = results.getOptionalReplacingChildPosition()
                                .flatMap(position -> query.getChild(originalNode, position))
                                .orElseGet(() -> query.getFirstChild(originalNode)
                                        .orElseThrow(() -> new IllegalStateException("No replacing child is available")));
                        return analyze(query, replacingChild, results.getSubstitutionToPropagate());

                    case INSERT_CONSTRUCTION_NODE:
                        throw new IllegalStateException("Construction node insertion not expected during query merging");

                    case DECLARE_AS_EMPTY:
                        return analyze(query, query.getFactory().createEmptyNode(query.getVariables(originalNode)),
                                substitutionToApply);

                    default:
                        throw new IllegalStateException("Unknown local action:" + results.getLocalAction());
                }
            }
            else {
                // Empty
                return new AnalysisResults(originalNode, originalNode, Optional.empty());
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

        public Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalPosition() {
            return optionalPosition;
        }
    }



    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final TermFactory termFactory;

    @Inject
    private QueryMergingExecutorImpl(IntermediateQueryFactory iqFactory,
                                     QueryTransformerFactory transformerFactory,
                                     SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }


    /**
     * Main method
     */
    @Override
    public ProposalResults apply(QueryMergingProposal proposal, IntermediateQuery mainQuery,
                                 QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        Optional<IntermediateQuery> optionalSubQuery = proposal.getSubQuery();
        if (optionalSubQuery.isPresent()) {
            mergeSubQuery(treeComponent, optionalSubQuery.get(), proposal.getIntensionalNode());
        }
        else {
            removeUnsatisfiedNode(treeComponent, proposal.getIntensionalNode());
        }

        // Non-final
        Optional<EmptyNode> nextEmptyNode = treeComponent.getEmptyNodes().stream()
                .findFirst();
        while (nextEmptyNode.isPresent()) {
            // Removes the empty nodes (in-place operation)
            RemoveEmptyNodeProposal cleaningProposal = new RemoveEmptyNodeProposalImpl(nextEmptyNode.get(), false);
            mainQuery.applyProposal(cleaningProposal);

            nextEmptyNode = treeComponent.getEmptyNodes().stream().findFirst();
        }

        return new ProposalResultsImpl();
    }

    private void removeUnsatisfiedNode(QueryTreeComponent treeComponent, IntensionalDataNode intensionalNode) {

        EmptyNode emptyNode = iqFactory.createEmptyNode(intensionalNode.getVariables());
        treeComponent.replaceSubTree(intensionalNode, emptyNode);
    }

    /**
     * TODO: explain
     *
     */
    protected void mergeSubQuery(QueryTreeComponent treeComponent, IntermediateQuery subQuery,
                                        IntensionalDataNode intensionalDataNode) {
        /**
         * Gets the parent of the intensional node and remove the latter
         */
        QueryNode parentOfTheIntensionalNode = treeComponent.getParent(intensionalDataNode)
                .orElseThrow(()-> new IllegalStateException("Bug: the intensional does not have a parent"));
        Optional<BinaryOrderedOperatorNode.ArgumentPosition> topOptionalPosition = treeComponent.getOptionalPosition(
                parentOfTheIntensionalNode, intensionalDataNode);
        treeComponent.removeSubTree(intensionalDataNode);


        VariableGenerator variableGenerator = new VariableGenerator(treeComponent.getKnownVariables(), termFactory);
        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(variableGenerator,
                subQuery.getKnownVariables());

        IntermediateQuery renamedSubQuery;
        if(renamingSubstitution.isEmpty()){
            renamedSubQuery = subQuery;
        } else {
            QueryRenamer queryRenamer = transformerFactory.createRenamer(renamingSubstitution);
            renamedSubQuery = queryRenamer.transform(subQuery);
        }

        /*
         * Starting node: the root of the sub-query
         */
        QueryNode rootNode = renamedSubQuery.getRootNode();
        Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalTau = Optional.of(extractSubstitution(
                renamingSubstitution.applyToDistinctVariableOnlyDataAtom(renamedSubQuery.getProjectionAtom()),
                intensionalDataNode.getProjectionAtom()))
                .filter(s -> !s.isEmpty());

        Queue<Transformation> originalNodesToVisit = new LinkedList<>();
        originalNodesToVisit.add(new Transformation(renamedSubQuery, rootNode, optionalTau, parentOfTheIntensionalNode,
                topOptionalPosition));


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

                renamedSubQuery.getChildren(originalNode)
                        .forEach(child ->
                                originalNodesToVisit.add(new Transformation(renamedSubQuery, child,
                                        substitutionToPropagate, nodeToInsert,
                                        renamedSubQuery.getOptionalPosition(originalNode, child)
                                )));
            }
        }
    }


    private ImmutableSubstitution<VariableOrGroundTerm> extractSubstitution(DistinctVariableOnlyDataAtom sourceAtom,
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

        return substitutionFactory.getSubstitution(newMap);
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