package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
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
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 *
 */
public class ForeignKeyLeftJoinExecutor implements SimpleNodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

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
            Optional<ImmutableExpression> rightFilterCondition = leftJoinNode.getOptionalFilterCondition();

            boolean replaceLeftJoinByInnerJoin = propose(leftDataNode, rightDataNode, rightFilterCondition, variablesToKeep,
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
                            Optional<ImmutableExpression> rightFilterCondition,
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
                    rightFilterCondition, variablesToKeep);

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
                                                      Optional<ImmutableExpression> rightFilterCondition,
                                                      ImmutableSet<Variable> variablesToKeep) {
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

                    if (rightFilterConditionCanBeIgnored(rightFilterCondition, rightDataNode, rightPredicateDatabaseRelation, variablesToKeep) ){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean rightFilterConditionCanBeIgnored(Optional<ImmutableExpression> rightFilterCondition,
                                                     DataNode rightDataNode,
                                                     DatabaseRelationDefinition rightPredicateDatabaseRelation,
                                                     ImmutableSet<Variable> variablesToKeep) {

        if(rightFilterCondition.isPresent()) {

            boolean allIsNotNullAtoms = true;
            Set<Variable> notNullVariables = new HashSet<>();
            for(ImmutableExpression conjunct: rightFilterCondition.get().flattenAND()) {
                if(! conjunct.getFunctionSymbol().equals(ExpressionOperation.IS_NOT_NULL))
                {
                    allIsNotNullAtoms = false;
                    break;
                } else {
                    notNullVariables.addAll(conjunct.getVariables());
                }
            }

            ImmutableSet<Variable> projectedNotNullVariables = Sets.intersection(variablesToKeep, notNullVariables).immutableCopy();
            if(allIsNotNullAtoms) {
                return projectedVariablesAreNotNullable(rightDataNode, rightPredicateDatabaseRelation, projectedNotNullVariables);
            } else {
                return false;
            }

        } else {
            return true;
        }
    }

    private boolean projectedVariablesAreNotNullable(DataNode rightDataNode,
                                                     DatabaseRelationDefinition rightPredicateDatabaseRelation,
                                                     ImmutableSet<Variable> projectedNotNullVariables) {
        ImmutableSet<Variable> rightProjectedNotNullVariables = Sets.intersection(rightDataNode.getVariables(), projectedNotNullVariables).immutableCopy();

        Set<Variable> notNullableVariables = new HashSet<>();
        DataAtom dataAtom = rightDataNode.getProjectionAtom();
        for(Attribute attr: rightPredicateDatabaseRelation.getAttributes()) {
            VariableOrGroundTerm term = dataAtom.getTerm(attr.getIndex() - 1);
            if(rightProjectedNotNullVariables.contains(term) && !attr.canNull()) {
                notNullableVariables.add((Variable) term);
            }
        }

        if(notNullableVariables.containsAll(rightProjectedNotNullVariables)) {
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
        /**
         * We do not copy over the optional filter condition in leftJoinNode
         * as we only replace left join by inner join if the present filter condition
         * can be ignored. Otherwise, keeping the original filter condition is not sound.
         */
        InnerJoinNode newTopNode = new InnerJoinNodeImpl(Optional.empty());
        treeComponent.replaceNode(leftJoinNode, newTopNode);

        return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(newTopNode));
    }


}
