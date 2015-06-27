package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: describe
 */
public class SubstitutionPropagator implements QueryNodeTransformer {

    /**
     * TODO: explain
     */
    public static class NewSubstitutionException extends QueryNodeTransformationException {
        private final ImmutableSubstitution<VariableOrGroundTerm> substitution;
        private final QueryNode transformedNode;

        public NewSubstitutionException(ImmutableSubstitution<VariableOrGroundTerm> substitution,
                                        QueryNode transformedNode) {
            super();
            this.substitution = substitution;
            this.transformedNode = transformedNode;
       }

        public ImmutableSubstitution<VariableOrGroundTerm> getSubstitution() {
            return substitution;
        }

        public QueryNode getTransformedNode() {
            return transformedNode;
        }
    }

    /**
     * TODO: explain
     */
    public static class UnificationException extends QueryNodeTransformationException {
        public UnificationException(String message) {
            super(message);
        }

    }


    private final ImmutableSubstitution<VariableOrGroundTerm> substitution;

    public SubstitutionPropagator(ImmutableSubstitution<VariableOrGroundTerm> substitution) {
        this.substitution = substitution;
    }

    public ImmutableSubstitution<VariableOrGroundTerm>  getSubstitution() {
        return substitution;
    }

    @Override
    public FilterNode transform(FilterNode filterNode){
        return new FilterNodeImpl(transformBooleanExpression(filterNode.getFilterCondition()));
    }

    @Override
    public TableNode transform(TableNode tableNode) {
        return new TableNodeImpl(transformDataAtom(tableNode.getAtom()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return new LeftJoinNodeImpl(
                transformOptionalBooleanExpression(leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        return unionNode.clone();
    }

    @Override
    public OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) {
        return new OrdinaryDataNodeImpl(transformDataAtom(ordinaryDataNode.getAtom()));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return new InnerJoinNodeImpl(
                transformOptionalBooleanExpression(innerJoinNode.getOptionalFilterCondition())
        );
    }

    /**
     * TODO: implement
     */
    @Override
    public ConstructionNode transform(ConstructionNode constructionNode)
            throws NewSubstitutionException, UnificationException {
        DataAtom newProjectionAtom = transformDataAtom(constructionNode.getProjectionAtom());

        try {
            /**
             * TODO: explain why it makes sense (interface)
             */
            P2<ConstructionNode, SubstitutionPropagator> unificationResults =
                    SubQueryUnificationTools.unifyConstructionNode(constructionNode, newProjectionAtom);

            ConstructionNode newConstructionNode = unificationResults._1();
            ImmutableSubstitution<VariableOrGroundTerm> newSubstitutionToPropagate =
                    unificationResults._2().getSubstitution();

            /**
             * If the substitution has changed, throws the new substitution
             * and the new construction node so that the "client" can continue
             * with the new substitution (for the children nodes).
             */
            if (!substitution.equals(newSubstitutionToPropagate)) {
                throw new NewSubstitutionException(newSubstitutionToPropagate,
                        newConstructionNode);
            }

            /**
             * Otherwise, continues with the current substitution
             */
            return newConstructionNode;

        } catch (SubQueryUnificationTools.SubQueryUnificationException e) {
            throw new UnificationException(e.getMessage());
        }
    }

    private ImmutableBooleanExpression transformBooleanExpression(ImmutableBooleanExpression booleanExpression) {
        return substitution.applyToBooleanExpression(booleanExpression);
    }

    private DataAtom transformDataAtom(DataAtom atom) {
        return substitution.applyToDataAtom(atom);
    }

    private Optional<ImmutableBooleanExpression> transformOptionalBooleanExpression(
            Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        if (optionalFilterCondition.isPresent()) {
            return Optional.of(transformBooleanExpression(optionalFilterCondition.get()));
        }
        return Optional.absent();
    }
}
