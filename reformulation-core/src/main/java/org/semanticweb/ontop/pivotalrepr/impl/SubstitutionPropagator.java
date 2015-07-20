package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.NonGroundFunctionalTermImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.SubQueryUnificationTools.ConstructionNodeUnification;

import static org.semanticweb.ontop.model.impl.GroundTermTools.isGroundTerm;

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
            ConstructionNodeUnification constructionNodeUnification = SubQueryUnificationTools.unifyConstructionNode(
                    constructionNode, newProjectionAtom);

            ConstructionNode newConstructionNode = constructionNodeUnification.getUnifiedNode();
            ImmutableSubstitution<VariableOrGroundTerm> newSubstitutionToPropagate =
                    constructionNodeUnification.getSubstitutionPropagator().getSubstitution();

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

    @Override
    public GroupNode transform(GroupNode groupNode) throws QueryNodeTransformationException, NotNeededNodeException {
        ImmutableList.Builder<NonGroundTerm> termBuilder = ImmutableList.builder();
        for (NonGroundTerm term : groupNode.getGroupingTerms()) {

            ImmutableTerm newTerm = substitution.apply(term);
            if (newTerm instanceof Variable) {
                termBuilder.add((Variable)newTerm);
            }
            /**
             * Functional term: adds it if remains a non-ground term.
             */
            else if (!isGroundTerm(newTerm)) {
                NonGroundFunctionalTerm functionalTerm = new NonGroundFunctionalTermImpl(
                        (ImmutableFunctionalTerm)newTerm);
                termBuilder.add(functionalTerm);
            }
        }

        ImmutableList<NonGroundTerm> newGroupingTerms = termBuilder.build();
        if (newGroupingTerms.isEmpty()) {
            throw new NotNeededNodeException("The group node is not needed anymore because no grouping term remains");
        }

        return new GroupNodeImpl(newGroupingTerms);
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
