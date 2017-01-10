package it.unibz.inf.ontop.executor.leftjoin;

import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.sql.*;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 *
 */
@Singleton
public class ForeignKeyLeftJoinExecutor implements SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    /**
     * This method assumes that all redundant IS_NOT_NULL predicates
     * that may appear in the joining condition have been removed.
     * By redundant, we mean that for a query
     *
     *      R(x,y) OPT Q(y,z) FILTER (IS_NOT_NULL(z))
     *
     * such that the second attribute of Q is not nullable,
     * the atom IS_NOT_NULL(z) is redundant, and therefore could be removed.
     */
    @Override
    public NodeCentricOptimizationResults<LeftJoinNode>
    apply(LeftJoinOptimizationProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

         LeftJoinNode leftJoinNode = proposal.getFocusNode();

        /**
         * We possibly optimize only if there is no filter condition
         */
        if(!leftJoinNode.getOptionalFilterCondition().isPresent()) {


            QueryNode leftChild = query.getChild(leftJoinNode, LEFT).orElseThrow(() -> new IllegalStateException("The left child of a LJ is missing: " + leftJoinNode));
            QueryNode rightChild = query.getChild(leftJoinNode, RIGHT).orElseThrow(() -> new IllegalStateException("The right child of a LJ is missing: " + leftJoinNode));


            if (leftChild instanceof DataNode && rightChild instanceof DataNode) {

                DataNode leftDataNode = (DataNode) leftChild;
                DataNode rightDataNode = (DataNode) rightChild;

                boolean replaceLeftJoinByInnerJoin = propose(leftDataNode, rightDataNode, query.getMetadata());
                if (replaceLeftJoinByInnerJoin) {

                    /**
                     * In this case we only change left join to inner join.
                     * We do not remove/modify the data nodes
                     */
                    return replaceLeftJoinByInnerJoin(query, treeComponent, leftJoinNode);
                }
            }
        }

        // No optimization
        return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
    }

    /**
     *  Assumes that LeftJoin has only two children, one left and one right.
     *
     *  Returns a proposal for optimization.
     */
    private boolean propose(DataNode leftDataNode, DataNode rightDataNode,
                            MetadataForQueryOptimization metadata) {

        AtomPredicate leftPredicate = leftDataNode.getProjectionAtom().getPredicate();
        AtomPredicate rightPredicate = rightDataNode.getProjectionAtom().getPredicate();

        /**
         * we check whether there are foreign key constraints
         */

        DatabaseRelationDefinition leftPredicateDatabaseRelation = getDatabaseRelation(metadata.getDBMetadata(), leftPredicate);
        DatabaseRelationDefinition rightPredicateDatabaseRelation = getDatabaseRelation(metadata.getDBMetadata(), rightPredicate);


        if(leftPredicateDatabaseRelation != null && rightPredicateDatabaseRelation != null) {
            return checkIfReplaceLeftJoinByInnerJoin(leftDataNode, rightDataNode,
                    leftPredicateDatabaseRelation, rightPredicateDatabaseRelation);
        }

        return false;
    }

    private boolean checkIfReplaceLeftJoinByInnerJoin(DataNode leftDataNode,
                                                      DataNode rightDataNode,
                                                      DatabaseRelationDefinition leftPredicateDatabaseRelation,
                                                      DatabaseRelationDefinition rightPredicateDatabaseRelation) {
        for( ForeignKeyConstraint foreignKey: leftPredicateDatabaseRelation.getForeignKeys() ) {

            /**
             * There could be multiple foreign key constraints for each referenced relation
             */
            if(rightPredicateDatabaseRelation.equals(foreignKey.getReferencedRelation())) {

                int joiningReferencedTermsCount = 0;
                for(ForeignKeyConstraint.Component component: foreignKey.getComponents()) {

                    Attribute childAttribute = component.getAttribute();
                    Attribute referencedAttribute = component.getReference();

                    VariableOrGroundTerm leftTerm = leftDataNode.getProjectionAtom().getTerm(childAttribute.getIndex() - 1);
                    VariableOrGroundTerm rightTerm = rightDataNode.getProjectionAtom().getTerm(referencedAttribute.getIndex() - 1);
                    if(leftTerm.equals(rightTerm)) {
                        joiningReferencedTermsCount++;
                    }
                }


                if(joiningReferencedTermsCount == foreignKey.getComponents().size()) {
                    return true;
                }
            }
        }
        return false;
    }

    private DatabaseRelationDefinition getDatabaseRelation(DBMetadata dbMetadata, AtomPredicate predicate) {

        RelationID relationId = Relation2DatalogPredicate.createRelationFromPredicateName(
                dbMetadata.getQuotedIDFactory(),
                predicate);

        return dbMetadata.getDatabaseRelation(relationId);
    }


    private NodeCentricOptimizationResults<LeftJoinNode> replaceLeftJoinByInnerJoin(IntermediateQuery query,
                                                                                    QueryTreeComponent treeComponent,
                                                                                    LeftJoinNode leftJoinNode) {
        /**
         * We do not copy over the optional filter condition in leftJoinNode
         * as we only replace left join by inner join if the filter condition
         * is not present.
         */
        InnerJoinNode newTopNode = new InnerJoinNodeImpl(Optional.empty());
        treeComponent.replaceNode(leftJoinNode, newTopNode);

        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(newTopNode));
    }


}
