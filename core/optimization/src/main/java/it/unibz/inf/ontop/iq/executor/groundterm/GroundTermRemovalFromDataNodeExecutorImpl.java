package it.unibz.inf.ontop.iq.executor.groundterm;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.GroundTermRemovalFromDataNodeProposal;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;

import java.util.Collection;
import java.util.Map;

/**
 * Turns constant values from a data node d into explicit equalities.
 * e.g. if d = A(5, x, 6),
 * transforms d into d' = A(y1, x , y2),
 * and generates the equalities y1 = 5 and y2 = 6.
 *
 * Then finds or creates a recipient node r in the query for these equalities.
 * By default, r is the first candidate recipient encountered by searching the query upwards from d',
 * up to the first construction or union node.
 * r can only be a filter node, an inner join, or a left join encountered from its right subtree.
 *
 * If such recipient cannot be found in the query,
 * r is a fresh filter node.
 * r is inserted as the child of the first ancestor of d' which is not a left join node accessed from the left
 * (bottom up)
 */
@Singleton
public class GroundTermRemovalFromDataNodeExecutorImpl implements
        GroundTermRemovalFromDataNodeExecutor {

    private static class VariableGroundTermPair {
        public final Variable variable;
        public final GroundTerm groundTerm;

        private VariableGroundTermPair(Variable variable, GroundTerm groundTerm) {
            this.variable = variable;
            this.groundTerm = groundTerm;
        }
    }

    private static class PairExtraction {
        private final ImmutableList<VariableGroundTermPair> pairs;
        private final DataNode newDataNode;


        private PairExtraction(ImmutableList<VariableGroundTermPair> pairs, DataNode newDataNode) {
            this.pairs = pairs;
            this.newDataNode = newDataNode;
        }
    }


    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private GroundTermRemovalFromDataNodeExecutorImpl(IntermediateQueryFactory iqFactory,
                                                      AtomFactory atomFactory, TermFactory termFactory,
                                                      ImmutabilityTools immutabilityTools) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
    }

    /**
     * TODO: explain
     */
    @Override
    public ProposalResults apply(GroundTermRemovalFromDataNodeProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException {

        ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> receivingNodes = processDataNodes(
                proposal.getDataNodesToSimplify(), query, treeComponent);

        processJoinOrFilterNodes(receivingNodes, treeComponent);

        return new ProposalResultsImpl();
    }

    /**
     * TODO: explain
     */
    private ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> processDataNodes(
            ImmutableList<DataNode> dataNodesToSimplify, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        ImmutableMultimap.Builder<JoinOrFilterNode, VariableGroundTermPair> multimapBuilder = ImmutableMultimap.builder();

        for (DataNode dataNode : dataNodesToSimplify) {
            Optional<JoinOrFilterNode> optionalReceivingNode = findClosestJoinOrFilterNode(query, dataNode);
            PairExtraction pairExtraction = extractPairs(dataNode, query);

            // Replaces the data node by another one without ground term
            treeComponent.replaceNode(dataNode, pairExtraction.newDataNode);

            if (optionalReceivingNode.isPresent()) {
                for (VariableGroundTermPair pair : pairExtraction.pairs) {
                    multimapBuilder.put(optionalReceivingNode.get(), pair);
                }
            }
            /**
             * TODO: explain
             */
            else {
                ImmutableExpression joiningCondition = convertIntoBooleanExpression(pairExtraction.pairs);
                FilterNode newFilterNode = iqFactory.createFilterNode(joiningCondition);
                treeComponent.insertParent(getRecipientChild(pairExtraction.newDataNode, query), newFilterNode);
            }
        }

        return multimapBuilder.build();
    }

    /** Recursive */
    private QueryNode getRecipientChild(QueryNode focusNode, IntermediateQuery query) {
        QueryNode parent = query.getParent(focusNode)
                .orElseThrow(() -> new InvalidIntermediateQueryException("Node "+focusNode+" has no parent"));
        if(parent instanceof LeftJoinNode) {
            BinaryOrderedOperatorNode.ArgumentPosition position = query.getOptionalPosition(parent, focusNode)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("No position for left join child " + focusNode));
            if (position.equals(BinaryOrderedOperatorNode.ArgumentPosition.LEFT)) {
                return getRecipientChild(parent, query);
            }
        }
        return focusNode;
    }

    private ImmutableExpression convertIntoBooleanExpression(Collection<VariableGroundTermPair> pairs) {
        ImmutableList.Builder<ImmutableExpression> booleanExpressionBuilder = ImmutableList.builder();

        for (VariableGroundTermPair pair : pairs ) {
            booleanExpressionBuilder.add(termFactory.getImmutableExpression(
                    ExpressionOperation.EQ, pair.variable, pair.groundTerm));
        }
        Optional<ImmutableExpression> optionalFoldExpression = immutabilityTools.foldBooleanExpressions(
                booleanExpressionBuilder.build());
        return optionalFoldExpression.get();
    }

    private PairExtraction extractPairs(DataNode dataNode, IntermediateQuery query)
            throws InvalidQueryOptimizationProposalException {
        ImmutableList.Builder<VariableGroundTermPair> pairBuilder = ImmutableList.builder();
        ImmutableList.Builder<VariableOrGroundTerm> newArgumentBuilder = ImmutableList.builder();

        for (VariableOrGroundTerm argument : dataNode.getProjectionAtom().getArguments()) {
            if (argument.isGround()) {
                Variable newVariable = query.generateNewVariable();
                pairBuilder.add(new VariableGroundTermPair(newVariable, (GroundTerm) argument));
                newArgumentBuilder.add(newVariable);
            }
            /**
             * Variable
             */
            else {
                newArgumentBuilder.add(argument);
            }
        }

        ImmutableList<VariableGroundTermPair> pairs = pairBuilder.build();
        if (pairs.isEmpty()) {
            throw new InvalidQueryOptimizationProposalException("The data node " + dataNode + " does not have" +
                    "ground terms");
        }
        else {
            DataNode newDataNode = generateDataNode(dataNode, newArgumentBuilder.build());
            return new PairExtraction(pairs, newDataNode);
        }
    }

    protected DataNode generateDataNode(DataNode formerDataNode, ImmutableList<VariableOrGroundTerm> arguments) {
        DataAtom dataAtom = atomFactory.getDataAtom(formerDataNode.getProjectionAtom().getPredicate(), arguments);
        if (formerDataNode instanceof ExtensionalDataNode) {
            return iqFactory.createExtensionalDataNode(dataAtom);
        }
        else if (formerDataNode instanceof IntensionalDataNode) {
            return iqFactory.createIntensionalDataNode(dataAtom);
        }
        else {
            throw new RuntimeException("Transformation of a data node of type "
                    + formerDataNode.getClass() + " is not supported yet");
        }
    }

    /**
     * TODO: explain
     */
    private Optional<JoinOrFilterNode> findClosestJoinOrFilterNode(IntermediateQuery query, DataNode dataNode) {

        // Non-final
        Optional<QueryNode> optionalAncestor = query.getParent(dataNode);

        // Non-final
        QueryNode ancestorChild = dataNode;

        while (optionalAncestor.isPresent()) {
            QueryNode ancestor = optionalAncestor.get();

            if ((ancestor instanceof CommutativeJoinNode)
                    || (ancestor instanceof FilterNode)) {
                return Optional.of((JoinOrFilterNode) ancestor);
            }
            else if (ancestor instanceof LeftJoinNode) {
                Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition = query.getOptionalPosition(ancestor, ancestorChild);
                if (optionalPosition.isPresent()) {
                    /**
                     * TODO: explain
                     */
                    switch (optionalPosition.get()) {
                        case RIGHT:
                            return Optional.of((JoinOrFilterNode)ancestor);
                            /* continue searching upwards */
                        case LEFT:;
                    }
                }
                else {
                    throw new IllegalStateException("Inconsistent tree: a LJ without positions for its children found");
                }
            }
            // TODO: consider that would allow lifting
            /*
             * By default: stops
             *
             * Ok for ConstructionNode and UnionNode
             */
            else {
                return Optional.empty();
            }

            ancestorChild = ancestor;
            optionalAncestor = query.getParent(ancestor);
        }
        return Optional.empty();
    }

    /**
     * TODO: explain
     */
    private void processJoinOrFilterNodes(ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> receivingNodes,
                                          QueryTreeComponent treeComponent) {

        for (Map.Entry<JoinOrFilterNode, Collection<VariableGroundTermPair>> entry : receivingNodes.asMap().entrySet()) {
            JoinOrFilterNode formerNode = entry.getKey();
            ImmutableExpression newAdditionalExpression = convertIntoBooleanExpression(entry.getValue());

            Optional<ImmutableExpression> optionalFormerExpression = formerNode.getOptionalFilterCondition();
            ImmutableExpression newExpression;
            if (optionalFormerExpression.isPresent()) {
                ImmutableExpression formerExpression = optionalFormerExpression.get();
                newExpression = immutabilityTools.foldBooleanExpressions(
                        ImmutableList.of(formerExpression, newAdditionalExpression))
                        .get();
            }
            else {
                newExpression = newAdditionalExpression;
            }

            JoinOrFilterNode newNode = generateNewJoinOrFilterNode(formerNode, newExpression);

            treeComponent.replaceNode(formerNode, newNode);
        }
    }

    protected JoinOrFilterNode generateNewJoinOrFilterNode(JoinOrFilterNode formerNode,
                                                         ImmutableExpression newExpression) {
        if (formerNode instanceof FilterNode) {
            return ((FilterNode)formerNode).changeFilterCondition(newExpression);
        }
        else if (formerNode instanceof JoinLikeNode) {
            return ((JoinLikeNode)formerNode).changeOptionalFilterCondition(Optional.of(newExpression));
        }
        else {
            throw new RuntimeException("This type of query node is not supported: " + formerNode.getClass());
        }
    }
}
