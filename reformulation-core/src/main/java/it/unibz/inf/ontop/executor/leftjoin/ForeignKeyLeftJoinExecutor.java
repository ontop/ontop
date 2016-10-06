package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.executor.join.SelfJoinLikeExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.GroundExpressionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.sql.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 *
 */
public class ForeignKeyLeftJoinExecutor implements NodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    protected static class ConcreteProposal {
        private final boolean replaceLeftJoinByInnerJoin;

        public ConcreteProposal(boolean replaceLeftJoinByInnerJoin) {
            this.replaceLeftJoinByInnerJoin = replaceLeftJoinByInnerJoin;
        }

        public boolean getReplaceLeftJoinByInnerJoin() {
            return replaceLeftJoinByInnerJoin;
        }

    }

        @Override
    public NodeCentricOptimizationResults<LeftJoinNode>
    apply(LeftJoinOptimizationProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        LeftJoinNode leftJoinNode = proposal.getFocusNode();

        QueryNode leftChild = query.getChild(leftJoinNode,LEFT).orElseThrow(() -> new IllegalStateException("The left child of a LJ is missing: " + leftJoinNode ));
        QueryNode rightChild = query.getChild(leftJoinNode,RIGHT).orElseThrow(() -> new IllegalStateException("The right child of a LJ is missing: " + leftJoinNode));


        if (leftChild instanceof DataNode && rightChild instanceof DataNode) {

            DataNode leftDataNode = (DataNode) leftChild;
            DataNode rightDataNode = (DataNode) rightChild;

            // TODO: explain
            ImmutableSet<Variable> variablesToKeep = query.getClosestConstructionNode(leftJoinNode).getVariables();
            Optional<ImmutableExpression> filterCondition = leftJoinNode.getOptionalFilterCondition();

            boolean replaceLeftJoinByInnerJoin = propose(leftDataNode, rightDataNode, filterCondition, variablesToKeep,
                    query.getMetadata());

            if (replaceLeftJoinByInnerJoin) {

                /**
                 * In this case we only change left join to inner join.
                 * We do not remove/modify the data nodes
                 */
                return replaceLeftJoinByInnerJoin(query, treeComponent, leftJoinNode);
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
                            Optional<ImmutableExpression> filterCondition,
                            ImmutableSet<Variable> variablesToKeep,
                            MetadataForQueryOptimization metadata) {

        AtomPredicate leftPredicate = leftDataNode.getProjectionAtom().getPredicate();
        AtomPredicate rightPredicate = rightDataNode.getProjectionAtom().getPredicate();

        /**
         * the left and the right predicates are different, so we
         * check whether there are foreign key constraints
         */

        DatabaseRelationDefinition leftPredicateDatabaseRelation = getDatabaseRelationByName(metadata.getDBMetadata(), leftPredicate.getName());
        DatabaseRelationDefinition rightPredicateDatabaseRelation = getDatabaseRelationByName(metadata.getDBMetadata(), rightPredicate.getName());


        if(leftPredicateDatabaseRelation != null && rightPredicateDatabaseRelation != null) {
            return checkIfReplaceLeftJoinByInnerJoin(leftDataNode, rightDataNode,
                    leftPredicateDatabaseRelation, rightPredicateDatabaseRelation,
                    filterCondition, variablesToKeep);

            /**
             * TODO: check that there is no crazy joining condition,
             * e.g., give me the father's name, but only if it is different from the child's name.
             */
        }

        return false;
    }

    private boolean checkIfReplaceLeftJoinByInnerJoin(DataNode leftDataNode,
                                                      DataNode rightDataNode,
                                                      DatabaseRelationDefinition leftPredicateDatabaseRelation,
                                                      DatabaseRelationDefinition rightPredicateDatabaseRelation,
                                                      Optional<ImmutableExpression> filterCondition,
                                                      ImmutableSet<Variable> variablesToKeep) {
        for( ForeignKeyConstraint foreignKey: leftPredicateDatabaseRelation.getForeignKeys() ) {

            /**
             * There could be multiple foreign key constraints for each referenced relation
             */
            if(rightPredicateDatabaseRelation.equals(foreignKey.getReferencedRelation())) {

                Set<VariableOrGroundTerm> foreignKeyReferencedRightTerms = new HashSet<>();
                int joiningReferencedTermsCount = 0;
                for(ForeignKeyConstraint.Component component: foreignKey.getComponents()) {

                    Attribute childAttribute = component.getAttribute();
                    Attribute referencedAttribute = component.getReference();

                    VariableOrGroundTerm leftTerm = leftDataNode.getProjectionAtom().getTerm(childAttribute.getIndex() - 1);
                    VariableOrGroundTerm rightTerm = rightDataNode.getProjectionAtom().getTerm(referencedAttribute.getIndex() - 1);
                    if(leftTerm.equals(rightTerm)) {
                        foreignKeyReferencedRightTerms.add(rightTerm);
                        joiningReferencedTermsCount++;
                    }
                }


                // TODO: continue and check the logic
                // check joining condition
                // not null
                if(joiningReferencedTermsCount == foreignKey.getComponents().size()) {

                    if(projectedVariablesAreNotNullable(rightDataNode, rightPredicateDatabaseRelation, variablesToKeep)) {
                        return true;
                    }
                    else if (doesNotContradictJoiningCondition(filterCondition)){
                        return true;
                    }
                }
                throw new RuntimeException("TODO: implement");
            }
        }
        return false;
    }

    private boolean doesNotContradictJoiningCondition(Optional<ImmutableExpression> filterCondition) {
        if(filterCondition.isPresent()) {

            ImmutableExpression actualFilterCondition = filterCondition.get();
            ImmutableSet<ImmutableExpression> conjunctionOfConditions = actualFilterCondition.flattenAND();
            for (ImmutableExpression atom : conjunctionOfConditions) {
                if(atom instanceof GroundExpressionImpl) {

                }
            }
            throw new RuntimeException("TODO: continue");
        } else {
            return true;
        }
    }

    private boolean projectedVariablesAreNotNullable(DataNode rightDataNode,
                                                     DatabaseRelationDefinition rightPredicateDatabaseRelation,
                                                     ImmutableSet<Variable> variablesToKeep) {
        ImmutableSet<Variable> projectedVariables = Sets.intersection(rightDataNode.getVariables(), variablesToKeep).immutableCopy();

        Set<Variable> notNullableVariables = new HashSet<>();
        DataAtom dataAtom = rightDataNode.getProjectionAtom();
        for(Attribute attr: rightPredicateDatabaseRelation.getAttributes()) {
            VariableOrGroundTerm term = dataAtom.getTerm(attr.getIndex() - 1);
            if(projectedVariables.contains(term) && !attr.canNull()) {
                notNullableVariables.add((Variable) term);
            }
        }

        if(notNullableVariables.containsAll(projectedVariables)) {
            return true;
        }
        else {
            return false;
        }
    }

    private DatabaseRelationDefinition getDatabaseRelationByName(DBMetadata dbMetadata, String name) {
        for(DatabaseRelationDefinition relation: dbMetadata.getDatabaseRelations()) {
            if(relation.getID().getTableName().equalsIgnoreCase(name)) {
                return relation;
            }
        }
        return null;
    }


    private NodeCentricOptimizationResults<LeftJoinNode> replaceLeftJoinByInnerJoin(IntermediateQuery query,
                                                                                    QueryTreeComponent treeComponent,
                                                                                    LeftJoinNode leftJoinNode) {
        InnerJoinNode newTopNode = new InnerJoinNodeImpl(leftJoinNode.getOptionalFilterCondition());
        treeComponent.replaceNode(leftJoinNode, newTopNode);

        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(newTopNode));
    }


}
