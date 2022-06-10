package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class FlattenNodeImpl extends CompositeQueryNodeImpl implements FlattenNode {



    private final Variable flattenedVariable;
    private final Variable outputVariable;
    private final Optional<Variable> indexVariable;
    private final DBTermType flattenedType;

    @AssistedInject
    private FlattenNodeImpl(@Assisted("outputVariable") Variable outputVariable,
                            @Assisted("flattenedVariable") Variable flattenedVariable,
                            @Assisted Optional<Variable> indexVariable,
                            @Assisted DBTermType flattenedType,
                            SubstitutionFactory substitutionFactory,
                            IntermediateQueryFactory iqFactory,
                            TermFactory termFactory) {
        super(substitutionFactory, termFactory, iqFactory);
        this.outputVariable = outputVariable;
        this.flattenedVariable = flattenedVariable;
        this.indexVariable = indexVariable;
        this.flattenedType = flattenedType;
    }

    @Override
    public Variable getFlattenedVariable() {
        return flattenedVariable;
    }

    @Override
    public DBTermType getFlattenedType() {
        return flattenedType;
    }

    @Override
    public Variable getOutputVariable() {
        return outputVariable;
    }

    @Override
    public Optional<Variable> getIndexVariable() {
        return indexVariable;
    }

    @Override
    public Optional<TermType> inferOutputType(Optional<TermType> flattenedVarType) {
        Optional<DBTermType> optTermType = flattenedVarType
                .filter(t -> t instanceof DBTermType)
                .map(t -> (DBTermType) t);
        if(optTermType.isPresent()){
            DBTermType type = optTermType.get();
            switch (type.getCategory()){
                case JSON:
                    return flattenedVarType;
                case ARRAY:
                    throw new FlattenedVariableTypeException("Array DBType not yet implemented");
                default:
                    throw new FlattenedVariableTypeException(
                            String.format(
                                    "Unexpected Datatype %s for flattened variable %s",
                                    type,
                                    flattenedVariable
                            ));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TermType> getIndexVariableType() {
        return Optional.empty();
    }

    @Override
    public ImmutableSet<Variable> getVariables(ImmutableSet<Variable> childVariables) {
        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
        childVariables.stream()
                .filter(v -> v != flattenedVariable)
                .forEach(builder::add);
        builder.add(outputVariable);
        indexVariable.ifPresent(builder::add);
        return builder.build();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of(flattenedVariable);
    }

    @Override
    public String toString() {
        return "FLATTEN  [" +
                outputVariable + "/flatten(" + flattenedVariable + ")" +
                indexVariable.map(v -> ", " + v + "/indexIn(" + flattenedVariable + ")").orElse("") +
                "]";
    }


    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of(flattenedVariable);
    }

    /**
     * TODO: see what is required here
     */
    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(
                applySubstitution(descendingSubstitution),
                child.applyDescendingSubstitution(
                        descendingSubstitution,
                        constraint
                ));
    }

    protected Variable applySubstitution(Variable var, ImmutableSubstitution<? extends VariableOrGroundTerm> sub) {
        ImmutableTerm newVar = sub.apply(var);
        if (!(newVar instanceof Variable))
            throw new InvalidIntermediateQueryException("This substitution application should yield a variable");
        return (Variable) newVar;
    }

    protected Optional<Variable> applySubstitution(Optional<Variable> var, ImmutableSubstitution<? extends VariableOrGroundTerm> sub) {
        return var.map(variable -> applySubstitution(variable, sub));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {

        return iqFactory.createUnaryIQTree(
                applySubstitution(descendingSubstitution),
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution));
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return ImmutableSet.of();
    }

    /**
     * Same implementation as FilterNode
     */
    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.removeDistincts();
        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChild.equals(child));
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    /**
     * Unique constraints are lost after flattening
     */
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return ImmutableSet.of();
    }

    /**
     * Only the flattened variable is required
     */
    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree child) {
        return child.getNotInternallyRequiredVariables().stream()
                .filter(v -> !v.equals(flattenedVariable))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().contains(flattenedVariable)) {
            throw new InvalidIntermediateQueryException(
                    String.format( "Variable %s is flattened by Node %s but is not projected by its child",
                            flattenedVariable,
                            this
                    ));
        }
    }

    /**
     * Assumption: a flattened array can contain null values.
     * <p>
     * If so, even a relaxed flatten has no incidence on variable nullability
     * (a tuple may map the output variable to null, and the position variable to a non-null value)
     */
    @Override
    public VariableNullability getVariableNullability(IQTree child) {

        return child.getVariableNullability().extendToExternalVariables(
                Stream.of(Optional.of(outputVariable), indexVariable)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
        );
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this, child.propagateDownConstraint(constraint));
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
        builder.add(outputVariable);
        indexVariable.ifPresent(builder::add);
        return builder.build();
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the flatten
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
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);
        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(applySubstitution(renamingSubstitution), newChild, newTreeCache);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlattenNodeImpl that = (FlattenNodeImpl) o;
        return
                flattenedVariable.equals(that.flattenedVariable) &&
                outputVariable.equals(that.outputVariable) &&
                indexVariable.equals(that.indexVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flattenedVariable, outputVariable, indexVariable);
    }

    @Override
    public FlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFlatten(tree, this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformFlatten(tree,this, child, context);
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
        Optional<Variable> sIndexVar = applySubstitution(indexVariable, sub);
        return sFlattenedVar.equals(flattenedVariable) &&
                sOutputVar.equals(outputVariable) &&
                sIndexVar.equals(indexVariable) ?
                this :
                iqFactory.createFlattenNode(
                        sOutputVar,
                        sFlattenedVar,
                        sIndexVar,
                        flattenedType
                );
    }

    private class FlattenedVariableTypeException extends OntopInternalBugException {
        protected FlattenedVariableTypeException (String message) {
            super(message);
        }
    }
}
