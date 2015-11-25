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
 * Abstract: does not define:
 *   - transform(ConstructionNode ...)
 *   - transform(LeftJoinNode ...)
 *
 */
public abstract class SubstitutionPropagator<T1 extends QueryNodeTransformationException, T2 extends QueryNodeTransformationException>
        implements HomogeneousQueryNodeTransformer<T1, T2> {


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
        return new ExtensionalDataNodeImpl(transformDataAtom(extensionalDataNode.getProjectionAtom()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        return unionNode.clone();
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return new IntensionalDataNodeImpl(transformDataAtom(intensionalDataNode.getProjectionAtom()));
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

    protected Optional<ImmutableBooleanExpression> transformOptionalBooleanExpression(
            Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        if (optionalFilterCondition.isPresent()) {
            return Optional.of(transformBooleanExpression(optionalFilterCondition.get()));
        }
        return Optional.absent();
    }
}
