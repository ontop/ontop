package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryNodeException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

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
                              IntermediateQueryFactory iqFactory) {
        super(substitutionFactory, iqFactory);
        this.arrayVariable = arrayVariable;
        this.arrayIndexIndex = arrayIndexIndex;
        this.dataAtom = dataAtom;

        if ((arrayIndexIndex >= dataAtom.getArguments().size())
                || arrayIndexIndex < 0)
            throw new InvalidQueryNodeException("The array index index must correspond to an argument of the data atom");
        this.argumentNullability = argumentNullability;

        if (argumentNullability.size() != dataAtom.getArity())
            throw new InvalidQueryNodeException("A nullability entry must be provided for each argument in the atom");

        if (this.argumentNullability.get(arrayIndexIndex))
            throw new InvalidQueryNodeException("The array index term must not be nullable");
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
        return dataAtom.getVariables().stream()
                .collect(ImmutableCollectors.toSet());
    }

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

    protected N applySubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        DataAtom<RelationPredicate> newAtom = substitution.applyToDataAtom(dataAtom);
        ImmutableTerm newArrayTerm = substitution.apply(getArrayVariable());
        if (!(newArrayTerm instanceof Variable))
            throw new InvalidIntermediateQueryException("The array of a FlattenNode must remain a variable");
        return newNode((Variable) newArrayTerm, arrayIndexIndex, newAtom, argumentNullability);
    }

    @Override
    public abstract N clone();

//    protected abstract N newFlattenNode(Variable newArrayVariable, DataAtom newAtom);

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of(arrayVariable);
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

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = childIQTree.liftBinding(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        IQProperties liftedProperties = currentIQProperties.declareLifted();

        if (newChildRoot instanceof ConstructionNode)
            return liftChildConstructionNode((ConstructionNode) newChildRoot, (UnaryIQTree) newChild, liftedProperties);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        return iqFactory.createUnaryIQTree(this, newChild, liftedProperties);
    }

    /**
     * Lifts the construction node above and applies its substitution
     */
    private IQTree liftChildConstructionNode(ConstructionNode liftedNode, UnaryIQTree newChild, IQProperties liftedProperties) {
        UnaryIQTree newFlattenTree = iqFactory.createUnaryIQTree(
                applySubstitution(liftedNode.getSubstitution()),
                newChild.getChild(),
                liftedProperties
        );
        return iqFactory.createUnaryIQTree(liftedNode, newFlattenTree, liftedProperties);
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        ImmutableList<? extends VariableOrGroundTerm> atomArguments = dataAtom.getArguments();

        ImmutableSet<Variable> localVars = atomArguments.stream()
                .filter(t -> t instanceof Variable)
                .map (v -> (Variable)v)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> childVariables = child.getVariables();
        Stream<Variable> nullableLocalVars = localVars.stream()
                .filter(v -> !v.equals(arrayVariable) && !isRepeatedIn(v, dataAtom) && !childVariables.contains(v) ||
                        !isDeclaredNonNullable(v, atomArguments));

        return new VariableNullabilityImpl(Stream.concat(
                child.getVariableNullability().getNullableGroups().stream()
                        .filter(g -> filterNullabilityGroup(g, localVars)),
                nullableLocalVars
                        .map(v -> ImmutableSet.of(v)))
                .collect(ImmutableCollectors.toSet())
        );
    }

    private boolean isDeclaredNonNullable(Variable v, ImmutableList<? extends VariableOrGroundTerm> atomArguments) {
        return !argumentNullability.get(atomArguments.indexOf(v));
    }

    private boolean isRepeatedIn(Variable v, DataAtom<RelationPredicate> dataAtom) {
        return dataAtom.getArguments().stream()
                .filter(t -> t.equals(v))
                .count() > 1;
    }

    private boolean filterNullabilityGroup(ImmutableSet<Variable> group, ImmutableSet<Variable> localVars) {
        return group.stream()
                .anyMatch(v -> localVars.contains(v));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this, child.propagateDownConstraint(constraint));
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getDataAtom().getVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        throw new FlattenNodeException("This method should not be called");
    }

    private static class FlattenNodeException extends OntopInternalBugException {
        FlattenNodeException(String message) {
            super(message);
        }
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        ImmutableList<? extends VariableOrGroundTerm> atomArguments = getDataAtom().getArguments();
        if (variable.equals(getArrayVariable()))
            return false;
        else if (atomArguments.contains(variable)) {
            /*Look for a second occurrence among the variables projected by the child --> implicit filter condition and thus not nullable */
            if(query.getVariables(query.getFirstChild(this)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("A FlattenNode must have a child")))
                    .contains(variable))
                return false;

            /*
             *Look for a second occurrence of the variable in the array --> implicit filter condition and thus not nullable
             */
            int firstIndex = atomArguments.indexOf(variable);
            if (!argumentNullability.get(firstIndex))
                return false;
            int arity = atomArguments.size();
            if (firstIndex >= (arity - 1))
                return true;
            int secondIndex = atomArguments.subList(firstIndex + 1, arity).indexOf(variable);
            return secondIndex < 0;
        }
        else {
            return query.getFirstChild(this)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("A FlattenNode must have a child"))
                    .isVariableNullable(query, variable);
        }
    }
}
