package org.semanticweb.ontop.pivotalrepr.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.NonGroundFunctionalTermImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.*;

import static org.semanticweb.ontop.model.impl.GroundTermTools.isGroundTerm;

/**
 * TODO: describe
 *
 * Abstract: does not define transform(ConstructionNode ...).
 *
 */
public abstract class SubstitutionPropagator
        extends HomogeneousQueryNodeTransformerImpl<SubstitutionPropagator.UnificationException,
        SubstitutionPropagator.NewSubstitutionException> {

    /**
     * TODO: explain
     */
    public static class NewSubstitutionException extends QueryNodeTransformationException {
        private final ImmutableSubstitution<VariableOrGroundTerm> substitution;
        private final QueryNode transformedNode;

        protected NewSubstitutionException(ImmutableSubstitution<VariableOrGroundTerm> substitution,
                                           QueryNode transformedNode) {
            super("New substitution to propagate (" + substitution + ") and new node (" + transformedNode + ")");
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
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return new ExtensionalDataNodeImpl(transformDataAtom(extensionalDataNode.getAtom()));
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
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return new IntensionalDataNodeImpl(transformDataAtom(intensionalDataNode.getAtom()));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return new InnerJoinNodeImpl(
                transformOptionalBooleanExpression(innerJoinNode.getOptionalFilterCondition())
        );
    }

    @Override
    public GroupNode transform(GroupNode groupNode) throws NotNeededNodeException {
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

    protected DataAtom transformDataAtom(DataAtom atom) {
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
