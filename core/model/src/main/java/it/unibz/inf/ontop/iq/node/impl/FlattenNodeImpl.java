package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class FlattenNodeImpl<N extends FlattenNode> extends CompositeQueryNodeImpl implements FlattenNode<N> {

    protected final Variable arrayVariable;
    protected final int arrayIndexIndex;
    protected final DataAtom<RelationPredicate> dataAtom;
    // True iff the argument is nullable (regardless of implicit equalities)
    protected final ImmutableList<Boolean> argumentNullability;

    protected FlattenNodeImpl(Variable arrayVariable, int arrayIndexIndex, DataAtom<RelationPredicate> dataAtom,
                              ImmutableList<Boolean> argumentNullability, SubstitutionFactory substitutionFactory,
                              IntermediateQueryFactory intermediateQueryFactory) {
        super(substitutionFactory, intermediateQueryFactory);
        this.arrayVariable = arrayVariable;
        this.arrayIndexIndex = arrayIndexIndex;
        this.dataAtom = dataAtom;

        if ((arrayIndexIndex >= dataAtom.getArguments().size())
                || arrayIndexIndex < 0)
            throw new IllegalArgumentException("The array index index must correspond to an argument of the data atom");
        this.argumentNullability = argumentNullability;

        if (argumentNullability.size() != dataAtom.getArity())
            throw new IllegalArgumentException("A nullability entry must be provided for each argument in the atom");

        if (this.argumentNullability.get(arrayIndexIndex))
            throw new IllegalArgumentException("The array index term must not be nullable");
    }

    @Override
    public Variable getArrayVariable() {
        return arrayVariable;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        throw new UnsupportedOperationException("TODO: support it");
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return Stream.concat(
                Stream.of(arrayVariable),
                dataAtom.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }
//    @Override
//    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
//        throw new UnsupportedOperationException("TODO: support it");
//    }
//
//    @Override
//    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
//        throw new UnsupportedOperationException("TODO: support it");
//    }

    protected String toString(String prefix) {
        return prefix + " " + arrayVariable + " -> " + dataAtom;
    }

    @Override
    public VariableOrGroundTerm getArrayIndexTerm() {
        return dataAtom.getTerm(arrayIndexIndex);
    }

    @Override
    public DataAtom<RelationPredicate> getDataAtom() {
        return dataAtom;
    }

    @Override
    public int getArrayIndexIndex() {
        return arrayIndexIndex;
    }


//    @Override
//    public SubstitutionResults<N> applyAscendingSubstitution(
//            ImmutableSubstitution<? extends ImmutableTerm> substitution, QueryNode childNode, IntermediateQuery query) {
//        return applySubstitution(substitution);
//    }

    protected N applySubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        DataAtom<RelationPredicate> newAtom = substitution.applyToDataAtom(dataAtom);
        ImmutableTerm newArrayTerm = substitution.apply(getArrayVariable());
        if (!(newArrayTerm instanceof Variable))
            throw new InvalidIntermediateQueryException("The array of a FlattenNode must remain a variable");
        return newNode((Variable) newArrayTerm, arrayIndexIndex, newAtom, argumentNullability);
    }

//    @Override
//    public N rename(InjectiveVar2VarSubstitution renamingSubstitution) {
//        //noinspection OptionalGetWithoutIsPresent
//        return applySubstitution(renamingSubstitution).getOptionalNewNode().get();
//    }

    @Override
    public abstract N clone();

//    protected abstract N newFlattenNode(Variable newArrayVariable, DataAtom newAtom);

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of(arrayVariable);
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return dataAtom.getVariables();
    }

    @Override
    public ImmutableList<Boolean> getArgumentNullability() {
        return argumentNullability;
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(
                applySubstitution(descendingSubstitution),
                applyDescendingSubstitution(
                        descendingSubstitution,
                        constraint,
                        child
                ));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        return iqFactory.createUnaryIQTree(
                applySubstitution(descendingSubstitution),
                applyDescendingSubstitutionWithoutOptimizing(
                        descendingSubstitution,
                        child
                ));
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return ImmutableSet.of();
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().contains(arrayVariable)){
            throw new InvalidIntermediateQueryException("The array variable of Flatten Node "+this+ "is not projected by its child");
        }
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the filter
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }
}
