package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.*;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;

import javax.xml.crypto.Data;
import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfLeftJoinExecutor implements NodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode>
    apply(LeftJoinOptimizationProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        LeftJoinNode leftJoinNode = proposal.getFocusNode();

        QueryNode leftChild = query.getChild(leftJoinNode,LEFT).orElseThrow(() -> new IllegalStateException("The left child of a LJ is missing: " + leftJoinNode ));
        QueryNode rightChild = query.getChild(leftJoinNode,RIGHT).orElseThrow(() -> new IllegalStateException("The right child of a LJ is missing: " + leftJoinNode));

        if( leftChild instanceof DataNode && rightChild instanceof DataNode) {

            DataNode leftDataNode = (DataNode) leftChild;
            DataNode rightDataNode = (DataNode) rightChild;


//            ImmutableSet<Variable> variablesToKeep = query.getClosestConstructionNode(leftJoinNode).getVariables();
//            Optional<ConcreteProposal> optionalConcreteProposal = propose(leftDataNode, rightDataNode, variablesToKeep,
//                    query.getMetadata());
//
//            if (optionalConcreteProposal.isPresent()) {
//                ConcreteProposal concreteProposal = optionalConcreteProposal.get();
//
//                // SIDE-EFFECT on the tree component (and thus on the query)
//                return applyOptimization(query, treeComponent, highLevelProposal.getFocusNode(),
//                        concreteProposal);
//            }
        }

//        return new NodeCentricOptimizationResultsImpl<>(query, topJoinNode);


        throw new RuntimeException("TODO: implement it");
    }

//    private Optional<ConcreteProposal> propose(DataNode leftDataNode, DataNode rightDataNode,
//                                               ImmutableSet<Variable> variablesToKeep,
//                                               MetadataForQueryOptimization metadata) {
//
//        AtomPredicate leftPredicate = leftDataNode.getProjectionAtom().getPredicate();
//        AtomPredicate rightPredicate = rightDataNode.getProjectionAtom().getPredicate();
//
//        PredicateLevelProposal predicateProposal;
//        if(leftPredicate.equals(rightPredicate)) {
//            if (metadata.getUniqueConstraints().containsKey(leftPredicate)) {
//                predicateProposal = proposePerPredicate(leftDataNode, rightDataNode, metadata.getUniqueConstraints().get(leftPredicate));
//            }
//        }
//        else {
//            /**
//             * the left and the right predicates are different, so we
//             * check whether there are foreign key constraints
//             */
//        }
//
//
//        ImmutableList.Builder<PredicateLevelProposal> proposalListBuilder = ImmutableList.builder();
//
//        for (AtomPredicate predicate : initialDataNodeMap.keySet()) {
//            ImmutableCollection<DataNode> initialNodes = initialDataNodeMap.get(predicate);
//            PredicateLevelProposal predicateProposal;
//            if (primaryKeys.containsKey(predicate)) {
//                try {
//                    predicateProposal = proposePerPredicate(initialNodes, primaryKeys.get(predicate));
//                }
//                /**
//                 * Fall back to the default predicate proposal: doing nothing
//                 */
//                catch (AtomUnificationException e) {
//                    predicateProposal = new PredicateLevelProposal(initialNodes);
//                }
//            }
//            else {
//                predicateProposal = new PredicateLevelProposal(initialNodes);
//            }
//            proposalListBuilder.add(predicateProposal);
//        }
//
//        return createConcreteProposal(proposalListBuilder.build(), variablesToKeep);
//    }

}
