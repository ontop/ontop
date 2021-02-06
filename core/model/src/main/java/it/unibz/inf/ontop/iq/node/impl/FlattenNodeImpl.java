package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FlattenNodeImpl extends CompositeQueryNodeImpl implements FlattenNode {


    private static final String RELAXED_FLATTEN_PREFIX = "RELAXED-FLATTEN";
    private static final String STRICT_FLATTEN_PREFIX = "STRICT-FLATTEN";

    private final Variable flattenedVariable;
    private final Variable outputVariable;
    private final Optional<Variable> positionVariable;
    private final boolean isStrict;


    @AssistedInject
    private FlattenNodeImpl(@Assisted Variable flattenedVariable,
                            @Assisted Variable outputVariable,
                            @Assisted Optional<Variable> positionVariable,
                            @Assisted boolean isStrict,
                            SubstitutionFactory substitutionFactory,
                            IntermediateQueryFactory iqFactory) {
        super(substitutionFactory, iqFactory);
        this.flattenedVariable = flattenedVariable;
        this.outputVariable = outputVariable;
        this.positionVariable = positionVariable;
        this.isStrict = isStrict;
    }

    @AssistedInject
    private FlattenNodeImpl(@Assisted Variable flattenedVariable,
                            @Assisted Variable outputVariable,
                            @Assisted Variable positionVariable,
                            @Assisted boolean isStrict,
                            SubstitutionFactory substitutionFactory,
                            IntermediateQueryFactory iqFactory) {
        super(substitutionFactory, iqFactory);
        this.flattenedVariable = flattenedVariable;
        this.outputVariable = outputVariable;
        this.positionVariable = Optional.of(positionVariable);
        this.isStrict = isStrict;
    }

    @Override
    public Variable getFlattenedVariable() {
        return flattenedVariable;
    }

    @Override
    public Variable getOutputVariable() {
        return outputVariable;
    }

    @Override
    public Optional<Variable> getPositionVariable() {
        return positionVariable;
    }

    @Override
    public boolean isStrict() {
        return isStrict;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return positionVariable.isPresent() ?
                ImmutableSet.of(flattenedVariable, outputVariable, positionVariable.get()) :
                ImmutableSet.of(flattenedVariable, outputVariable);
    }

    @Override
    public String toString() {
        String prefix = isStrict ?
                STRICT_FLATTEN_PREFIX :
                RELAXED_FLATTEN_PREFIX;

        return prefix + " [" +
                outputVariable + "/FLATTEN(" + flattenedVariable + ")" +
                (positionVariable.isPresent() ?
                        positionVariable.get() + "/POSITION_IN(" + flattenedVariable + ")" :
                        ""
                ) +
                "]";
    }

//    protected abstract N newFlattenNode(Variable newArrayVariable, DataAtom newAtom);

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of(flattenedVariable);
    }

//    @Override
//    public ImmutableList<Boolean> getArgumentNullability() {
//        return argumentNullability;
//    }

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

    protected Variable applySubstitution(Variable var, ImmutableSubstitution<? extends VariableOrGroundTerm> sub) {
        ImmutableTerm newVar = sub.apply(var);
        if (!(newVar instanceof Variable))
            throw new InvalidIntermediateQueryException("This substitution application should yield a variable");
        return (Variable) newVar;
    }

    protected Optional<Variable> applySubstitution(Optional<Variable> var, ImmutableSubstitution<? extends VariableOrGroundTerm> sub) {
        return var.isPresent() ?
                Optional.of(applySubstitution(var.get(), sub)) :
                Optional.empty();
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
    public IQTree removeDistincts(IQTree child, IQProperties iqProperties) {
        return ;
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return ;
    }

    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree child) {
        return ;
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().contains(flattenedVariable)) {
            throw new InvalidIntermediateQueryException("The variable flattened by node " + this + "is not projected by its child");
        }
    }

//    @Override
//    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
//        IQTree newChild = child.liftIncompatibleDefinitions(variable);
//        QueryNode newChildRoot = newChild.getRootNode();
//
//        /*
//         * Lift the union above the filter
//         */
//        if ((newChildRoot instanceof UnionNode)
//                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
//            UnionNode unionNode = (UnionNode) newChildRoot;
//            ImmutableList<IQTree> grandChildren = newChild.getChildren();
//
//            ImmutableList<IQTree> newChildren = grandChildren.stream()
//                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
//                    .collect(ImmutableCollectors.toList());
//
//            return iqFactory.createNaryIQTree(unionNode, newChildren);
//        }
//        return iqFactory.createUnaryIQTree(this, newChild);
//    }

