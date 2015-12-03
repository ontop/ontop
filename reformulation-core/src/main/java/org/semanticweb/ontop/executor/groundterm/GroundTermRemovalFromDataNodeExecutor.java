package org.semanticweb.ontop.executor.groundterm;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.ImmutabilityTools;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.impl.*;
import org.semanticweb.ontop.pivotalrepr.proposal.GroundTermRemovalFromDataNodeProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;

import java.util.Collection;
import java.util.Map;

/**
 * TODO: explain
 */
public class GroundTermRemovalFromDataNodeExecutor implements
        InternalProposalExecutor<GroundTermRemovalFromDataNodeProposal, ProposalResults> {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

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

    /**
     * TODO: explain
     */
    @Override
    public ProposalResults apply(GroundTermRemovalFromDataNodeProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException {

        ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> receivingNodes = processDataNodes(
                proposal.getDataNodesToSimplify(), query, treeComponent);

        processJoinOrFilterNodes(receivingNodes, treeComponent);

        return new ProposalResultsImpl(query);
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
                ImmutableBooleanExpression joiningCondition = convertIntoBooleanExpression(pairExtraction.pairs);
                FilterNode newFilterNode = new FilterNodeImpl(joiningCondition);
                try {
                    treeComponent.insertParent(dataNode, newFilterNode);
                } catch (IllegalTreeUpdateException e) {
                    throw new RuntimeException("Unexpected exception: " + e);
                }
            }
        }

        return multimapBuilder.build();
    }

    private ImmutableBooleanExpression convertIntoBooleanExpression(Collection<VariableGroundTermPair> pairs) {
        ImmutableList.Builder<ImmutableBooleanExpression> booleanExpressionBuilder = ImmutableList.builder();

        for (VariableGroundTermPair pair : pairs ) {
            booleanExpressionBuilder.add(DATA_FACTORY.getImmutableBooleanExpression(
                    OBDAVocabulary.EQ, pair.variable, pair.groundTerm));
        }
        Optional<ImmutableBooleanExpression> optionalFoldExpression = ImmutabilityTools.foldBooleanExpressions(
                booleanExpressionBuilder.build());
        return optionalFoldExpression.get();
    }

    private PairExtraction extractPairs(DataNode dataNode, IntermediateQuery query)
            throws InvalidQueryOptimizationProposalException {
        ImmutableList.Builder<VariableGroundTermPair> pairBuilder = ImmutableList.builder();
        ImmutableList.Builder<VariableOrGroundTerm> newArgumentBuilder = ImmutableList.builder();

        for (VariableOrGroundTerm argument : dataNode.getProjectionAtom().getVariablesOrGroundTerms()) {
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
        DataAtom dataAtom = DATA_FACTORY.getDataAtom(formerDataNode.getProjectionAtom().getPredicate(), arguments);
        if (formerDataNode instanceof ExtensionalDataNode) {
            return new ExtensionalDataNodeImpl(dataAtom);
        }
        else if (formerDataNode instanceof IntensionalDataNode) {
            return new IntensionalDataNodeImpl(dataAtom);
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

            if ((ancestor instanceof InnerJoinNode )
                    || (ancestor instanceof FilterNode)) {
                return Optional.of((JoinOrFilterNode) ancestor);
            }
            else if (ancestor instanceof LeftJoinNode) {
                Optional<ArgumentPosition> optionalPosition = query.getOptionalPosition(ancestor, ancestorChild);
                if (optionalPosition.isPresent()) {
                    /**
                     * TODO: explain
                     */
                    switch (optionalPosition.get()) {
                        case LEFT:
                            break;
                        case RIGHT:
                            return Optional.of((JoinOrFilterNode)ancestor);

                    }
                }
                else {
                    throw new IllegalStateException("Inconsistent tree: a LJ without positions for its children found");
                }
            }
            else if (ancestor instanceof SubTreeDelimiterNode) {
                return Optional.absent();
            }

            /**
             * By default: continues
             */
            ancestorChild = ancestor;
            optionalAncestor = query.getParent(ancestor);
        }
        return Optional.absent();
    }

    /**
     * TODO: explain
     */
    private void processJoinOrFilterNodes(ImmutableMultimap<JoinOrFilterNode, VariableGroundTermPair> receivingNodes,
                                          QueryTreeComponent treeComponent) {

        for (Map.Entry<JoinOrFilterNode, Collection<VariableGroundTermPair>> entry : receivingNodes.asMap().entrySet()) {
            JoinOrFilterNode formerNode = entry.getKey();
            ImmutableBooleanExpression newAdditionalExpression = convertIntoBooleanExpression(entry.getValue());

            Optional<ImmutableBooleanExpression> optionalFormerExpression = formerNode.getOptionalFilterCondition();
            ImmutableBooleanExpression newExpression;
            if (optionalFormerExpression.isPresent()) {
                ImmutableBooleanExpression formerExpression = optionalFormerExpression.get();
                newExpression = ImmutabilityTools.foldBooleanExpressions(
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
                                                         ImmutableBooleanExpression newExpression) {
        if (formerNode instanceof FilterNode) {
            return new FilterNodeImpl(newExpression);
        }
        else if (formerNode instanceof InnerJoinNode) {
            return new InnerJoinNodeImpl(Optional.of(newExpression));
        }
        else if (formerNode instanceof LeftJoinNode) {
            return new LeftJoinNodeImpl(Optional.of(newExpression));
        }
        else {
            throw new RuntimeException("This type of query node is not supported: " + formerNode.getClass());
        }
    }
}