//    @Override
//    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
//        IQTree newChild = childIQTree.liftBinding(variableGenerator);
//        QueryNode newChildRoot = newChild.getRootNode();
//
//        IQProperties liftedProperties = currentIQProperties.declareLifted();
//
//        if (newChildRoot instanceof ConstructionNode)
//            return liftChildConstructionNode((ConstructionNode) newChildRoot, (UnaryIQTree) newChild, liftedProperties);
//        else if (newChildRoot instanceof EmptyNode)
//            return newChild;
//        return iqFactory.createUnaryIQTree(this, newChild, liftedProperties);
//    }

//    /**
//     * Lifts the construction node above and applies its substitution
//     */
//    private IQTree liftChildConstructionNode(ConstructionNode liftedNode, UnaryIQTree newChild, IQProperties liftedProperties) {
//        UnaryIQTree newFlattenTree = iqFactory.createUnaryIQTree(
//                applySubstitution(liftedNode.getSubstitution()),
//                newChild.getChild(),
//                liftedProperties
//        );
//        return iqFactory.createUnaryIQTree(liftedNode, newFlattenTree, liftedProperties);
//    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        ImmutableList<? extends VariableOrGroundTerm> atomArguments = dataAtom.getArguments();

        ImmutableSet<Variable> localVars = atomArguments.stream()
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable) v)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> childVariables = child.getVariables();
        Stream<Variable> nullableLocalVars = localVars.stream()
                .filter(v -> !v.equals(flattenedVariable) && !isRepeatedIn(v, dataAtom) && !childVariables.contains(v) &&
                        canNull(v, atomArguments));

        return new VariableNullabilityImpl(Stream.concat(
                child.getVariableNullability().getNullableGroups().stream()
                        .filter(g -> filterNullabilityGroup(g, localVars)),
                nullableLocalVars
                        .map(v -> ImmutableSet.of(v)))
                .collect(ImmutableCollectors.toSet())
        );
    }

    private boolean canNull(Variable v, ImmutableList<? extends VariableOrGroundTerm> atomArguments) {
        return canNull(v, atomArguments.iterator(), 0, dataAtom.getPredicate().getRelationDefinition().getAttributes());
    }

    private boolean canNull(Variable v, UnmodifiableIterator<? extends VariableOrGroundTerm> it, int i, List<Attribute> attributes) {
        if (it.hasNext()) {
            if (it.next().equals(v) && !attributes.get(i).canNull()) {
                return false;
            }
            return canNull(v, it, i + 1, attributes);
        }
        return true;
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
        return ImmutableSet.of(flattenedVariable);
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
        if (positionVariable.isPresent() && variable.equals(positionVariable.get()))
            return isStrict;
        if (variable.equals(outputVariable)) {
            return false;
        }
        return query.getFirstChild(this)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A FlattenNode must have a child"))
                .isVariableNullable(query, variable);
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        // TODO: check whether some normalization is needed
        return iqFactory.createUnaryIQTree(
                this,
                normalizeForOptimization(child, variableGenerator, currentIQProperties)
        );
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        // Without further information about the flattened arrays, we cannot assume that distinctness is preserved after flattening
        // It would be the case if we know that each flattened array contains distinct values (i.e. is a set)
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        return ;
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);
        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(applySubstitution(renamingSubstitution), newChild, newTreeCache);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof FlattenNode) {
            FlattenNode flattenNode = (FlattenNode) node;
            return flattenNode.getFlattenedVariable().equals(flattenedVariable) &&
                    flattenNode.getOutputVariable().equals(outputVariable) &&
                    flattenNode.getPositionVariable().equals(positionVariable) &&
                    flattenNode.isStrict() == isStrict;
        }
        return false;
    }

    @Override
    public boolean isEquivalentTo(QueryNode node) {
        return isSyntacticallyEquivalentTo(node);
    }

    @Override
    public FlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public FlattenNode clone() {
        return iqFactory.createFlattenNode(flattenedVariable, outputVariable, positionVariable, isStrict);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFlatten(tree, this, child);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitFlatten(this, child);
    }

    /**
     * Avoids creating an instance if unnecessary (a similar optimization is implemented for Filter Nodes)
     */
    private FlattenNode applySubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> sub) {
        Variable sFlattenedVar = applySubstitution(flattenedVariable, sub);
        Variable sOutputVar = applySubstitution(outputVariable, sub);
        Optional<Variable> sPositionVar = applySubstitution(positionVariable, sub);
        return sFlattenedVar.equals(flattenedVariable) &&
                sOutputVar.equals(outputVariable) &&
                sPositionVar.equals(positionVariable)?
                this:
                iqFactory.createFlattenNode(
                        applySubstitution(flattenedVariable, sub),
                        applySubstitution(outputVariable, sub),
                        applySubstitution(positionVariable, sub),
                        isStrict
                );
    }
}
